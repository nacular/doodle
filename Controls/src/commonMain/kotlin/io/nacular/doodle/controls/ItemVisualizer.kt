package io.nacular.doodle.controls

import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.Dimension

/**
 * Provides a mapping between an item and a [View] to represent it.
 * @param T item type
 * @param C context about the item
 */
public interface ItemVisualizer<T, in C> {
    /**
     * Called whenever an item needs to be translated to a View.
     *
     * @param item being represented
     * @param previous View that represented an item
     * @param context providing more details about the item
     * @return a View to represent this item
     */
    public operator fun invoke(item: T, previous: View? = null, context: C): View
}

public operator fun <T> ItemVisualizer<T, Any>.invoke(item: T, previous: View? = null): View = invoke(item, previous, Unit)

/**
 * Creates an [ItemVisualizer] from the given lambda.
 *
 * @param block that will serve as the visualizer's [ItemVisualizer.invoke].
 */
public inline fun <T, C> itemVisualizer(crossinline block: (item: T, previous: View?, context: C) -> View): ItemVisualizer<T, C> = object: ItemVisualizer<T, C> {
    override fun invoke(item: T, previous: View?, context: C) = block(item, previous, context)
}

/**
 * Visualizes Strings using [Label]s.
 */
public open class StringVisualizer(private val fitText: Set<Dimension>? = null): ItemVisualizer<String, Any> {
    override fun invoke(item: String, previous: View?, context: Any): Label = when (previous) {
        is Label -> previous.apply { text = item; this@StringVisualizer.fitText?.let { fitText = it } }
        else     -> Label(StyledText(item)).apply {
            this@StringVisualizer.fitText?.let { fitText = it }
        }
    }
}

/**
 * Visualizes [StyledText] using [Label]s.
 */
public open class StyledTextVisualizer(private val fitText: Set<Dimension>? = null): ItemVisualizer<StyledText, Any> {
    override fun invoke(item: StyledText, previous: View?, context: Any): Label = when (previous) {
        is Label -> previous.apply { styledText = item; this@StyledTextVisualizer.fitText?.let { fitText = it } }
        else     -> Label(item).apply {
            this@StyledTextVisualizer.fitText?.let { fitText = it }
        }
    }
}

/**
 * Visualizes Booleans using [CheckBox]es.
 */
public open class BooleanVisualizer(private val defaultSize: Size = Size(16)): ItemVisualizer<Boolean, Any> {
    override fun invoke(item: Boolean, previous: View?, context: Any): CheckBox = when (previous) {
        is CheckBox -> previous.apply   { enabled = true;  selected = item; enabled = false; }
        else        -> CheckBox().apply { enabled = true;  selected = item; enabled = false; }
    }.apply { suggestSize(defaultSize) }
}

/**
 * Simply uses the item (which is a View) as its own visualizer
 */
public object ViewVisualizer: ItemVisualizer<View, Any> {
    override fun invoke(item: View, previous: View?, context: Any): View = item
}

/**
 * Places the item (which is a View) within a [ScrollPanel].
 */
public open class ScrollPanelVisualizer: ItemVisualizer<View, Any> {
    override fun invoke(item: View, previous: View?, context: Any): ScrollPanel = when (previous) {
        is ScrollPanel -> previous.also { it.content = item }
        else           -> ScrollPanel(item)
    }
}

/**
 * Visualizes a string representation of the item (obtained from [mapper]) using [delegate].
 *
 * @param delegate to visualize the item's string representation
 * @param mapper to convert the item to a string
 */
public fun <T, C> toString(delegate: ItemVisualizer<String, C>, mapper: (T) -> String = { "$it" }): ItemVisualizer<T, C> = object: ItemVisualizer<T, C> {
    override fun invoke(item: T, previous: View?, context: C) = delegate.invoke(mapper(item), previous, context)
}

/**
 * Creates a visualizer for [T] using a visualizer for [R] by mapping T -> R.
 *
 * @param mapper function
 */
public fun <T, R, C> ItemVisualizer<R, C>.after(mapper: (T) -> R): ItemVisualizer<T, C> = object: ItemVisualizer<T, C> {
    override fun invoke(item: T, previous: View?, context: C) = this@after(mapper(item), previous, context)
}

/**
 * A selectable item with an index that is often used as the context with [ItemVisualizer].
 * @property index of the item
 * @property selected is `true` for selected items
 */
public interface IndexedItem {
    public val index   : Int
    public val selected: Boolean
}

/**
 * An indexed item with an expanded state that is often used as the context with [ItemVisualizer].
 * @property expanded is `true` for expanded items
 */
public interface ExpandableItem: IndexedItem {
    public val expanded: Boolean
}

public open class SimpleIndexedItem(override val index: Int, override val selected: Boolean): IndexedItem