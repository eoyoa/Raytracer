import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL
import org.khronos.webgl.Float32Array
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.Mat4
import vision.gears.webglmath.Vec4
import kotlin.js.Date

class Scene (
  val gl : WebGL2RenderingContext) : UniformProvider("scene") {

  val vsQuad = Shader(gl, GL.VERTEX_SHADER, "quad-vs.glsl")
  val fstrace = Shader(gl, GL.FRAGMENT_SHADER, "trace-fs.glsl")
  val traceProgram = Program(gl, vsQuad, fstrace)
  val skyCubeTexture = TextureCube(gl,
      "media/posx512.jpg", "media/negx512.jpg",
      "media/posy512.jpg", "media/negy512.jpg",
      "media/posz512.jpg", "media/negz512.jpg"
    )
  val traceMaterial = Material(traceProgram).apply{
    this["envTexture"]?.set( skyCubeTexture )
  }
  val quadGeometry = TexturedQuadGeometry(gl)
  val traceMesh = Mesh(traceMaterial, quadGeometry)

  val camera = PerspectiveCamera()

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  val quadrics = Array(8) { Quadric(it) }

  init{
    for (quadric in quadrics) {
      val random = Vec3();
      random.randomize(Vec3(-9.0f, -9.0f, -9.0f), Vec3 (9.0f, 9.0f, 9.0f))
      quadric.surface.set(Quadric.unitSphere)
      quadric.surface.transform(Mat4().set().scale(3.0f, 3.0f, 3.0f).translate(random))
      quadric.clipper.set(Quadric.unitSlab)
      quadric.clipper.transform(Mat4().set().scale(1.0f, 0.5f, 1.0f))

      // todo: just testing color
      quadric.color.set(0f, 0f, 1f)

      // todo: testing reflectance
      quadric.reflectance.set(0.5f)
    }
  }

  val lights = Array(1) { Light(it) }
  init {
    for (light in lights) {
      val random = Vec3()
      light.position.set(Vec4(0.5f, 0.5f, 0.5f, 0f))
      light.powerDensity.set(0.5f, 0.5f, 0.5f)

    }
  }

  init{
    gl.enable(GL.DEPTH_TEST)
    addComponentsAndGatherUniforms(*Program.all)
  }

  fun resize(gl : WebGL2RenderingContext, canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)
    camera.setAspectRatio(canvas.width.toFloat() / canvas.height.toFloat())
  }

  fun update(gl : WebGL2RenderingContext, keysPressed : Set<String>) {

    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t  = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f    
    timeAtLastFrame = timeAtThisFrame

    camera.move(dt, keysPressed)

    // clear the screen
    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)
    gl.clearDepth(1.0f)
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)

    traceMesh.draw(camera, *quadrics, *lights)

  }
}
