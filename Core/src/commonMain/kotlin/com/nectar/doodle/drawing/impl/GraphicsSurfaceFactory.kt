package com.nectar.doodle.drawing.impl

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.GraphicsSurface

interface GraphicsSurfaceFactory<T: GraphicsSurface> {
    operator fun invoke(): T
    operator fun invoke(parent: T? = null, view: View, isContainer: Boolean = view.children_.isNotEmpty(), addToRootIfNoParent: Boolean = true): T
}