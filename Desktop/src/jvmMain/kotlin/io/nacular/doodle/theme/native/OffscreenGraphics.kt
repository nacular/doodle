package io.nacular.doodle.theme.native

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.skia.toImage
import io.nacular.doodle.utils.observable
import org.jetbrains.skija.Image
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.ImageCapabilities
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.image.VolatileImage
import java.awt.image.VolatileImage.IMAGE_INCOMPATIBLE

/**
 * Created by Nicholas Eddy on 7/8/21.
 */
internal class OffscreenGraphics(private val graphicsConfiguration: GraphicsConfiguration, private val contentScale: Double) {
    private lateinit var volatileImage: VolatileImage

    var size by observable(Size.Empty) { _, new ->
        if (!new.empty && contentScale > 0.0) {
            volatileImage = createVolatileImage()

            do {
                if (volatileImage.validate(graphicsConfiguration) == IMAGE_INCOMPATIBLE) {
                    // old vImg doesn't work with new GraphicsConfig; re-create it
                    volatileImage = createVolatileImage()
                }
            } while (volatileImage.contentsLost())

            /*
            val colorModel = ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE)
            val raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, imageWidth, imageHeight, imageWidth * 4, 4, intArrayOf(0, 1, 2, 3), null) // ARGB order R, G, B, A order

            bufferedImage = BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied, null).also {
                graphics = it.createGraphics().apply {
                    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    scale(contentScale, contentScale)
                }
            }
             */
        }
    }

    fun render(block: (Graphics2D) -> Unit): Image {
        do {
            if (!this::volatileImage.isInitialized || volatileImage.validate(graphicsConfiguration) == IMAGE_INCOMPATIBLE) {
                // old image doesn't work with new GraphicsConfig; re-create it
                volatileImage = createVolatileImage()
            }

            volatileImage.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                scale(contentScale, contentScale)
            }.also(block).dispose()

        } while (volatileImage.contentsLost())

        return volatileImage.snapshot.toImage()
    }

    private fun createVolatileImage(): VolatileImage = graphicsConfiguration.createCompatibleVolatileImage(
            (size.width  * contentScale).toInt(),
            (size.height * contentScale).toInt(),
            ImageCapabilities(true),
            Transparency.TRANSLUCENT
    )
}