package io.nacular.doodle.theme.basic.dropdown

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.dropdown.Dropdown
import io.nacular.doodle.controls.dropdown.MutableDropdown
import io.nacular.doodle.controls.dropdown.MutableDropdownBehavior
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.constant
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.theme.basic.GenericTextEditOperation
import io.nacular.doodle.theme.basic.dropdown.BasicDropdownBehavior.Companion.INSET
import io.nacular.doodle.utils.Encoder

public class BasicMutableDropdownBehavior<T, M: MutableListModel<T>>(
        display            : Display,
        textMetrics        : TextMetrics,
        backgroundColor    : Color,
        darkBackgroundColor: Color,
        foregroundColor    : Color,
        cornerRadius       : Double,
        buttonWidth        : Double,
        focusManager       : FocusManager? = null): MutableDropdownBehavior<T, M>(), PointerListener {

    private val delegate = BasicDropdownBehavior<T, M>(
            display,
            textMetrics,
            backgroundColor,
            darkBackgroundColor,
            foregroundColor,
            cornerRadius,
            buttonWidth,
            focusManager
    ).apply {
        buttonAlignment = {
            top    = parent.top
            right  = parent.right
            bottom = parent.bottom
            width  = constant(buttonWidth + INSET)
        }
        centerChanged += { dropdown, old, new ->
            (dropdown as? MutableDropdown<T, M>)?.let {
                dropdown.cancelEditing()
                setupEditingTriggers(old, new)
            }
        }
    }

    public var hoverColorMapper   : ColorMapper get() = delegate.hoverColorMapper;    set(new) { delegate.hoverColorMapper    = new }
    public var disabledColorMapper: ColorMapper get() = delegate.disabledColorMapper; set(new) { delegate.disabledColorMapper = new }

    override fun contains             (view: Dropdown<T, M>, point: Point): Boolean = delegate.contains(view, point)
    override fun mirrorWhenRightToLeft(view: Dropdown<T, M>              ): Boolean = delegate.mirrorWhenRightToLeft(view)
    override fun clipCanvasToBounds   (view: Dropdown<T, M>              ): Boolean = delegate.clipCanvasToBounds(view)
    override fun install              (view: Dropdown<T, M>              ) { delegate.install(view) }

    override fun uninstall(view: Dropdown<T, M>): Unit = delegate.uninstall(view)

    override fun render(view: Dropdown<T, M>, canvas: Canvas) { delegate.render(view, canvas) }

    override fun editingStarted(dropdown: MutableDropdown<T, M>, value: T): EditOperation<T>? {
        val center = delegate.visualizedValue(dropdown)!!

        return dropdown.editor?.edit(dropdown, value, center)?.also { operation ->
            operation()?.let { delegate.updateCenter(dropdown, it) }
        }
    }

    override fun editingEnded(dropdown: MutableDropdown<T, M>) {
        delegate.updateCenter(dropdown)
    }

    override fun changed(dropdown: Dropdown<T, M>): Unit = delegate.changed(dropdown)

    override fun pressed(event: PointerEvent) {
        event.source.let { dropdown ->
            if (event.clickCount >= 2 && dropdown is MutableDropdown<*,*>) {
                dropdown.startEditing()
                event.consume()
            }
        }
    }

    private fun setupEditingTriggers(old: View?, new: View?) {
        old?.pointerFilter?.minusAssign(this)
        new?.pointerFilter?.plusAssign (this)
    }
}

public open class DropdownTextEditOperation<T>(
                    focusManager: FocusManager?,
                    mapper      : Encoder<T, String>,
        private val dropdown    : MutableDropdown<T, *>,
                    value       : T,
                    current     : View): GenericTextEditOperation<T, MutableDropdown<T, *>>(focusManager, mapper, dropdown, value, current) {

    private val changed = { _: Dropdown<T, *> ->
        dropdown.cancelEditing()
    }

    init {
        dropdown.changed += changed
    }

    override fun cancel() {
        dropdown.changed -= changed
    }
}