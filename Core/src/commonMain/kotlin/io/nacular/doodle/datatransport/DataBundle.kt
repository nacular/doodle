package io.nacular.doodle.datatransport

import io.nacular.measured.units.BinarySize
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import kotlin.reflect.KClass

/**
 * Created by Nicholas Eddy on 11/3/18.
 */
/**
 * Represents a file on the local machine that is being transferred
 * within a [DataBundle].
 * @property name of the file
 * @property size of the file
 * @property type mime-type of the file
 * @property isClosed indicates whether the file can be read
 * @property lastModified epoch time of the file
 */
interface LocalFile {
    val name        : String
    val size        : Measure<BinarySize>
    val type        : String
    val isClosed    : Boolean
    val lastModified: Measure<Time>

    /**
     * Reads the file contents as a [ByteArray].
     *
     * @param progress listener
     * @return the file contents or null if there was an error
     */
    suspend fun read(progress: (Float) -> Unit = {}): ByteArray?

    /**
     * Reads the file contents as a String.
     *
     * @param progress listener
     * @return the file contents or null if there was an error
     */
    suspend fun readText(encoding: String? = null, progress: (Float) -> Unit = {}): String?
}

/**
 * Represents a [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml).
 */
open class MimeType<T> internal constructor(private val primary: String, private val secondary: String, private val parameters: Map<String, String> = emptyMap()) {
    override fun toString() = "$primary/$secondary${if (parameters.isNotEmpty()) ";${parameters.entries.joinToString(";")}" else ""}"

    open fun assignableTo(other: MimeType<*>) = this.primary == other.primary && this.secondary == other.secondary

    override fun equals(other: Any?): Boolean {
        if (this === other       ) return true
        if (other !is MimeType<*>) return false

        if (primary    != other.primary   ) return false
        if (secondary  != other.secondary ) return false
        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = primary.hashCode()
        result = 31 * result + secondary.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }

    companion object {
        operator fun <T> invoke(primary   : String,
                                secondary : String,
                                parameters: Map<String, String> = emptyMap()
        ): MimeType<T> = MimeType(primary, secondary, parameters)
    }
}

/**
 * text [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
open class TextType internal constructor(type: String, charSet: String? = null): MimeType<String>("text", type, charSet?.let { mapOf("charset" to it) } ?: emptyMap())

/**
 * text/plain [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
object PlainText: TextType("plain") {
    operator fun invoke(charSet: String) = TextType("plain", charSet)
}

object UriList: TextType("uri-list")

/**
 * json [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
object Json: ApplicationType<String>("json")

/**
 * Internal mime-type to indicate a collection of files
 */
class Files(vararg val types: MimeType<*>): ApplicationType<List<LocalFile>>("files")

/**
 * Primary portion of the application [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
open class ApplicationType<T> internal constructor(type: String): MimeType<T>("application", type)

/**
 * Primary portion of the image [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
class Image(type: String): MimeType<LocalFile>("image", type)

class ReferenceType<T: Any>(private val type: KClass<out T>): ApplicationType<T>("reference<${type.simpleName}>") {
    override fun equals(other: Any?): Boolean {
        if (this === other            ) return true
        if (other !is ReferenceType<*>) return false
        if (!super.equals(other)      ) return false

        return type == other.type
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    companion object {
        inline operator fun <reified T: Any> invoke() = ReferenceType(T::class)
    }
}

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
    operator fun <T> invoke(type: MimeType<T>): T?

    /**
     * Check whether data for the given [MimeType] is contained in this bundle
     *
     * @return true if data contained
     */
    operator fun <T> contains(type: MimeType<T>): Boolean

    /**
     * Creates a [CompositeBundle] by joining this with the given bundle
     *
     * @param other bundle to combine with this
     * @return a new [CompositeBundle] with the data of both bundles
     */
    operator fun plus(other: DataBundle): CompositeBundle = CompositeBundle(sequenceOf(this, other))
}

inline operator fun <reified T: Any> DataBundle.invoke(): T? = this.invoke(ReferenceType(T::class))

inline fun <reified T: Any> DataBundle.contains(): Boolean = this.contains(ReferenceType(T::class))

/**
 * Simple bundle holding a single item.
 */
class SingleItemBundle<Item>(private val type: MimeType<Item>, private val item: Item): DataBundle {
    override fun <T> invoke  (type: MimeType<T>) = if (type in this) item as? T else null
    override fun <T> contains(type: MimeType<T>) = this.type.assignableTo(type)
}

/**
 * A bundle that combines several other bundles into one.
 */
class CompositeBundle(private var bundles: Sequence<DataBundle>): DataBundle {
    constructor(vararg bundles: DataBundle): this(sequenceOf(*bundles))

    override fun <T> invoke  (type: MimeType<T>) = bundles.find { type in it }?.let { it(type) }
    override fun <T> contains(type: MimeType<T>) = bundles.find { type in it }?.let { true } ?: false

    override operator fun plus(other: DataBundle) = CompositeBundle(bundles + other)
}

fun textBundle(text: String) = SingleItemBundle(PlainText, text)
fun uriBundle (uri : String) = SingleItemBundle(UriList,    uri)

inline fun <reified T: Any> refBundle(item: T) = SingleItemBundle(ReferenceType(), item)
