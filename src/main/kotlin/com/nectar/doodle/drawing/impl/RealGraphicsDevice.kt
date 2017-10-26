package com.nectar.doodle.drawing.impl


import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.GraphicsDevice
import org.w3c.dom.HTMLElement


class RealGraphicsDevice(private val surfaceFactory: GraphicsSurfaceFactory<RealGraphicsSurface>): GraphicsDevice<RealGraphicsSurface> {

    private val gizmoSurfaceMap  = mutableMapOf<Gizmo, RealGraphicsSurface>()
    private val mSurfaceGizmoMap = mutableMapOf<RealGraphicsSurface, Gizmo>()

    override operator fun get(gizmo: Gizmo): RealGraphicsSurface {
        var surface: RealGraphicsSurface? = gizmoSurfaceMap.get(gizmo)

        if (surface == null) {
            val parent = gizmo.parent

            surface = surfaceFactory.surface(gizmoSurfaceMap[parent], !gizmo.children_.isEmpty())

            surface.zIndex = if (parent != null) parent.zIndex_(gizmo) else 0

            gizmoSurfaceMap.put(gizmo, surface)
            mSurfaceGizmoMap.put(surface, gizmo)
        }

        return surface
    }

    override fun create(): RealGraphicsSurface {
        val surface = surfaceFactory.surface()

        surface.zIndex = 0

        return surface
    }

    override fun create(element: HTMLElement) = surfaceFactory.surface(element)

    override fun release(gizmo: Gizmo) {
        gizmoSurfaceMap[gizmo]?.let {
            release(it)
        }
    }

    override fun release(surface: RealGraphicsSurface) {
        clearResources(surface)

        surface.release()
    }

    private fun clearResources(surface: RealGraphicsSurface) {
        for (aChild in surface) {
            clearResources(aChild)
        }

        gizmoSurfaceMap.remove(mSurfaceGizmoMap[surface])
        mSurfaceGizmoMap.remove(surface)
    }
}
