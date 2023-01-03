package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.utils.fastMutableMapOf

/** @suppress */
@Internal
public class RealGraphicsDevice<T: GraphicsSurface>(private val surfaceFactory: GraphicsSurfaceFactory<T>): GraphicsDevice<T> {

    private val viewSurfaceMap = fastMutableMapOf<View, T>()
    private val surfaceViewMap = fastMutableMapOf<T, View>()

    override operator fun get(view: View): T = viewSurfaceMap.getOrPut(view) {
        val surface = surfaceFactory(view.parent?.let { this[it] }, view).apply { zOrder = view.zOrder }

        surfaceViewMap[surface] = view

        surface
    }

    override fun create(view: View): T = viewSurfaceMap.getOrPut(view) {
        val surface = surfaceFactory(null, view, addToRootIfNoParent = false).apply { zOrder = view.zOrder }

        surfaceViewMap[surface] = view

        surface
    }

    override operator fun contains(view: View): Boolean = view in viewSurfaceMap

    override fun create(): T = surfaceFactory().apply { index = 0 }

    override fun release(view: View) {
        viewSurfaceMap[view]?.let {
            it.release()
            surfaceViewMap.remove(it  )
            viewSurfaceMap.remove(view)
        }

        view.children_.forEach { release(it) }
    }

    override fun release(surface: T) {
        surfaceViewMap[surface]?.let { release(it) }
    }
}