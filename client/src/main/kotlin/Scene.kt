import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL
import org.khronos.webgl.Float32Array
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.Mat4
import vision.gears.webglmath.Vec4
import kotlin.js.Date
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

  val fir = Array(2) { Quadric(it) }
  init {
    val leafColor = Vec3(0.004f,0.196f,0.125f)
    fir[0].apply {
      surface.apply {
        set(Quadric.cone)
        transform(
          Mat4()
            .translate(0f, 1f)
        )
      }

      clipper.apply {
        set(Quadric.unitSlab)
        negate()
      }

      reflectance.set(0f)

      color.set(leafColor)

      holeyness.set(0.5f)
    }

    fir[1].apply {
      surface.apply {
        set(Quadric.cone)
        transform(
          Mat4()
            .translate(0f, 1f)
        )
      }

      clipper.apply {
        set(Quadric.unitSlab)
        negate()
      }

      reflectance.set(0f)

      color.set(leafColor)

      holeyness.set(0.25f)

      translate(Vec3(0f, 1f))
    }
  }

  val snowman = Array(4) { Quadric(it + fir.size) }
  init {
    snowman[0].apply {
      surface.set(Quadric.unitSphere)
      clipper.apply {
        set(Quadric.plane)
        transform(
          Mat4().translate(0f, -2f)
        )
      }
    }
    snowman[1].apply {
      surface.set(Quadric.unitSphere)
      clipper.apply {
        set(Quadric.plane)
        transform(
          Mat4().translate(0f, -2f)
        )
      }
      scale(0.75f)
      translate(Vec3(0f, 1f))
    }
    snowman[2].apply {
      surface.set(Quadric.unitSphere)
      clipper.apply {
        set(Quadric.plane)
        transform(
          Mat4().translate(0f, -2f)
        )
      }
      scale(0.75f * 0.75f)
      translate(Vec3(0f, 2f))
    }
    snowman[3].apply {
      // nose
      surface.apply {
        set(Quadric.cone)
        transform(
          Mat4()
            .scale(0.25f, 1f, 0.25f)
            .translate(0f, 1f)
        )
      }
      clipper.apply {
        set(Quadric.unitSlab)
        negate()
      }
      rotate(-PI.toFloat() / 2, Vec3(0f, 0f, 1f).normalize())
      scale(0.25f)
      translate(Vec3(0.75f, 2.1f))

      color.set(1f,0.647f,0f)
      reflectance.set(0.09f)
    }

    val snowmanPos = Vec3(3f )
    for (quadric in snowman) {
      quadric.translate(snowmanPos)
    }
  }

  val floor = Quadric(fir.size + snowman.size)
  init {
    floor.apply {
      surface.apply {
        set(Quadric.unitSlab)
        transform(
          Mat4().translate(0f, -1f)
        )
      }
      clipper.set(Quadric.unitSlab)
      mixFreq.set(0.5f)
      color.set(1f,0.647f,0.31f)
      secondColor.set(0.545f,0.271f,0.075f)
      translate(Vec3(0f, 1f))
    }
  }

  val lights = Array(1) { Light(it) }
  init {
    for (light in lights) {
      light.position.set(Vec4(1f, 0.5f, 1f, 0f))
      light.powerDensity.set(1f,1f,1f).normalize().timesAssign(0.75f)
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

    traceMesh.draw(camera, *lights, *fir, *snowman, floor)

  }
}
