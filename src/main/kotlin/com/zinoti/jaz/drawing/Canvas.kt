package com.zinoti.jaz.drawing

import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Size


interface Canvas: Renderer {

    var size     : Size
    var transform: AffineTransform
    val imageData: ImageData

    fun import(imageData: ImageData, position: Point)

    fun scale    (pin   : Point)
    fun rotate   (angle : Double)
    fun rotate   (around: Point, aAngle: Double)
    fun translate(by    : Point)

    fun flipVertically()
    fun flipVertically(around: Double)

    fun flipHorizontally()
    fun flipHorizontally(around: Double)

    interface ImageData
}
