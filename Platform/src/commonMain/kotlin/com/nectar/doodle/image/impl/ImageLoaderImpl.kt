package com.nectar.doodle.image.impl

import com.nectar.doodle.HTMLImageElement
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.image.Image
import com.nectar.doodle.image.ImageLoader
import com.nectar.doodle.scheduler.Scheduler


class ImageLoaderImpl(private val htmlFactory: HtmlFactory, private val scheduler: Scheduler): ImageLoader {
    private val images  = mutableMapOf<String, Image>()
    private val loading = mutableMapOf<String, HTMLImageElement>()

    override suspend fun load(source: String): Image {
        images[source]?.let { return it }

        if (source !in loading) {
            val img = htmlFactory.createImage(source)

            loading[source] = img

            scheduler.delayUntil { img.complete }

            images[source] = ImageImpl(img)
        }

        scheduler.delayUntil { source in images }

        return images[source]!!
    }

    override fun unload(image: Image) {
        images.remove (image.source)
    }
}