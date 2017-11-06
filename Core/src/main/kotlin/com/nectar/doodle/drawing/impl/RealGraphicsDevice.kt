package com.nectar.doodle.drawing.impl

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface


class RealGraphicsDevice(private val surfaceFactory: GraphicsSurfaceFactory<GraphicsSurface>): GraphicsDevice<GraphicsSurface> {

    private val gizmoSurfaceMap  = mutableMapOf<Gizmo, GraphicsSurface>()
    private val surfaceGizmoMap = mutableMapOf<GraphicsSurface, Gizmo>()

    override operator fun get(gizmo: Gizmo): GraphicsSurface {
        var surface: GraphicsSurface? = gizmoSurfaceMap.get(gizmo)

        if (surface == null) {
            val parent = gizmo.parent

            surface = surfaceFactory.surface(gizmoSurfaceMap[parent], !gizmo.children_.isEmpty())

            surface.zIndex = if (parent != null) parent.zIndex_(gizmo) else 0

            gizmoSurfaceMap.put(gizmo, surface)
            surfaceGizmoMap.put(surface, gizmo)
        }

        return surface
    }

    override fun create(): GraphicsSurface {
        val surface = surfaceFactory.surface()

        surface.zIndex = 0

        return surface
    }

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

//    private fun clearResources(surface: GraphicsSurface) {
//        for (aChild in surface) {
//            clearResources(aChild)
//        }
//
//        gizmoSurfaceMap.remove(surfaceGizmoMap[surface])
//        surfaceGizmoMap.remove(surface)
//    }
}