package com.nectar.doodle.drawing.impl

import com.nectar.doodle.drawing.GraphicsSurface

interface GraphicsSurfaceFactory<T: GraphicsSurface> {
//    fun surface(element: HTMLElement? = null): T

    fun surface(parent: T? = null, isContainer: Boolean = false): T
}