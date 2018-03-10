package com.nectar.doodle.image.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.image.Image
import com.nectar.doodle.image.ImageFactory
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.units.milliseconds
import org.w3c.dom.HTMLImageElement


private class ImageMonitor(val image: HTMLImageElement, val completed: MutableSet<(Image) -> Unit> = mutableSetOf(), val error: MutableSet<(Throwable) -> Unit> = mutableSetOf())

class ImageFactoryImpl(private val htmlFactory: HtmlFactory, private val scheduler: Scheduler): ImageFactory {

    private val images  = mutableMapOf<String, Image>()
    private val loading = mutableMapOf<String, ImageMonitor>()

    private var task: Task? = null

//    suspend override fun load(source: String) =  suspendCoroutine<Image> { continuation ->
//        val image = htmlFactory.createImage(source)
//
//        while (!image.complete);
//
//        continuation.resume(Image(Size(image.width.toDouble(), image.height.toDouble()), source))
//    }

    override fun load(source: String, completed: (Image) -> Unit, error: (Throwable) -> Unit) {
        images[source]?.let { completed(it); return  }

        loading.getOrPut(source) { ImageMonitor(htmlFactory.createImage(source)) }.also {
            it.completed += completed
            it.error     += error
        }

        if (task == null ) {
            scheduleLoadCheck()
        }
    }

    override fun unload(image: Image) {
        images.remove (image.source)
        loading.remove(image.source)

        task?.let {
            if (loading.isEmpty()) {
                it.cancel()
                task = null
            }
        }
    }

    private fun processLoading() {
        val iterator = loading.iterator()

        while (iterator.hasNext()) {
            iterator.next().also { (_, monitor) ->
                if (monitor.image.complete) {
                    ImageImpl(monitor.image).also { image ->
                        monitor.completed.forEach { it(image) }
                    }

                    iterator.remove()
                }
            }
        }

        if (!loading.isEmpty()) {
            scheduleLoadCheck()
        } else {
            task = null
        }
    }

    private fun scheduleLoadCheck() {
        task = scheduler.after(10.milliseconds, ::processLoading)
    }
}