package com.zinoti.jaz.drawing

import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.geometry.Size
import com.zinoti.jaz.image.Image


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
