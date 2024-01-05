package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/4/24.
 */
internal actual fun SVGGraphicsElement.getBBox(options: BoundingBoxOptions): DOMRect = this.getBBox(
    jsObject {
        fill = options.fill
        stroke = options.stroke
        markers = options.markers
        clipped = options.clipped
    }
)