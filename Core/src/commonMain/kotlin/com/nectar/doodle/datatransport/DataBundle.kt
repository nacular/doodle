package com.nectar.doodle.datatransport

/**
 * Created by Nicholas Eddy on 11/3/18.
 */

/**
 * Defines a set of known mime-types.
 */
sealed class MimeType<T>(private val primary: String, private val secondary: String) {
    override fun toString() = "$primary/$secondary"
}

object PlainText: MimeType<String>("text",        "plain"   )
object UriList  : MimeType<String>("text",        "uri-list")
object Json     : MimeType<String>("application", "json"    )

/**
 * Represents a set of data keyed by [MimeType].  These bundles are used in drag-drop operations since they
 * allow arbitrary data to be encoded for data-transfer.
 */
interface DataBundle {
    /**
     * Read data for the given [MimeType]
     *
     * @return the associated data if any
     */
    operator fun <T: Any> invoke  (type: MimeType<T>): T?

    /**
     * Check whether data for the given [MimeType] is contained in this bundle
     *
     * @return true if data contained
     */
    operator fun <T: Any> contains(type: MimeType<T>): Boolean

    /**
     * Creates a [CompositeBundle] by joining this with the given bundle
     *
     * @param other bundle to combine with this
     * @return a new [CompositeBundle] with the data of both bundles
     */
    operator fun plus(other: DataBundle): CompositeBundle = CompositeBundle(sequenceOf(this, other))
}

/**
 * Simple bundle holding a single item.
 */
class SingleItemBundle<Item>(private val type: MimeType<Item>, private val item: Item): DataBundle {
    override fun <T: Any> invoke  (type: MimeType<T>) = if (type in this) item as T else null
    override fun <T: Any> contains(type: MimeType<T>) = this.type == type
}

/**
 * A bundle that combines several other bundles into one.
 */
class CompositeBundle(private var bundles: Sequence<DataBundle>): DataBundle {
    constructor(vararg bundles: DataBundle): this(sequenceOf(*bundles))

    override fun <T : Any> invoke  (type: MimeType<T>) = bundles.find { type in it }?.let { it(type) }
    override fun <T : Any> contains(type: MimeType<T>) = bundles.find { type in it }?.let { true } ?: false

    override operator fun plus(other: DataBundle) = CompositeBundle(bundles + other)
}
fun textBundle(text: String) = SingleItemBundle(PlainText, text)
fun uriBundle (uri : String) = SingleItemBundle(UriList,    uri)
