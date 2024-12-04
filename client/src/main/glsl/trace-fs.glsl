#version 300 es 
precision highp float;

out vec4 fragmentColor;
in vec4 rayDir;

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
} quadrics[16];

float intersectClippedQuadric(vec4 e, vec4 d, mat4 A, mat4 B) {

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
	if (dot(hit1 * B, hit1) < 0.0)
	{
		t1 = -1.0;
	}

	vec4 hit2 = e + d * t2;
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
	for (int i = 0; i < 16; i++)
	{
		float tLocal = intersectClippedQuadric(e, d, quadrics[i].surface, quadrics[i].clipper);
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

void main(void) {
	vec4 e = vec4(camera.position, 1);
	vec4 d = vec4(normalize(rayDir.xyz), 0);

	// initialize best T and best index
	float bestT = 10000.0;
	int bestI = 0;

	bool hitSomething = findBestHit(e, d, bestT, bestI);

	if (!hitSomething) {
		// basically didn't hit anything, so draw the background
		fragmentColor = texture (material.envTexture, d.xyz);
		return;
	}

	// compute intersection point
	float t = bestT;
	vec4 hit = e + d * t;

	// compute quadric normal
	mat4 A = quadrics[bestI].surface;
	vec3 normal = normalize( (hit * A + A * hit).xyz );

	// set fragment color to whatever you want
	// todo: proper shading
	fragmentColor = vec4(normal, 1.0);

//	while (validHit && killer >0)
//	{
//		killer--;
//		bool isValidHit = findBestHit(e, d, bestT, bestI);
//
//		if (isValidHit) {
//			float t = bestT;
//			mat4 A = quadrics[bestI].surface;
//			vec4 hit = e + d * t;
//			vec3 normal = normalize( (hit * A + A * hit).xyz );
//			vec3 reflectedDir = reflect (d.xyz, normal);
//
//			if (dot (normal, -d.xyz) < 0.0)
//			{
//				normal *= -1.0;
//			}
//
//			e = vec4 (hit.xyz + normal * 0.0001, 1.0);
//			d = vec4 (reflectedDir.xyz, 0.0);
//
//			bestT = 10000.0;
//		}
//		else
//		{
//			validHit = false;
//		}
//	}
//
//	fragmentColor = texture (material.envTexture, d.xyz);
}
