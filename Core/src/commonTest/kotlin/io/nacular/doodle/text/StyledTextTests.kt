package io.nacular.doodle.text

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
}