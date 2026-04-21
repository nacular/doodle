package io.nacular.doodle.layout.constraints

import io.nacular.doodle.layout.constraints.Strength.Companion.Required

/**
 * Created by Nicholas Eddy on 3/23/26.
 */
public class Edit internal constructor(internal val property: Variable, internal val value: Double, internal var strength: Strength = Required) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Edit) return false

        if (value != other.value) return false
        if (property != other.property) return false
        if (strength != other.strength) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + property.hashCode()
        result = 31 * result + strength.hashCode()
        return result
    }
}