package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image


interface Canvas: Renderer {

    var size        : Size
    var transform   : AffineTransform
    var optimization: Renderer.Optimization

    fun import(imageData: ImageData, at: Point)

    fun scale    (pin   : Point)
    fun rotate   (angle : Double)
    fun rotate   (around: Point, angle: Double)
    fun translate(by    : Point)

    fun flipVertically()
    fun flipVertically(around: Double)

    fun flipHorizontally()
    fun flipHorizontally(around: Double)

    fun image(image: Image, destination: Rectangle, opacity: Float = 1f)

    interface ImageData
}
