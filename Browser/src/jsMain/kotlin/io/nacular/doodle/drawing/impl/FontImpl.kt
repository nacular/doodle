package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font

internal class FontImpl(override val size: Int, override val weight: Int, override val style: Font.Style, override val family: String): Font {
    override fun hashCode(): Int {
        var result = size
        result = 31 * result + weight
        result = 31 * result + style.hashCode ()
        result = 31 * result + family.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FontImpl) return false

        if (size   != other.size  ) return false
        if (weight != other.weight) return false
        if (style  != other.style ) return false
        if (family != other.family) return false

        return true
    }
}