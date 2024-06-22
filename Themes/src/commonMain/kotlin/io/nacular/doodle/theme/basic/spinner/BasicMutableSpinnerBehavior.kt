package io.nacular.doodle.theme.basic.spinner

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.spinner.MutableSpinButton
import io.nacular.doodle.controls.spinner.MutableSpinButtonBehavior
import io.nacular.doodle.controls.spinner.MutableSpinButtonModel
import io.nacular.doodle.controls.spinner.SpinButton
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

@Deprecated("Use BasicMutableSpinButtonBehavior", replaceWith = ReplaceWith("BasicMutableSpinButtonBehavior<T, M>"))
public typealias BasicMutableSpinnerBehavior<T, M> = BasicMutableSpinButtonBehavior<T, M>

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
        centerChanged += { spinner, old, new ->
            (spinner as? MutableSpinButton<T, M>)?.let {
                spinner.cancelEditing()
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

    override fun editingStarted(spinner: MutableSpinButton<T, M>, value: T): EditOperation<T>? {
        val center = delegate.visualizedValue(spinner)

        return spinner.editor?.edit(spinner, value, center!!)?.also { operation ->
            operation()?.let { newCenter -> delegate.updateCenter(spinner, oldCenter = center, newCenter) }
        }
    }

    override fun editingEnded(spinner: MutableSpinButton<T, M>) {
        delegate.updateCenter(spinner)
    }

    override fun changed(spinner: SpinButton<T, M>): Unit = delegate.changed(spinner)

    override fun pressed(event: PointerEvent) {
        event.source.let { spinner ->
            if (event.clickCount >= 2 && spinner is MutableSpinButton<*,*>) {
                spinner.startEditing()
                event.consume()
            }
        }
    }

    private fun setupEditingTriggers(old: View?, new: View) {
        old?.pointerFilter?.minusAssign(this)
        new.pointerFilter += this
    }
}

public open class SpinnerTextEditOperation<T>(
                focusManager: FocusManager?,
                mapper      : Encoder<T, String>,
    private val spinner     : MutableSpinButton<T, *>,
                value       : T,
                current     : View
): GenericTextEditOperation<T, MutableSpinButton<T, *>>(focusManager, mapper, spinner, value, current) {

    private val changed = { _: SpinButton<T, *> ->
        spinner.cancelEditing()
    }

    init {
        spinner.changed += changed
    }

    override fun cancel() {
        spinner.changed -= changed
    }
}