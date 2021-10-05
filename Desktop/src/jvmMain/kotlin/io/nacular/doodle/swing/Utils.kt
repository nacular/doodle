package io.nacular.doodle.swing

import io.nacular.doodle.geometry.Point
import org.jetbrains.skiko.SkiaWindow
import java.awt.event.MouseEvent

/**
 * Created by Nicholas Eddy on 10/4/21.
 */
internal fun MouseEvent.location(window: SkiaWindow): Point {
    val windowScreenLocation = window.layeredPane.locationOnScreen
    return locationOnScreen.run { Point(x - windowScreenLocation.x, y - windowScreenLocation.y) }
}