package io.nacular.doodle.text

import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.ColorPaint
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 10/17/19.
 */
class StyledTextTests {
    @Test @JsName("equalsWorks"  ) fun `equals works`  () = expect(StyledText("foo", foreground = ColorPaint(Red))) { StyledText("foo", foreground = ColorPaint(Red)) }
    @Test @JsName("hashcodeWorks") fun `hashcode works`() = expect(StyledText("foo", foreground = ColorPaint(Red)).hashCode()) { StyledText("foo", foreground = ColorPaint(Red)).hashCode() }
    @Test @JsName("subString"    ) fun `substring works`() {
        val text = StyledText("abc") .. Red("def") .. Black("ghijk")

        val text2 = text.subString(3 .. 7)

        expect(text.text.substring(3 .. 7)) { text2.text }
        expect(Red("def") .. Black("gh")) { text2 }
    }
}