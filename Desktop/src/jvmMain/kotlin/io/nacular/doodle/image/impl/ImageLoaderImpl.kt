package io.nacular.doodle.image.impl

import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
public class ImageLoaderImpl: ImageLoader {
    private val loadedImages = mutableMapOf<String, Image>()

    override suspend fun load(source: String): Image? {
        loadedImages[source]?.let { return it }

        try {
            val file = javaClass.getResourceAsStream(source)

            loadedImages[source] = ImageImpl(org.jetbrains.skija.Image.makeFromEncoded(file.readBytes()), source)
        } catch (ignored: Throwable) {}

        return loadedImages[source]
    }

    override suspend fun load(file: LocalFile): Image? {
        TODO("Not yet implemented")
    }

    override fun unload(image: Image) {
        // FIXME: Implement
    }
}