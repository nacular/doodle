package io.nacular.doodle.controls

import io.nacular.doodle.accessibility.ImageRole
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.aspectRatio

/**
 * A simple wrapper around an [Image]. The image is scaled to fit within the
 * bounds of this Photo when drawn.
 */
public class Photo(private var image: Image): View(accessibilityRole = ImageRole()) {
    init {
        suggestSize(image.size)
    }

    /**
     * Ratio of Photo's [width]/[height].
     */
    public val aspectRatio: Double get() = image.aspectRatio

    override fun render(canvas: Canvas) {
        canvas.image(image, destination = bounds.atOrigin)
    }
}