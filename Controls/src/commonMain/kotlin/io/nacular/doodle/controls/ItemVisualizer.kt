package io.nacular.doodle.controls

import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextFit
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.text.StyledText
import kotlin.math.max

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

public inline fun <T, C> itemVisualizer(crossinline block: (item: T, previous: View?, context: C) -> View): ItemVisualizer<T, C> = object: ItemVisualizer<T, C> {
    override fun invoke(item: T, previous: View?, context: C) = block(item, previous, context)
}

/**
 * Visualizes Strings using [Label]s.
 */
public open class TextVisualizer(private val fitText: Set<TextFit>? = null): ItemVisualizer<String, Any> {
    override fun invoke(item: String, previous: View?, context: Any): Label = when (previous) {
        is Label -> previous.apply { text = item; this@TextVisualizer.fitText?.let { fitText = it } }
        else     -> Label(StyledText(item)).apply {
            this@TextVisualizer.fitText?.let { fitText = it }
        }
    }
}

/**
 * Visualizes Booleans using [CheckBox]es.
 */
public open class BooleanVisualizer(private val defaultSize: Size = Size(16)): ItemVisualizer<Boolean, Any> {
    override fun invoke(item: Boolean, previous: View?, context: Any): CheckBox = when (previous) {
        is CheckBox -> previous.apply   { enabled = true;  selected = item; enabled = false; }
        else        -> CheckBox().apply { enabled = false; selected = item                   }
    }.apply { size = idealSize ?: Size(max(minimumSize.width, defaultSize.width), max(minimumSize.height, defaultSize.height)) }
}

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
 * Visualizes the item's `toString()` using the delegate.
 *
 * @param delegate to visualize the item's `toString()`
 */
public fun <T, C> toString(delegate: ItemVisualizer<String, C>): ItemVisualizer<T, C> = object: ItemVisualizer<T, C> {
    override fun invoke(item: T, previous: View?, context: C) = delegate.invoke(item.toString(), previous, context)
}

/**
 * A selectable item with an index that is often used as the context with [ItemVisualizer].
 * @property index of the item
 * @property selected is `true` for selected items
 */
public interface IndexedIem {
    public val index: Int
    public val selected: Boolean
}

public class SimpleIndexedItem(override val index: Int, override val selected: Boolean): IndexedIem