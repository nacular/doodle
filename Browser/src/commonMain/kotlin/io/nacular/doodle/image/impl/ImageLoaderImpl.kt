package io.nacular.doodle.image.impl

import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.dom.HTMLImageElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


internal class ImageLoaderImpl(private val htmlFactory: HtmlFactory, private val images: MutableMap<String, Image> = mutableMapOf()): ImageLoader {

    private val loading           = mutableMapOf<String, HTMLImageElement>()
    private val pendingCoroutines = mutableMapOf<String, MutableList<Continuation<Image?>>>()

    override suspend fun load(source: String): Image? = suspendCoroutine { coroutine ->
        when (source) {
            in images  -> coroutine.resume(images[source])
            in loading -> pendingCoroutines.getOrPut(source) { mutableListOf() }.plusAssign(coroutine)
            else       -> {
                loading[source] = htmlFactory.createImage(source).apply {
                    onload = {
                        loading        -= source
                        images[source]  = ImageImpl(this)
                        notify(source, coroutine)
                        removeListeners(this)
                    }

                    onerror = {
                        loading -= source
                        removeListeners(this)
                        notify(source, coroutine)
                    }
                }
            }
        }
    }

    private fun notify(source: String, current: Continuation<Image?>) {
        val image = images[source]

        current.resume(image)
        pendingCoroutines[source]?.forEach { it.resume(image) }
        pendingCoroutines.remove(source)
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