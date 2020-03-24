package com.nectar.doodle.controls.buttons

import com.nectar.doodle.JsName
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/15/20.
 */
class PushButtonTests {
    @Test @JsName("defaults")
    fun `defaults valid`() {
        mapOf(
            PushButton::text to "",
            PushButton::icon to null
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test @JsName("clickWorks")
    fun `click works`() {
        val model = mockk<ButtonModel>(relaxed = true)

        PushButton(model = model).apply {
            click()

            verifySequence {
                model.armed   = true
                model.pressed = true
                model.pressed = false
                model.armed   = false
            }
        }
    }

    @Test @JsName("cannotClickDisabled")
    fun `cannot click disabled`() {
        val model = mockk<ButtonModel>(relaxed = true)

        PushButton(model = model).apply {
            enabled = false
            click()

            verify { model wasNot Called }
        }
    }

    private fun <T> validateDefault(p: KProperty1<PushButton, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(PushButton()) }
    }
}