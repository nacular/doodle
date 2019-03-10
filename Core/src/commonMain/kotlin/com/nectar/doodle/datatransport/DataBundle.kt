package com.nectar.doodle.datatransport

/**
 * Created by Nicholas Eddy on 11/3/18.
 */
sealed class MimeType<T>(private val primary: String, private val secondary: String) {
    override fun toString() = "$primary/$secondary"
}

object PlainText: MimeType<String>("text",        "plain"   )
object UriList  : MimeType<String>("text",        "uri-list")
object Json     : MimeType<String>("application", "json"    )

interface DataBundle {
    operator fun <T: Any> invoke  (type: MimeType<T>): T?
    operator fun <T: Any> contains(type: MimeType<T>): Boolean

    operator fun plus(other: DataBundle): CompositeBundle = CompositeBundle(sequenceOf(this, other))
}

open class SingleItemBundle<Item>(private val type: MimeType<Item>, private val item: Item): DataBundle {
    override fun <T: Any> invoke  (type: MimeType<T>) = if (type in this) item as T else null
    override fun <T: Any> contains(type: MimeType<T>) = this.type == type
}

class TextBundle(text: String): SingleItemBundle<String>(PlainText, text)

class UriBundle(uri: String): SingleItemBundle<String>(UriList, uri)

class CompositeBundle(private var bundles: Sequence<DataBundle>): DataBundle {
    constructor(vararg bundles: DataBundle): this(sequenceOf(*bundles))

    override fun <T : Any> invoke  (type: MimeType<T>) = bundles.find { type in it }?.let { it(type) }
    override fun <T : Any> contains(type: MimeType<T>) = bundles.find { type in it }?.let { true } ?: false

    override operator fun plus(other: DataBundle): CompositeBundle = this.also {
        bundles += other
    }
}