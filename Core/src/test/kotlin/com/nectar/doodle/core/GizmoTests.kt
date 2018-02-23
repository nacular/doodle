package com.nectar.doodle.core

import com.nectar.doodle.JsName
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle.Companion.Empty
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets.Companion.None
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/23/18.
 */
class GizmoTests {

    @Test @JsName("defaults")
    fun `defaults valid`() {
        expect("", "Gizmo::toolTipText") { object: Gizmo() {}.toolTipText }

        mapOf(
                Gizmo::x                   to 0.0,
                Gizmo::y                   to 0.0,
                Gizmo::font                to null,
                Gizmo::size                to Size.Empty,
                Gizmo::width               to 0.0,
                Gizmo::parent              to null,
                Gizmo::height              to 0.0,
                Gizmo::bounds              to Empty,
                Gizmo::cursor              to null,
                Gizmo::enabled             to true,
                Gizmo::visible             to true,
                Gizmo::insets_             to None,
                Gizmo::layout_             to null,
                Gizmo::position            to Origin,
                Gizmo::hasFocus            to false,
                Gizmo::focusable           to true,
                Gizmo::idealSize           to null,
                Gizmo::minimumSize         to Size.Empty,
                Gizmo::monitorsMouse       to false,
                Gizmo::foregroundColor     to null,
                Gizmo::backgroundColor     to null,
                Gizmo::monitorsKeyboard    to true,
                Gizmo::monitorsMouseWheel  to true,
                Gizmo::monitorsMouseMotion to false,
                Gizmo::monitorsDisplayRect to false
        ).forEach { validateDefault(it.key, it.value) }
    }

    private fun <T> validateDefault(p: KProperty<T>, default: T?) {
        expect(default, "$p defaults to $default") { p.call(object: Gizmo() {}) }
    }
}