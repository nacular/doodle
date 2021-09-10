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

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
public class ImageLoaderImpl(private val urlDecoder: UrlDecoder): ImageLoader {
    private val loadedImages   = mutableMapOf<String, Image>()
    private val dataImageRegex = Regex("^\\s*data:(?<mediaType>(?<mimeType>[a-z\\-+/]+/[a-z\\-+]+)(?<params>(;[a-z\\-]+=[a-z\\-]+)*))?;(?<encoding>[^,]*)?,(?<data>[a-zA-Z0-9!$&',()*+;=\\-._~:@/?%\\s]*\\s*)")

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
                            else -> data.let { ImageImpl(SkiaImage.makeFromEncoded(it.encodeToByteArray()), source) }
                        }
                    } ?:

                    ImageImpl(SkiaImage.makeFromEncoded(source.encodeToByteArray()), source)
                }
                else -> ImageImpl(SkiaImage.makeFromEncoded(file.readBytes()), source)
            }
        } catch (ignored: Throwable) { ignored.printStackTrace() }

        return loadedImages[source]
    }

    override suspend fun load(file: LocalFile): Image? {
        try {
            return ImageImpl(SkiaImage.makeFromEncoded(file.read()), file.name)
        } catch (ignored: Throwable) { ignored.printStackTrace() }

        return null
    }

    override fun unload(image: Image) {
        loadedImages.remove(image.source)
    }
}