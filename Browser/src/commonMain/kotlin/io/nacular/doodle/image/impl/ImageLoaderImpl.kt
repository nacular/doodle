package io.nacular.doodle.image.impl

import io.nacular.doodle.HTMLImageElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.scheduler.Scheduler


public class ImageLoaderImpl(private val htmlFactory: HtmlFactory, private val scheduler: Scheduler, private val images: MutableMap<String, Image> = mutableMapOf()): ImageLoader {
    private val loading = mutableMapOf<String, HTMLImageElement>()

    override suspend fun load(source: String): Image? {
        images[source]?.let { return it }

        if (source !in loading) {
            val img = htmlFactory.createImage(source)

            loading[source] = img

            scheduler.delayUntil { img.complete }

            images[source] = ImageImpl(img)

            loading -= source
        }

        scheduler.delayUntil { source in images }

        return images[source]
    }

    override fun unload(image: Image) {
        images.remove(image.source)
    }
}