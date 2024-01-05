package io.nacular.doodle.dom

internal actual interface SVGBoundingBoxOptions: JsAny {
    actual var fill   : Boolean?
    actual var stroke : Boolean?
    actual var markers: Boolean?
    actual var clipped: Boolean?
}