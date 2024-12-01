package io.nacular.doodle.text

import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.ColorPaint
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 10/17/19.
 */
class StyledTextTests {
    @Test fun `equals works`  () =
        expect(StyledText("foo", foreground = ColorPaint(Color.Red))) {
            StyledText(
                "foo",
                foreground = ColorPaint(Color.Red)
            )
        }
    @Test fun `hashcode works`() =
        expect(StyledText("foo", foreground = ColorPaint(Color.Red)).hashCode()) {
            StyledText(
                "foo",
                foreground = ColorPaint(Color.Red)
            ).hashCode()
        }
    @Test fun `substring works`() {
        val text = StyledText("abc").. Red { "def" } .. Black { "ghijk" }

        val text2 = text.subString(3 .. 7)

        expect(text.text.substring(3..7)) { text2.text }
        expect(Red { "def" }..Black { "gh" }) { text2 }
    }
}