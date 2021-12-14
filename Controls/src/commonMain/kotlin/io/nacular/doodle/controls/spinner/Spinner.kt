package io.nacular.doodle.controls.spinner

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.properties.Delegates


/**
 * An iterator-like model that tracks the items a [Spinner] can present.
 */
public interface Model<T> {
    public fun next    ()
    public fun previous()

    public val value      : T
    public val hasNext    : Boolean
    public val hasPrevious: Boolean
    public val changed    : ChangeObservers<Model<T>>
}

/**
 * Provides presentation and behavior customization for [Spinner].
 */
public abstract class SpinnerBehavior<T, M: Model<T>>: Behavior<Spinner<T, M>> {
    public val Spinner<T, M>.children: ObservableList<View> get() = this._children
    public var Spinner<T, M>.insets  : Insets               get() = this._insets; set(new) { _insets = new }
    public var Spinner<T, M>.layout  : Layout?              get() = this._layout; set(new) { _layout = new }

    /**
     * Called whenever the Spinner's selection changes. This is an explicit API to ensure that
     * behaviors receive the notification before listeners to [Spinner.changed].
     *
     * @param spinner with change
     */
    public abstract fun changed(spinner: Spinner<T, M>)

    /**
     * Called whenever [Spinner.cellAlignment] changes.
     *
     * @param spinner with change
     */
    public fun alignmentChanged(spinner: Spinner<T, M>) {}
}

/**
 * Controls used to flip through items (one at a time) within a sequential collection.
 */
@Suppress("PropertyName")
public open class Spinner<T, M: Model<T>>(public val model: M, itemVisualizer: ItemVisualizer<T, Spinner<T, M>>? = null): View() {

    public fun next    (): Unit = model.next    ()
    public fun previous(): Unit = model.previous()

    public open val value      : Result<T> get() = runCatching { model.value }
    public      val hasPrevious: Boolean   get() = model.hasPrevious
    public      val hasNext    : Boolean   get() = model.hasNext

    public var behavior: SpinnerBehavior<T, M>? by behavior()

    public var itemVisualizer: ItemVisualizer<T, Spinner<T, M>>? = itemVisualizer
        protected set

    /**
     * Defines how the contents within the spinner should be aligned.
     */
    public var cellAlignment: (Constraints.() -> Unit)? by Delegates.observable(null) { _,_,_ ->
        behavior?.alignmentChanged(this)
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    // Expose container APIs for behavior
    internal val _children get() = children
    internal var _insets   get() = insets; set(new) { insets = new }
    internal var _layout   get() = layout; set(new) { layout = new }

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { ChangeObserversImpl(this) }

    public val changed: ChangeObservers<Spinner<T, M>> = changed_

    private val modelChanged: (Model<T>) -> Unit = {
        changed_()
    }

    init {
        this.model.changed += modelChanged
    }

    public companion object {
        public operator fun     invoke(progression: IntProgression, itemVisualizer: ItemVisualizer<Int, Any>? = null): Spinner<Int, IntModel>            = Spinner(IntModel (progression), itemVisualizer)
        public operator fun <T> invoke(values: List<T>,             itemVisualizer: ItemVisualizer<T,   Any>? = null): Spinner<T, ListModel<T, List<T>>> = Spinner(ListModel(values     ), itemVisualizer)
    }
}