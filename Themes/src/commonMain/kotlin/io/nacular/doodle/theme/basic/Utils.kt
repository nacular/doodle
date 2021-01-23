package io.nacular.doodle.theme.basic

import io.nacular.doodle.drawing.Color
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.ParentConstraints

/**
 * Created by Nicholas Eddy on 6/23/20.
 */
public class ConstraintWrapper(delegate: Constraints, parent: (ParentConstraints) -> ParentConstraints): Constraints by delegate {
    override val parent: ParentConstraints = parent(delegate.parent)
}

public open class ParentConstraintWrapper(delegate: ParentConstraints): ParentConstraints by delegate

public typealias ColorMapper = (Color) -> Color