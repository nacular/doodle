package io.nacular.doodle.datatransport

import io.nacular.measured.units.BinarySize
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import kotlin.reflect.KClass

/**
 * Represents a file on the local machine that is being transferred
 * within a [DataBundle].
 * @property name of the file
 * @property size of the file
 * @property type mime-type of the file
 * @property isClosed indicates whether the file can be read
 * @property lastModified epoch time of the file
 */
public interface LocalFile {
    public val name        : String
    public val size        : Measure<BinarySize>
    public val type        : String
    public val isClosed    : Boolean
    public val lastModified: Measure<Time>

    /**
     * Reads the file contents as a [ByteArray].
     *
     * @param progress listener
     * @return the file contents or null if there was an error
     */
    public suspend fun read(progress: (Float) -> Unit = {}): ByteArray?

    /**
     * Reads the file contents as a String.
     *
     * @param encoding of the resulting string
     * @param progress listener
     * @return the file contents or null if there was an error
     */
    public suspend fun readText(encoding: String? = null, progress: (Float) -> Unit = {}): String?

    /**
     * Reads the file contents as a base64 encoded String.
     *
     * @param progress listener
     * @return the file contents or null if there was an error
     */
    public suspend fun readBase64(progress: (Float) -> Unit = {}): String?
}

/**
 * Represents a [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml).
 */
public open class MimeType<T> internal constructor(private val primary: String, private val secondary: String, private val parameters: Map<String, String> = emptyMap()) {
    override fun toString(): String = "$primary/$secondary${if (parameters.isNotEmpty()) ";${parameters.entries.joinToString(";")}" else ""}"

    public open infix fun assignableTo(other: MimeType<*>): Boolean = this.primary == other.primary && this.secondary == other.secondary

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

    public companion object {
        public operator fun <T> invoke(
                primary   : String,
                secondary : String,
                parameters: Map<String, String> = emptyMap()
        ): MimeType<T> = MimeType(primary, secondary, parameters)
    }
}

/**
 * text [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
public open class TextType(type: String, charSet: String? = null): MimeType<String>("text", type, charSet?.let { mapOf("charset" to it) } ?: emptyMap())

/**
 * text/plain [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
public object PlainText: TextType("plain") {
    public operator fun invoke(charSet: String): TextType = TextType("plain", charSet)
}

public object UriList: TextType("uri-list")

/**
 * json [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
public object Json: ApplicationType<String>("json")

/**
 * Internal mime-type to indicate a collection of files
 */
public class Files(public vararg val types: MimeType<*>): ApplicationType<List<LocalFile>>("files")

/**
 * Primary portion of the application [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
public open class ApplicationType<T> internal constructor(type: String): MimeType<T>("application", type)

/**
 * Primary portion of the image [MIME type](https://www.iana.org/assignments/media-types/media-types.xhtml)
 */
public class Image(type: String): MimeType<LocalFile>("image", type)

/**
 * Runtime mime-type to represent a reference to some value of type [type].
 */
public class ReferenceType<T: Any>(private val type: KClass<out T>): ApplicationType<T>("doodle-reference-${type.simpleName}") {
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

    public companion object {
        public inline operator fun <reified T: Any> invoke(): ReferenceType<T> = ReferenceType(T::class)
    }
}

/**
 * Represents a set of data keyed by [MimeType].  These bundles are used in drag-drop operations since they
 * allow arbitrary data to be encoded for data-transfer.
 */
public interface DataBundle {
    /**
     * Read data for the given [MimeType]
     *
     * @return the associated data if any
     */
    public operator fun <T> get(type: MimeType<T>): T?

    /**
     * Check whether data for the given [MimeType] is contained in this bundle
     *
     * @return true if data contained
     */
    public operator fun <T> contains(type: MimeType<T>): Boolean

    /**
     * List of [MimeType]s contained in the bundle
     */
    public val includedTypes: List<MimeType<*>>

    /**
     * Creates a [CompositeBundle] by joining this with the given bundle
     *
     * @param other bundle to combine with this
     * @return a new [CompositeBundle] with the data of both bundles
     */
    public operator fun plus(other: DataBundle): CompositeBundle = CompositeBundle(sequenceOf(this, other))
}

public inline operator fun <reified T: Any> DataBundle.invoke(): T? = this[ReferenceType(T::class)]

public inline fun <reified T: Any> DataBundle.contains(): Boolean = ReferenceType(T::class) in this

/**
 * Simple bundle holding a single item.
 */
public class SingleItemBundle<Item>(private val type: MimeType<Item>, private val item: Item): DataBundle {
    @Suppress("UNCHECKED_CAST")
    override fun <T> get     (type: MimeType<T>): T? = if (type in this) item as? T else null
    override fun <T> contains(type: MimeType<T>): Boolean = type assignableTo this.type
    override val includedTypes: List<MimeType<*>> by lazy { listOf(type) }
}

/**
 * A bundle that combines several other bundles into one.
 */
public class CompositeBundle(private var bundles: Sequence<DataBundle>): DataBundle {
    public constructor(vararg bundles: DataBundle): this(sequenceOf(*bundles))

    override fun <T> get     (type: MimeType<T>): T?      = bundles.find { type in it }?.let { it[type] }
    override fun <T> contains(type: MimeType<T>): Boolean = bundles.find { type in it }?.let { true } ?: false

    override val includedTypes: List<MimeType<*>> by lazy { bundles.flatMap { it.includedTypes }.toList() }

    override operator fun plus(other: DataBundle): CompositeBundle = CompositeBundle(bundles + other)
}

public fun textBundle(text: String): SingleItemBundle<String> = SingleItemBundle(PlainText, text)
public fun uriBundle (uri : String): SingleItemBundle<String> = SingleItemBundle(UriList,    uri)

public inline fun <reified T: Any> refBundle(item: T): SingleItemBundle<T> = SingleItemBundle(ReferenceType(), item)
