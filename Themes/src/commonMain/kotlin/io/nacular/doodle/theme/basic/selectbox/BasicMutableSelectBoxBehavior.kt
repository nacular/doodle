package io.nacular.doodle.theme.basic.selectbox

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.PopupManager
import io.nacular.doodle.controls.selectbox.MutableSelectBox
import io.nacular.doodle.controls.selectbox.MutableSelectBoxBehavior
import io.nacular.doodle.controls.selectbox.SelectBox
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.theme.basic.GenericTextEditOperation
import io.nacular.doodle.utils.Encoder

public class BasicMutableSelectBoxBehavior<T, M: MutableListModel<T>>(
    display            : Display,
    textMetrics        : TextMetrics,
    backgroundColor    : Color,
    darkBackgroundColor: Color,
    foregroundColor    : Color,
    cornerRadius       : Double,
    buttonWidth        : Double,
    focusManager       : FocusManager? = null,
    popupManager       : PopupManager? = null,
    buttonA11yLabel    : String?       = null,
    inset              : Double        = 4.0,
): MutableSelectBoxBehavior<T, M>(), PointerListener {

    private val delegate = BasicSelectBoxBehavior<T, M>(
        display,
        textMetrics,
        backgroundColor,
        darkBackgroundColor,
        foregroundColor,
        cornerRadius,
        buttonWidth,
        focusManager,
        popupManager,
        buttonA11yLabel,
    ).apply {
        buttonAlignment = {
            it.top    eq 0
            it.right  eq parent.right
            it.bottom eq parent.bottom
            it.width  eq buttonWidth + inset
        }
        centerChanged += { selectBox, old, new ->
            (selectBox as? MutableSelectBox<T, M>)?.let {
                selectBox.cancelEditing()
                setupEditingTriggers(old, new)
            }
        }
    }

    public var hoverColorMapper   : ColorMapper get() = delegate.hoverColorMapper;    set(new) { delegate.hoverColorMapper    = new }
    public var disabledColorMapper: ColorMapper get() = delegate.disabledColorMapper; set(new) { delegate.disabledColorMapper = new }

    override fun contains             (view: SelectBox<T, M>, point: Point): Boolean = delegate.contains(view, point)
    override fun mirrorWhenRightToLeft(view: SelectBox<T, M>              ): Boolean = delegate.mirrorWhenRightToLeft(view)
    override fun clipCanvasToBounds   (view: SelectBox<T, M>              ): Boolean = delegate.clipCanvasToBounds(view)
    override fun install              (view: SelectBox<T, M>              ) { delegate.install(view) }

    override fun uninstall(view: SelectBox<T, M>): Unit = delegate.uninstall(view)

    override fun render(view: SelectBox<T, M>, canvas: Canvas) { delegate.render(view, canvas) }

    override fun editingStarted(selectBox: MutableSelectBox<T, M>, value: T): EditOperation<T>? {
        val center = delegate.visualizedValue(selectBox)!!

        return selectBox.editor?.edit(selectBox, value, center)?.also { operation ->
            operation()?.let { delegate.updateCenter(selectBox, it) }
        }
    }

    override fun editingEnded(selectBox: MutableSelectBox<T, M>) {
        delegate.updateCenter(selectBox)
    }

    override fun changed(selectBox: SelectBox<T, M>): Unit = delegate.changed(selectBox)

    override fun pressed(event: PointerEvent) {
        event.source.let { selectBox ->
            if (event.clickCount >= 2 && selectBox is MutableSelectBox<*,*>) {
                selectBox.startEditing()
                event.consume()
            }
        }
    }

    private fun setupEditingTriggers(old: View?, new: View?) {
        old?.pointerFilter?.minusAssign(this)
        new?.pointerFilter?.plusAssign (this)
    }
}

public open class SelectBoxTextEditOperation<T>(
                focusManager: FocusManager?,
                mapper      : Encoder<T, String>,
    private val selectBox   : MutableSelectBox<T, *>,
                value       : T,
                current     : View): GenericTextEditOperation<T, MutableSelectBox<T, *>>(focusManager, mapper, selectBox, value, current) {

    private val changed = { _: SelectBox<T, *> ->
        selectBox.cancelEditing()
    }

    init {
        selectBox.changed += changed
    }

    override fun cancel() {
        selectBox.changed -= changed
    }
}