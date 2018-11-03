package com.nectar.doodle.drawing.impl

import com.nectar.doodle.drawing.GraphicsSurface

interface GraphicsSurfaceFactory<T: GraphicsSurface> {
    operator fun invoke(parent: T? = null, isContainer: Boolean = false): T
}