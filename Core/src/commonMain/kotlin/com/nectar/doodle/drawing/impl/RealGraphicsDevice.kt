package com.nectar.doodle.drawing.impl

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface


class RealGraphicsDevice(private val surfaceFactory: GraphicsSurfaceFactory<GraphicsSurface>): GraphicsDevice<GraphicsSurface> {

    private val viewSurfaceMap = mutableMapOf<View, GraphicsSurface>()
    private val surfaceViewMap = mutableMapOf<GraphicsSurface, View>()

    override operator fun get(view: View): GraphicsSurface = viewSurfaceMap.getOrPut(view) {
        val surface = surfaceFactory(view.parent?.let { this[it] }, view).apply { zOrder = view.zOrder }

        surfaceViewMap[surface] = view

        surface
    }

    override fun create() = surfaceFactory().apply { index = 0 }

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