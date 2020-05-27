package com.nectar.doodle.text

import com.nectar.doodle.JsName
import com.nectar.doodle.drawing.Color.Companion.Red
import com.nectar.doodle.drawing.ColorBrush
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 10/17/19.
 */
class StyledTextTests {
    @Test @JsName("equalsWorks"  ) fun `equals works`  () = expect(StyledText("foo", foreground = ColorBrush(Red))) { StyledText("foo", foreground = ColorBrush(Red)) }
    @Test @JsName("hashcodeWorks") fun `hashcode works`() = expect(StyledText("foo", foreground = ColorBrush(Red)).hashCode()) { StyledText("foo", foreground = ColorBrush(Red)).hashCode() }
}