package io.nacular.doodle.theme.basic.spinbutton

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.spinbutton.MutableSpinButton
import io.nacular.doodle.controls.spinbutton.MutableSpinButtonBehavior
import io.nacular.doodle.controls.spinbutton.MutableSpinButtonModel
import io.nacular.doodle.controls.spinbutton.SpinButton
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

public class BasicMutableSpinButtonBehavior<T, M: MutableSpinButtonModel<T>>(
    textMetrics        : TextMetrics,
    backgroundColor    : Color,
    darkBackgroundColor: Color,
    foregroundColor    : Color,
    cornerRadius       : Double,
    buttonWidth        : Double,
    focusManager       : FocusManager? = null,
    incrementLabel     : String?       = null,
    decrementLabel     : String?       = null,
): MutableSpinButtonBehavior<T, M>(), PointerListener {

    private val delegate = BasicSpinButtonBehavior<T, M>(
        textMetrics,
        backgroundColor,
        darkBackgroundColor,
        foregroundColor,
        cornerRadius,
        buttonWidth,
        focusManager,
        incrementLabel,
        decrementLabel,
    ).apply {
        centerChanged += { spinButton, old, new ->
            (spinButton as? MutableSpinButton<T, M>)?.let {
                spinButton.cancelEditing()
                setupEditingTriggers(old, new)
            }
        }
    }

    public var hoverColorMapper   : ColorMapper get() = delegate.hoverColorMapper;    set(new) { delegate.hoverColorMapper    = new }
    public var disabledColorMapper: ColorMapper get() = delegate.disabledColorMapper; set(new) { delegate.disabledColorMapper = new }

    override fun contains             (view: SpinButton<T, M>, point: Point): Boolean = delegate.contains(view, point)
    override fun mirrorWhenRightToLeft(view: SpinButton<T, M>              ): Boolean = delegate.mirrorWhenRightToLeft(view)
    override fun clipCanvasToBounds   (view: SpinButton<T, M>              ): Boolean = delegate.clipCanvasToBounds(view)
    override fun install              (view: SpinButton<T, M>              ) { delegate.install(view) }

    override fun uninstall(view: SpinButton<T, M>): Unit = delegate.uninstall(view)

    override fun render(view: SpinButton<T, M>, canvas: Canvas) { delegate.render(view, canvas) }

    override fun editingStarted(spinButton: MutableSpinButton<T, M>, value: T): EditOperation<T>? {
        val center = delegate.visualizedValue(spinButton)

        return spinButton.editor?.edit(spinButton, value, center!!)?.also { operation ->
            operation()?.let { newCenter -> delegate.updateCenter(spinButton, oldCenter = center, newCenter) }
        }
    }

    override fun editingEnded(spinButton: MutableSpinButton<T, M>) {
        delegate.updateCenter(spinButton)
    }

    override fun changed(spinButton: SpinButton<T, M>): Unit = delegate.changed(spinButton)

    override fun pressed(event: PointerEvent) {
        event.source.let { spinButton ->
            if (event.clickCount >= 2 && spinButton is MutableSpinButton<*,*>) {
                spinButton.startEditing()
                event.consume()
            }
        }
    }

    private fun setupEditingTriggers(old: View?, new: View) {
        old?.pointerFilter?.minusAssign(this)
        new.pointerFilter += this
    }
}

public open class SpinButtonTextEditOperation<T>(
                focusManager: FocusManager?,
                mapper      : Encoder<T, String>,
    private val spinButton  : MutableSpinButton<T, *>,
                value       : T,
                current     : View
): GenericTextEditOperation<T, MutableSpinButton<T, *>>(focusManager, mapper, spinButton, value, current) {

    private val changed = { _: SpinButton<T, *> ->
        spinButton.cancelEditing()
    }

    init {
        spinButton.changed += changed
    }

    override fun cancel() {
        spinButton.changed -= changed
    }
}