package io.nacular.doodle.layout

import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.Constrainer
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.center
import io.nacular.doodle.layout.constraints.fill
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 6/29/23.
 */
class ConstrainerTests {
    private data class Args(
        val rectangle: Rectangle,
        val within   : Rectangle,
        val minSize  : Size  = Size.Empty,
        val idealSize: Size? = null,
        val using    : ConstraintDslContext.(Bounds) -> Unit
    )

    @Test fun `positions rect properly`() {
        val constrainer = Constrainer()
        val within      = Rectangle(10, 10, 100, 100)

        listOf(
            Args(Empty,                      within, using = fill  ) to within,
            Args(Rectangle(size = Size(50)), within, using = center) to Rectangle(within.x + (within.width - 50) / 2, within.y + (within.height - 50) / 2, 50.0, 50.0),
        ).forEach { (args, expectation) ->
            expect(expectation) {
                constrainer(args.rectangle, args.within, forceSetup = false, args.using)
            }
        }
    }
}