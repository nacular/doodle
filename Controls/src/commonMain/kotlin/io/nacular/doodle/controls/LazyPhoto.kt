package io.nacular.doodle.controls

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.image.Image
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.job

/**
 * A simple wrapper around a deferred [Image]. It renders a [placeHolder] while the image loads. Then the image is scaled to fit within the
 * bounds of the Photo when it finally loads.
 *
 * @param pendingImage to render when loaded
 * @param initialized notified when loaded
 * @param placeHolder used to render while the image loads
 */
@ExperimentalCoroutinesApi
public class LazyPhoto(pendingImage: Deferred<Image>, initialized: (imageSize: Size) -> Unit = {}, private val placeHolder: Canvas.() -> Unit = {}): View() {
    private lateinit var image: Image

    init {
        pendingImage.callOnCompleted {
            image = it

            if (size == Empty) {
                suggestSize(image.size)
            }
            initialized(image.size)
            rerender()
        }
    }

    override fun render(canvas: Canvas) {
        if (!::image.isInitialized) {
            placeHolder(canvas)
            return
        }

        canvas.image(image, destination = bounds.atOrigin)
    }

    private fun Deferred<Image>.callOnCompleted(block: (Image) -> Unit) {
        job.invokeOnCompletion {
            if (it == null) {
                block(this.getCompleted())
            }
        }
    }
}