package io.nacular.doodle.image.impl

import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import java.io.InputStream
import org.jetbrains.skija.Image as SkijaImage

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
public class ImageLoaderImpl: ImageLoader {
    private val loadedImages = mutableMapOf<String, Image>()

    override suspend fun load(source: String): Image? {
        loadedImages[source]?.let { return it }

        try {
            loadedImages[source] = when (val file: InputStream? = javaClass.getResourceAsStream(source)) {
                null -> {
                    ImageImpl(SkijaImage.makeFromEncoded(source.encodeToByteArray()), source)
                }
                else -> ImageImpl(SkijaImage.makeFromEncoded(file.readBytes()), source)
            }
        } catch (ignored: Throwable) { ignored.printStackTrace() }

        return loadedImages[source]
    }

    override suspend fun load(file: LocalFile): Image? {
        try {
            return ImageImpl(SkijaImage.makeFromEncoded(file.read()), file.name)
        } catch (ignored: Throwable) { ignored.printStackTrace() }

        return null
    }

    override fun unload(image: Image) {
        loadedImages.remove(image.source)
    }
}