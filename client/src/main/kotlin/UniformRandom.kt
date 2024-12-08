import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec1
import vision.gears.webglmath.Vec2

class UniformRandom : UniformProvider("uniformRandom") {
    val randf1 by Vec1();
    val randf2 by Vec2();

    fun randomize() {
        randf1.randomize(0f, 1f)
        randf2.randomize(0f, 1f)
    }
}