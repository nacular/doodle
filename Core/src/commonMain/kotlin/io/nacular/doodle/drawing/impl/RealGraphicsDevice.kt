package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.GraphicsSurface

@Internal
public class RealGraphicsDevice(private val surfaceFactory: GraphicsSurfaceFactory<GraphicsSurface>): GraphicsDevice<GraphicsSurface> {

    private val viewSurfaceMap = mutableMapOf<View, GraphicsSurface>()
    private val surfaceViewMap = mutableMapOf<GraphicsSurface, View>()

    override operator fun get(view: View): GraphicsSurface = viewSurfaceMap.getOrPut(view) {
        val surface = surfaceFactory(view.parent?.let { this[it] }, view).apply { zOrder = view.zOrder }

        surfaceViewMap[surface] = view

        surface
    }

    override fun create(view: View): GraphicsSurface = viewSurfaceMap.getOrPut(view) {
        val surface = surfaceFactory(null, view, addToRootIfNoParent = false).apply { zOrder = view.zOrder }

        surfaceViewMap[surface] = view

        surface
    }

    override fun create(): GraphicsSurface = surfaceFactory().apply { index = 0 }

    override fun release(view: View) {
        viewSurfaceMap[view]?.let {
            it.release()
            surfaceViewMap.remove(it  )
            viewSurfaceMap.remove(view)
        }

        view.children_.forEach { release(it) }
    }

    override fun release(surface: GraphicsSurface) {
        surfaceViewMap[surface]?.let { release(it) }
    }
}