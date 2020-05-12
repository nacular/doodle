package com.nectar.doodle.controls.buttons

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Icon
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.Anchor.Left
import com.nectar.doodle.utils.ChangeObserver
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Right
import com.nectar.doodle.utils.PropertyObserver
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.VerticalAlignment.Bottom
import com.nectar.doodle.utils.VerticalAlignment.Middle
import io.mockk.Called
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/15/20.
 */
class ButtonTests {
    private class TestButton(text: String = "", icon: Icon<Button>? = null, model: ButtonModel): Button(text, icon, model) {
        override fun click() {}
    }

    @Test @JsName("defaults")
    fun `defaults valid`() {
        mapOf(
                Button::text                   to "",
                Button::icon                   to null,
                Button::behavior               to null,
                Button::selected               to false,
                Button::iconAnchor             to Left,
                Button::pressedIcon            to null,
                Button::disabledIcon           to null,
                Button::selectedIcon           to null,
                Button::mouseOverIcon          to null,
                Button::iconTextSpacing        to 4.0,
                Button::verticalAlignment      to Middle,
                Button::horizontalAlignment    to Center,
                Button::disabledSelectedIcon   to null,
                Button::mouseOverSelectedIcon  to null
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test @JsName("settersWork")
    fun `setters work`() {
        validateSetter(Button::text,                  "foo"                )
        validateSetter(Button::icon,                  mockk(relaxed = true))
        validateSetter(Button::behavior,              null                 )
        validateSetter(Button::iconAnchor,            Left                 )
        validateSetter(Button::pressedIcon,           mockk(relaxed = true))
        validateSetter(Button::disabledIcon,          mockk(relaxed = true))
        validateSetter(Button::selectedIcon,          mockk(relaxed = true))
        validateSetter(Button::mouseOverIcon,         mockk(relaxed = true))
        validateSetter(Button::iconTextSpacing,       5.6                  )
        validateSetter(Button::verticalAlignment,     Bottom               )
        validateSetter(Button::horizontalAlignment,   Right                )
        validateSetter(Button::disabledSelectedIcon,  mockk(relaxed = true))
        validateSetter(Button::mouseOverSelectedIcon, mockk(relaxed = true))
    }

    @Test @JsName("iconsFallback")
    fun `icons fallback`() {
        TestButton(icon = mockk(relaxed = true), model = mockk(relaxed = true)).apply {
            expect(pressedIcon          ) { icon         }
            expect(disabledIcon         ) { icon         }
            expect(selectedIcon         ) { icon         }
            expect(mouseOverIcon        ) { icon         }
            expect(disabledSelectedIcon ) { disabledIcon }
            expect(mouseOverSelectedIcon) { selectedIcon }
        }
    }

    @Test @JsName("selectionNotifiesModel")
    fun `selection notifies model`() {
        val model = mockk<ButtonModel>(relaxed = true)

        TestButton(model = model).apply {
            selected = true

            verify(exactly = 1) { model.selected = true }
        }
    }

    @Test @JsName("cannotSelectDisabled")
    fun `cannot select disabled`() {
        val model = mockk<ButtonModel>(relaxed = true)

        TestButton(model = model).apply {
            enabled  = false
            selected = true

            verify { model wasNot Called }
        }
    }

    @Test @JsName("installsUninstallsBehaviors")
    fun `installs and uninstalls behaviors`() {
        val button    = TestButton(model = mockk(relaxed = true))
        val behavior1 = mockk<Behavior<Button>>(relaxed = true)
        val behavior2 = mockk<Behavior<Button>>(relaxed = true)

        button.behavior = behavior1

        verify(exactly = 1) { behavior1.install  (button) }
        verify(exactly = 0) { behavior1.uninstall(any() ) }

        button.behavior = behavior2

        verify(exactly = 1) { behavior1.uninstall(button) }
        verify(exactly = 1) { behavior2.install  (button) }
    }

    @Test @JsName("modelChangeWorks")
    fun `model change works`() {
        val button = TestButton(model = mockk(relaxed = true))
        val model1 = mockk<ButtonModel>(relaxed = true)
        val model2 = mockk<ButtonModel>(relaxed = true)

        button.model = model1
        button.model = model2

        verify(exactly = 1) { model1.fired -= any() }
    }

    @Test @JsName("notifiesOfTextChange")
    fun `notifies of text change`() {
        val button    = TestButton(model = mockk(relaxed = true))
        val listener = mockk<PropertyObserver<Button, String>>(relaxed = true)

        button.textChanged += listener

        button.text = "foo"

        verify(exactly = 1) { listener(button, "", "foo") }
    }

    @Ignore @Test @JsName("notifiesWhenModelFires")
    fun `notifies when model fires`() {
        val listener = slot<ChangeObserver<ButtonModel>>()

        val model = mockk<ButtonModel>(relaxed = true).apply {
            every { fired += capture(listener) } just Runs
        }

        val myListener = mockk<ChangeObserver<Button>>()

        val button = TestButton(model = model).apply {
            fired += myListener
        }

        // FIXME: Update once re-usable test components (i.e. DoodleTest) are figured out
//        button.addedToDisplay(renderManager)

        listener.captured.invoke(model)

        verify(exactly = 1) { myListener.invoke(button) }
    }

    @Ignore @Test @JsName("stopsMonitoringModelWhenNotDisplayed")
    fun `stops monitoring model when not displayed`() {
        val model = mockk<ButtonModel>(relaxed = true)
        val renderManager = mockk<RenderManager>(relaxed = true)

        val button: Button = TestButton(model = model)

        // FIXME: Update once re-usable test components (i.e. DoodleTest) are figured out
//        button.addedToDisplay(renderManager)
    }

    @Test @JsName("renderWithoutBehaviorNoOp")
    fun `render without behavior no-op`() {
        val button = TestButton(model = mockk(relaxed = true))

        button.render(mockk(relaxed = true))
    }

    @Test @JsName("delegatesRenderToBehavior")
    fun `delegates render to behavior`() {
        val button   = TestButton(model = mockk(relaxed = true))
        val canvas   = mockk<Canvas>(relaxed = true)
        val behavior = mockk<Behavior<Button>>(relaxed = true)

        button.behavior = behavior

        button.render(canvas)

        verify(exactly = 1) { behavior.render(button, canvas) }
    }

    @Test @JsName("styleChangeEventsWork")
    fun `style change events work`() {
        validateStyleChanged(Button::iconAnchor,          mockk(relaxed = true))
        validateStyleChanged(Button::iconTextSpacing,     5.6                  )
        validateStyleChanged(Button::verticalAlignment,   mockk(relaxed = true))
        validateStyleChanged(Button::horizontalAlignment, mockk(relaxed = true))
    }

    @Test @JsName("delegatesContainsPointToBehavior")
    fun `delegates contains point to behavior`() {
        val button   = TestButton(model = mockk(relaxed = true)).apply { size = Size(100) }
        val behavior = mockk<Behavior<Button>>(relaxed = true)
        val point    = Point(4, 5)

        button.behavior = behavior

        point in button

        verify(exactly = 1) { behavior.contains(button, point) }
    }

    private fun <T> validateDefault(p: KProperty1<Button, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(object: Button() { override fun click() {} }) }
    }

    private fun validateChanged(property: KMutableProperty1<Button, Boolean>, changed: KProperty1<Button, PropertyObservers<Button, Boolean>>) {
        val view     = object: Button() { override fun click() {} }
        val old      = property.get(view)
        val observer = mockk<PropertyObserver<Button, Boolean>>(relaxed = true)

        changed.get(view).plusAssign(observer)

        property.set(view, !property.get(view))

        verify(exactly = 1) { observer(view, old, property.get(view)) }
    }

    private fun <T> validateSetter(p: KMutableProperty1<Button, T>, value: T) {
        TestButton(model = mockk(relaxed = true)).also {
            p.set(it, value)

            expect(value, "$p set to $value") { p.get(it) }
        }
    }

    private fun <T: Any?> validateStyleChanged(property: KMutableProperty1<Button, T>, value: T) {
        val button   = TestButton(model = mockk(relaxed = true))
        val observer = mockk<ChangeObserver<View>>(relaxed = true)

        button.styleChanged += observer

        property.set(button, value)

        verify(exactly = 1) { observer(button) }
    }
}