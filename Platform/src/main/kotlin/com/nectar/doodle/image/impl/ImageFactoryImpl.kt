package com.nectar.doodle.image.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image
import com.nectar.doodle.image.ImageFactory
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.units.milliseconds
import org.w3c.dom.HTMLImageElement


private class ImageMonitor(val image: HTMLImageElement, val observers: MutableSet<(Image) -> Unit>)

class ImageFactoryImpl(private val htmlFactory: HtmlFactory, private val scheduler: Scheduler): ImageFactory {

    private val images  = mutableMapOf<String, Image>()
    private val loading = mutableMapOf<String, ImageMonitor>()

    private var task: Task? = null

//    suspend override fun load(url: String) =  suspendCoroutine<Image> { continuation ->
//        val image = htmlFactory.createImage(url)
//
//        while (!image.complete);
//
//        continuation.resume(Image(Size(image.width.toDouble(), image.height.toDouble()), url))
//    }

    override fun load(url: String, result: (Image) -> Unit) {
        images[url]?.let { result(it); return  }

        loading.getOrPut(url) { ImageMonitor(htmlFactory.createImage(url), mutableSetOf()) }.observers += result

        if (task == null ) {
            task = scheduler.after(10.milliseconds, this::processLoading)
        }
    }

    private fun processLoading() {
        val iter = loading.iterator()

        while (iter.hasNext()) {
            iter.next().also { (url, monitor) ->
                if (monitor.image.complete) {
                    Image(Size(monitor.image.width.toDouble(), monitor.image.height.toDouble()), url).also { image ->
                        monitor.observers.forEach { it(image) }
                    }

                    iter.remove()
                }
            }
        }

        if (!loading.isEmpty()) {
            task = scheduler.after(10.milliseconds, this::processLoading)
        }
    }
}