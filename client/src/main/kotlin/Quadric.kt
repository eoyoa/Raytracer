import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Mat4
import vision.gears.webglmath.Vec1
import vision.gears.webglmath.Vec3

class Quadric(i : Int) : UniformProvider("""quadrics[${i}]""") {
  val surface by QuadraticMat4(unitSphere.clone())
  val clipper by QuadraticMat4(unitSlab.clone())
  val color by Vec3(0.5f, 0.5f, 0.5f)
  val reflectance by Vec1(0.18f)
  val holeyness by Vec1(0f)

  fun translate(position: Vec3) {
    val translation = Mat4().translate(position)
    surface.transform(translation)
    clipper.transform(translation)
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