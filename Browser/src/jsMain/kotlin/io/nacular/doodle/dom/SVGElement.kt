package io.nacular.doodle.dom

internal actual fun SVGGraphicsElement.getBBox(options: BoundingBoxOptions): DOMRect = this.getBBox(jsObject {
    fill    = options.fill
    stroke  = options.stroke
    markers = options.markers
    clipped = options.clipped
})