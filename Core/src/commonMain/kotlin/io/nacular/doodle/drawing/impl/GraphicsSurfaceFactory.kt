package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.GraphicsSurface

@Internal
public interface GraphicsSurfaceFactory<T: GraphicsSurface> {
    public operator fun invoke(): T
    public operator fun invoke(parent: T? = null, view: View, isContainer: Boolean = view.children_.isNotEmpty(), addToRootIfNoParent: Boolean = true): T
}