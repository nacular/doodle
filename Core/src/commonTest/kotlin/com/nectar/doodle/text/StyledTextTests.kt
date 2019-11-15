package com.nectar.doodle.text

import com.nectar.doodle.JsName
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.ColorBrush
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 10/17/19.
 */
class StyledTextTests {
    @Test @JsName("equalsWorks"  ) fun `equals works`  () = expect(StyledText("foo", foreground = ColorBrush(red))) { StyledText("foo", foreground = ColorBrush(red)) }
    @Test @JsName("hashcodeWorks") fun `hashcode works`() = expect(StyledText("foo", foreground = ColorBrush(red)).hashCode()) { StyledText("foo", foreground = ColorBrush(red)).hashCode() }
}