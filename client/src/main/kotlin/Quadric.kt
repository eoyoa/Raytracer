import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Mat4
import vision.gears.webglmath.Vec1
import vision.gears.webglmath.Vec3

class Quadric(i : Int) : UniformProvider("""quadrics[${i}]""") {
  val surface by QuadraticMat4(unitSphere.clone())
  val clipper by QuadraticMat4(unitSlab.clone())

  val color by Vec3(1f, 1f, 1f)
  val secondColor by Vec3(0f, 0f, 0f)

  val reflectance by Vec1(0.18f)

  val holeyness by Vec1(0f)
  val mixFreq by Vec1(0f)
  val normalBumpFreq by Vec1(0.0f)

  fun translate(position: Vec3) {
    val translation = Mat4().translate(position)
    surface.transform(translation)
    clipper.transform(translation)
  }

  fun scale(scalar: Float) {
    val scaling = Mat4().scale(scalar, scalar, scalar)
    surface.transform(scaling)
    clipper.transform(scaling)
  }

  fun rotate(angle: Float, axis: Vec3) {
    val rotation = Mat4().rotate(angle, axis)
    surface.transform(rotation)
    clipper.transform(rotation)
  }

  companion object {
    val unitSphere = 
      Mat4(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -1.0f
      )
    val unitSlab = 
      Mat4(
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -1.0f
      ) 
    val plane = 
      Mat4(
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -0.0f
      )
    val cone =
      Mat4(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, -1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f
      )
  }

}