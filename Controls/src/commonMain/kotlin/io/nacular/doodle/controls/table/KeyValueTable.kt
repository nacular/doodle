package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.View

/**
 * Map based model used with [KeyValueTable].
 */
public interface KeyValueModel<K, V>: ListModel<Pair<K, V>> {
    /**
     * Tells whether the model contains the given [key].
     *
     * @param key to check
     * @return `true` IFF the key is in the model's data
     */
    public fun containsKey(key: K): Boolean

    /**
     * Tells whether the model contains the given [value].
     *
     * @param value to check
     * @return `true` IFF the key is in the model's data
     */
    public fun containsValue(value: V): Boolean
}

/**
 * KeyValueModel that simply wraps a [Map]
 *
 * @param map containing data
 */
public class SimpleKeyValueModel<K, V>(private val map: Map<K, V>): KeyValueModel<K, V>, SimpleListModel<Pair<K, V>>(map.toList()) {
    public override fun containsKey  (key  : K): Boolean = map.containsKey  (key  )
    public override fun containsValue(value: V): Boolean = map.containsValue(value)
}

/**
 * Data describing a [KeyValueTable] column.
 *
 * @property header to show for the column
 * @property visualizer that renders the column's contents
 * @property footer to show for the column
 * @property builder to configure the column
 * @constructor creates a new info
 */
public data class ColumnInfo<K, V, T>(
    val header    : View? = null,
    val visualizer: CellVisualizer<Pair<K, V>, T>,
    val footer    : View? = null,
    val builder   : ColumnBuilder.() -> Unit
)

/**
 * A visual component that renders an immutable list of key ([K]) - value ([V]) pairs using a [TableBehavior]. Items are obtained via
 * the [model] and selection is managed via the optional [selectionModel]. Large ("infinite") lists are supported
 * efficiently, since Table recycles the Views generated to render its items.
 *
 * Note that this class assumes the given [SimpleKeyValueModel] is immutable and will not automatically respond
 * to changes in the model.
 *
 * KeyValueTable provides vertical scrolling internally, so it does not need to be embedded in a [ScrollPanel] or similar component,
 * unless horizontal scrolling is desired.
 *
 * @param model that holds the data for the table
 * @param keyColumn defines the key ([K]) column's properties
 * @param valueColumn defines the value ([V]) column's properties
 * @param selectionModel that manages the Table's selection state
 * @param scrollCache determining how many "hidden" items are rendered above and below the Table's view-port. A value of 0 means
 * only visible items are rendered, but quick scrolling is more likely to show blank areas.
 */
public class KeyValueTable<K, V>(
    model          : KeyValueModel<K, V>,
    keyColumn      : ColumnInfo<K, V, K>,
    valueColumn    : ColumnInfo<K, V, V>,
    selectionModel : SelectionModel<Int>? = null,
    scrollCache    : Int                  = 0,
): Table<Pair<K, V>, KeyValueModel<K, V>>(model, selectionModel, scrollCache, columns = {
    column(header = keyColumn.header,   footer = keyColumn.footer,   extractor = { first  }, cellVisualizer = keyColumn.visualizer,   builder = keyColumn.builder  )
    column(header = valueColumn.header, footer = valueColumn.footer, extractor = { second }, cellVisualizer = valueColumn.visualizer, builder = valueColumn.builder)
}) {
    init {
        columnSizePolicy = ProportionalSizePolicy
    }

    /**
     * Tells whether the table contains the given [key].
     *
     * @param key to check
     * @return `true` IFF the key is in the table's data
     */
    public fun containsKey(key: K): Boolean = model.containsKey(key)

    /**
     * Tells whether the table contains the given [value].
     *
     * @param value to check
     * @return `true` IFF the key is in the table's data
     */
    public fun containsValue(value: V): Boolean = model.containsValue(value)

    public companion object {
        /**
         * Creates a [KeyValueTable].
         *
         * @param values to include in the table
         * @param keyColumn defines the key ([K]) column's properties
         * @param valueColumn defines the value ([V]) column's properties
         * @param selectionModel that manages the Table's selection state
         * @param scrollCache determining how many "hidden" items are rendered above and below the table's view-port. A value of 0 means
         * only visible items are rendered, but quick scrolling is more likely to show blank areas.
         */
        public operator fun <K, V> invoke(
            values        : Map<K, V>,
            keyColumn     : ColumnInfo<K, V, K>,
            valueColumn   : ColumnInfo<K, V, V>,
            selectionModel: SelectionModel<Int>? = null,
            scrollCache   : Int                  = 0,): KeyValueTable<K, V> = KeyValueTable(SimpleKeyValueModel(values), keyColumn, valueColumn, selectionModel, scrollCache)
    }
}