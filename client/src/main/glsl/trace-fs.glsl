#version 300 es 
precision highp float;

out vec4 fragmentColor;
in vec4 rayDir;
// procedural solid texturing
in vec4 modelPosition;

uniform struct {
	samplerCube envTexture;
} material;

uniform struct {
  vec3 position;
  mat4 rayDirMatrix;
} camera;

uniform struct {
  mat4 surface;
  mat4 clipper;

	// material properties per quadric
	vec3 color;
	vec3 secondColor;

	// reflectance for raytracing
	float reflectance;

	// procedural solid texturing
	float holeyness;
	float mixFreq;
} quadrics[16];
const int NUM_QUADRICS = 16;

uniform struct {
	vec4 position;
	vec3 powerDensity;
} lights[8];
const int NUM_LIGHTS = 1;

// procedural texturing
float noise(vec3 r) {
	uvec3 s = uvec3(
	0x1D4E1D4E,
	0x58F958F9,
	0x129F129F);
	float f = 0.0;
	for(int i=0; i<16; i++) {
		vec3 sf =
		vec3(s & uvec3(0xFFFF))
		/ 65536.0 - vec3(0.5, 0.5, 0.5);

		f += sin(dot(sf, r));
		s = s >> 1;
	}
	return f / 32.0 + 0.5;
}
// end of proc. texturing

float intersectClippedQuadric(vec4 e, vec4 d, mat4 A, mat4 B, float holeyness) {
	float a = dot(d * A, d);
	float b = dot(d * A, e) + dot(e * A, d);
	float c = dot(e * A, e);

	float D = b * b - 4.0 * a * c;
	if (D < 0.0)
	{
		return -1.0;
	}

	D = sqrt (D);

	float t1 = (-b + D) / (2.0 * a);
	float t2 = (-b - D) / (2.0 * a);

	vec4 hit1 = e + d * t1;
	// throw out ray hit if should be hole
	float w1 = fract(hit1.x + pow(noise(hit1.xyz * holeyness), 2.0)) * 3.0;
	if (w1 < holeyness) {
		t1 = -1.0;
	}
	if (dot(hit1 * B, hit1) < 0.0)
	{
		t1 = -1.0;
	}

	vec4 hit2 = e + d * t2;
	// throw out ray hit if should be hole
	float w2 = fract(hit2.x + pow(noise(hit2.xyz * holeyness), 2.0)) * 3.0;
	if (w2 < holeyness) {
		t2 = -1.0;
	}
	if (dot(hit2 * B, hit2) < 0.0)
	{
		t2 = -1.0;
	}

	return (t1<0.0)?t2:((t2<0.0)?t1:min(t1, t2));
}

// takes ray e and d
bool findBestHit(vec4 e, vec4 d, out float bestT, out int bestIndex) {
	// maintains best hit's bestT and quadric index bestIndex
	bestT = 10000.0;
	bestIndex = 0;

	// !! ensure i does not go out of quadrics range
	for (int i = 0; i < NUM_QUADRICS; i++)
	{
		float tLocal = intersectClippedQuadric(e, d, quadrics[i].surface, quadrics[i].clipper, quadrics[i].holeyness);
		if (tLocal < bestT && tLocal > 0.0)
		{
			// new intersection's T is better than the best so far
			// so update bestT and bestIndex
			bestT = tLocal;
			bestIndex = i;
		}
	}

	return bestT > 0.0 && bestT < 10000.0;
}

// shading
vec3 shadeDiffuse(
	vec3 normal, vec3 lightDir,
	vec3 powerDensity, vec3 materialColor) {

	float cosa = clamp(dot(lightDir, normal), 0.0, 1.0);

	return powerDensity * materialColor * cosa + powerDensity;
}

vec3 shade(vec4 d, vec3 normal, vec4 worldPosition, int qI) {
	vec3 outputColor = vec3(0.0, 0.0, 0.0);

	// !! ensure i does not go out of lights range
	for (int i = 0; i < NUM_LIGHTS; i++) {
		vec3 lightDiff = lights[i].position.xyz - worldPosition.xyz * lights[i].position.w;
		vec3 lightDir = normalize (lightDiff); // lights[i].position.xyz
		float distanceSquared = dot(lightDiff, lightDiff);

		// see if we're in shadow (so we don't need to do anything)
		// check light source's visibility
		vec4 e = vec4(worldPosition.xyz + normal * 0.001, 1);
		vec4 d = vec4(lightDir, 0);

		float bestShadowT = 10000.0;
		int bestI = 0;
		bool shadowRayHitSomething = findBestHit(e, d, bestShadowT, bestI);

		if (shadowRayHitSomething && bestShadowT * lights[i].position.w <= sqrt( distanceSquared )) {
			// we in shadow fr
			continue;
		}

		// otherwise continue calculating contribution
		if (lights[i].position.w < 1.0) {
			distanceSquared = 1.0;
		}
		vec3 powerDensity = lights[i].powerDensity / distanceSquared; //lights[i].powerDensity

		vec3 color = quadrics[qI].color;
		// potentially mix color
		if (quadrics[qI].mixFreq > 0.0) {
			float w = fract( worldPosition.x * quadrics[qI].mixFreq
			+ pow(
				noise(worldPosition.xyz * 0.5),
				2.0)
			 * 3.0
			);

			color = mix( quadrics[qI].color, quadrics[qI].secondColor, w);
		}
		outputColor += shadeDiffuse(normal, lightDir, powerDensity, color);
	}

	return outputColor;
}
// end of shading

void main(void) {
	vec4 e = vec4(camera.position, 1);
	vec4 d = vec4(normalize(rayDir.xyz), 0);

	float w = 1.0;

	// iterative ray tracing
	for (int currBounce = 0; currBounce < 2 && w > 0.01; currBounce++) {
		// initialize best T and best index
		float bestT = 10000.0;
		int bestI = 0;

		// find best hit
		bool hitSomething = findBestHit(e, d, bestT, bestI);

		if (!hitSomething) {
			// didn't hit anything
			fragmentColor += texture (material.envTexture, d.xyz) * w;
			w = 0.0;
			continue;
		}

		// compute intersection point
		float t = bestT;
		vec4 hit = e + d * t;

		// compute quadric normal
		mat4 A = quadrics[bestI].surface;
		vec3 normal = normalize( (hit * A + A * hit).xyz );
		// don't forget to flip the normals
		if (dot (normal, -d.xyz) < 0.0)
		{
			normal *= -1.0;
		}

		// set fragment color to whatever you want
		fragmentColor.rgb += shade(d, normal, hit, bestI) * w;

		// compute reflected ray and update origin e and dir d
		vec3 reflectedDir = reflect (d.xyz, normal);
		e = vec4 (hit.xyz + normal * 0.0001, 1.0);
		d = vec4 (reflectedDir.xyz, 0.0);

		// accumulate reflectance
		w *= quadrics[bestI].reflectance;
	}

	// set fragment color w so it's a proper output
	fragmentColor.w = 1.0;
}
