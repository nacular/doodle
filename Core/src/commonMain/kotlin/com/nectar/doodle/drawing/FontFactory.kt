package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Font.Style
import com.nectar.doodle.drawing.Font.Weight
import com.nectar.doodle.drawing.Font.Weight.Normal


class FontInfo(
        var size  : Int        = -1,
        var style : Set<Style> = setOf(),
        var weight: Weight     = Normal,
        var family: String     = "")

private class FontImpl(
        override val size  : Int,
        override val weight: Weight,
        override val style : Set<Style>,
        override val family: String): Font {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FontImpl) return false

        if (size   != other.size  ) return false
        if (style  != other.style ) return false
        if (weight != other.weight) return false
        if (family != other.family) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + weight.hashCode()
        result = 31 * result + style.hashCode()
        result = 31 * result + family.hashCode()
        return result
    }
}

interface FontDetector {
    suspend operator fun invoke(            info: FontInfo.() -> Unit): Font
    suspend operator fun invoke(font: Font, info: FontInfo.() -> Unit) = invoke {
        size   = font.size
        style  = font.style
        weight = font.weight
        family = font.family
        apply(info)
    }
}
