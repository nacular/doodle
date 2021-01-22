package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 2/28/19.
 *
 * @author Nicholas Eddy
 */
public interface Renderable {
    public val size: Size

    public fun render(canvas: Canvas)
}