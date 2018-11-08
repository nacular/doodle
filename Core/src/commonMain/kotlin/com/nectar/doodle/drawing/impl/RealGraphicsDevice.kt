package com.nectar.doodle.drawing.impl

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface


class RealGraphicsDevice(private val surfaceFactory: GraphicsSurfaceFactory<GraphicsSurface>): GraphicsDevice<GraphicsSurface> {

    private val viewSurfaceMap = mutableMapOf<View, GraphicsSurface>()
    private val surfaceViewMap = mutableMapOf<GraphicsSurface, View>()

    override operator fun get(view: View): GraphicsSurface {
        var surface: GraphicsSurface? = viewSurfaceMap[view]

        if (surface == null) {
            val parent = view.parent

            surface = surfaceFactory(viewSurfaceMap[parent], view.children_.isNotEmpty())

            surface.zIndex = parent?.zIndex_(view) ?: 0

            viewSurfaceMap[view  ] = surface
            surfaceViewMap[surface] = view
        }

        return surface
    }

    override fun create() = surfaceFactory().apply { zIndex = 0 }

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