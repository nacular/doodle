package io.nacular.doodle.image.impl

import io.nacular.doodle.HTMLImageElement
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.scheduler.Scheduler
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


public class ImageLoaderImpl(private val htmlFactory: HtmlFactory, private val scheduler: Scheduler, private val images: MutableMap<String, Image> = mutableMapOf()): ImageLoader {

    private val loading = mutableMapOf<String, HTMLImageElement>()

    override suspend fun load(source: String): Image? = suspendCoroutine { coroutine ->
        when (source) {
            in images   -> coroutine.resume(images[source])
            !in loading -> {
                val img = htmlFactory.createImage(source)

                loading[source] = img

                img.onload = {
                    loading        -= source
                    images[source]  = ImageImpl(img)
                    removeListeners(img)
                    coroutine.resume(images[source])
                }

                img.onerror = { _,_,_,_,_ ->
                    loading -= source
                    removeListeners(img)
                    coroutine.resume(null)
                }
            }
            else        -> coroutine.resume(images[source])
        }
    }

    override suspend fun load(file: LocalFile): Image? = file.readBase64()?.let { load("data:*/*;base64,$it")  }

    override fun unload(image: Image) {
        images.remove(image.source)
    }

    private fun removeListeners(image: HTMLImageElement) {
        image.onload  = null
        image.onerror = null
    }
}