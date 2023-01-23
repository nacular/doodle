package io.nacular.doodle.image.impl

import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import org.jetbrains.skia.Data
import org.jetbrains.skia.svg.SVGDOM
import java.io.InputStream
import org.jetbrains.skia.Image as SkiaImage

public interface UrlDecoder {
    public fun decode(string: String, encoding: String): String
}

public interface Base64Decoder {
    public fun decode(string: String): ByteArray
}

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
public class ImageLoaderImpl(private val urlDecoder: UrlDecoder, private val base64Decoder: Base64Decoder): ImageLoader {
    private val loadedImages   = mutableMapOf<String, Image>()
    private val dataImageRegex = Regex("^\\s*data:(?<mediaType>(?<mimeType>[a-z\\-+/]+/[a-z\\-+]+)(?<params>(;[a-z\\-]+=[a-z\\-]+)*))?;(?<encoding>[^,]*)?,(?<data>[a-zA-Z\\d!$&',()*+;=\\-._~:@/?%\\s]*\\s*)")

    override suspend fun load(source: String): Image? {
        loadedImages[source]?.let { return it }

        try {
            loadedImages[source] = when (val file: InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream(source)) {
                null -> {
                    dataImageRegex.find(source)?.let {
                        val encoding = it.groups["encoding"]?.value
                        val mimeType = it.groups["mimeType"]?.value
                        val data     = it.groups["data"    ]?.value

                        if (encoding.isNullOrBlank() || mimeType.isNullOrBlank() || data.isNullOrBlank()) {
                            return null
                        }

                        return when (mimeType) {
                            "image/svg+xml" -> {
                                SvgImage(SVGDOM(Data.makeFromBytes(urlDecoder.decode(data, encoding).toByteArray())))
                            }
                            else -> ImageImpl(SkiaImage.makeFromEncoded(base64Decoder.decode(data)), source)
                        }
                    } ?:

                    ImageImpl(SkiaImage.makeFromEncoded(base64Decoder.decode(source)), source)
                }
                else -> {
                    val bytes = file.readBytes()

                    runCatching {
                        ImageImpl(SkiaImage.makeFromEncoded(bytes), source)
                    }.getOrElse {
                        SvgImage(SVGDOM(Data.makeFromBytes(bytes)))
                    }
                }
            }
        } catch (ignored: Throwable) { ignored.printStackTrace() }

        return loadedImages[source]
    }

    override suspend fun load(file: LocalFile): Image? = file.read()?.let { data ->
        try {
            ImageImpl(SkiaImage.makeFromEncoded(data), file.name)
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
            null
        }
    }

    override fun unload(image: Image) {
        loadedImages.remove(image.source)
    }
}