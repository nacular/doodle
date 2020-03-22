package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Font.Style
import com.nectar.doodle.drawing.Font.Weight
import com.nectar.doodle.drawing.Font.Weight.Normal


class FontInfo(
        var size  : Int        = -1,
        var style : Set<Style> = setOf(),
        var weight: Weight     = Normal,
        var family: String     = "")

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
