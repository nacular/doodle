package io.nacular.doodle.layout.constraints

import io.nacular.doodle.layout.constraints.Strength.Companion.Required

/**
 * Created by Nicholas Eddy on 3/23/26.
 */
internal class Edit(val property: Variable, val value: Double, val strength: Strength = Required)