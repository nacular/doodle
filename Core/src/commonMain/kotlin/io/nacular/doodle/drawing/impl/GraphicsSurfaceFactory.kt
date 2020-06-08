package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.GraphicsSurface

interface GraphicsSurfaceFactory<T: GraphicsSurface> {
    operator fun invoke(): T
    operator fun invoke(parent: T? = null, view: View, isContainer: Boolean = view.children_.isNotEmpty(), addToRootIfNoParent: Boolean = true): T
}