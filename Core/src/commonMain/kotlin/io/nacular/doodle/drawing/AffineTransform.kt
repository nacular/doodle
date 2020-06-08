package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.AffineMatrix3D
import io.nacular.doodle.utils.matrixOf
import io.nacular.doodle.utils.times
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Angle.Companion.radians
import com.nectar.measured.units.Measure
import kotlin.math.cos
import kotlin.math.sin

@Suppress("ReplaceSingleLineLet")
class AffineTransform private constructor(private val matrix: AffineMatrix3D) {

    /**
     * Creates a transform with the given properties
     *
     * @param scaleX     how much to scale the x direction
     * @param shearX     how much to shear the x direction
     * @param translateX how much to translate in the x direction
     * @param scaleY     how much to scale the y direction
     * @param shearY     how much to shear the y direction
     * @param translateY how much to translate in the y direction
     */
    constructor(
            scaleX    : Double = 1.0,
            shearX    : Double = 0.0,
            translateX: Double = 0.0,
            scaleY    : Double = 1.0,
            shearY    : Double = 0.0,
            translateY: Double = 0.0):
            this(AffineMatrix3D(scaleX, shearX, translateX,
                                shearY, scaleY, translateY))

    val isIdentity       = matrix.isIdentity
    val scaleX     get() = matrix[0, 0]
    val shearX     get() = matrix[0, 1]
    val translateX get() = matrix[0, 2]
    val shearY     get() = matrix[1, 0]
    val scaleY     get() = matrix[1, 1]
    val translateY get() = matrix[1, 2]

    operator fun times(other: AffineTransform) = AffineTransform(matrix * other.matrix)

    fun scale(x: Double = 1.0, y: Double = x) = AffineTransform(
            matrix * AffineMatrix3D(
                    x,   0.0, 0.0,
                    0.0,   y, 0.0)
    )

    fun scale(around: Point, x: Double = 1.0, y: Double = x) = (this translate around).scale(x, y) translate -around

    infix fun translate(by: Point) = translate(by.x, by.y)

    fun translate(x: Double = 0.0, y: Double = 0.0) = AffineTransform(
            matrix * AffineMatrix3D(
                    1.0, 0.0, x,
                    0.0, 1.0, y)
    )

    fun skew(x: Double, y: Double) = AffineTransform(
            matrix * AffineMatrix3D(
                    1.0,   x, 0.0,
                      y, 1.0, 0.0)
    )

    infix fun rotate(by: Measure<Angle>): AffineTransform {
        val radians = by `in` radians
        val sin     = sin(radians)
        val cos     = cos(radians)

        return AffineTransform(
                matrix * AffineMatrix3D(
                        cos, -sin, 0.0,
                        sin,  cos, 0.0)
        )
    }

    fun rotate(around: Point, by: Measure<Angle>) = this translate around rotate by translate -around

    fun flipVertically  () = scale (1.0, -1.0)
    fun flipHorizontally() = scale(-1.0,  1.0)

    fun flipVertically  (at: Double) = this.translate(y = at).flipVertically  ().translate(y = -at)
    fun flipHorizontally(at: Double) = this.translate(x = at).flipHorizontally().translate(x = -at)

    val inverse: AffineTransform? by lazy {
        when {
            isIdentity -> this
            else       -> matrix.inverse?.let { AffineTransform(
                    it[0, 0], it[0, 1], it[0, 2],
                    it[1, 1], it[1, 0], it[1, 2]) }
        }
    }

    operator fun invoke(point: Point) = this(listOf(point)).first()

    /**
     * Transforms the given set of points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     */
    operator fun invoke(points: List<Point>) = points.map {
        val point   = matrixOf(3, 1) { col, row -> listOf(listOf(it.x), listOf(it.y), listOf(1.0))[row][col] }
        val product = matrix * point

        Point(product[0, 0], product[1, 0])
    }

    operator fun invoke(rectangle: Rectangle): ConvexPolygon = when {
        isIdentity -> rectangle
        else       -> this(rectangle.points).let { ConvexPolygon(it[0], it[1], it[2], it[3]) }
    }

    override fun toString() = matrix.toString()

    override fun hashCode(): Int = matrix.hashCode()

    private operator fun get(vararg values: Double) = values.toList()
    override fun equals(other: Any?): Boolean {
        if (this === other           ) return true
        if (other !is AffineTransform) return false

        if (matrix != other.matrix) return false

        return true
    }

    companion object {
        val Identity = AffineTransform()
    }
}
