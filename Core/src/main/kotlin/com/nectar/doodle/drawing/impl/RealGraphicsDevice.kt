package com.nectar.doodle.drawing.impl

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface


class RealGraphicsDevice(private val surfaceFactory: GraphicsSurfaceFactory<GraphicsSurface>): GraphicsDevice<GraphicsSurface> {

    private val gizmoSurfaceMap = mutableMapOf<Gizmo, GraphicsSurface>()
    private val surfaceGizmoMap = mutableMapOf<GraphicsSurface, Gizmo>()

    override operator fun get(gizmo: Gizmo): GraphicsSurface {
        var surface: GraphicsSurface? = gizmoSurfaceMap[gizmo]

        if (surface == null) {
            val parent = gizmo.parent

            surface = surfaceFactory.surface(gizmoSurfaceMap[parent], !gizmo.children_.isEmpty())

            surface.zIndex = parent?.zIndex_(gizmo) ?: 0

            gizmoSurfaceMap[gizmo  ] = surface
            surfaceGizmoMap[surface] = gizmo
        }

        return surface
    }

    override fun create() = surfaceFactory.surface().apply { zIndex = 0 }

//    override fun create(element: HTMLElement) = surfaceFactory.surface(element)

    override fun release(gizmo: Gizmo) {
        gizmoSurfaceMap[gizmo]?.let {
            it.release()
            surfaceGizmoMap.remove(it   )
            gizmoSurfaceMap.remove(gizmo)
        }

        gizmo.children_.forEach { release(it) }
    }

    override fun release(surface: GraphicsSurface) {
        surfaceGizmoMap[surface]?.let { release(it) }
    }
}