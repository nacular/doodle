package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Font.Layout
import com.nectar.doodle.drawing.Font.Layout.BOTTOM_TOP
import com.nectar.doodle.drawing.Font.Layout.LEFT_RIGHT
import com.nectar.doodle.drawing.Font.Layout.TOP_BOTTOM
import com.nectar.doodle.drawing.Font.Style


class FontInfo(
        var size    : Int = -1,
        var style   : Set<Style> = setOf(),
        var layout  : Layout = LEFT_RIGHT,
        var families: List<String> = listOf(),
        var rotated : Boolean = false)

interface FontFactory {
    fun create(builder: FontInfo.() -> Unit): Font

    fun family(vararg families: String): String
}

class FontFactoryImpl: FontFactory {
    override fun create(builder: FontInfo.() -> Unit) = FontInfo().apply(builder).run {
        val family = families.map { "\"$it\"" }.joinToString(",")

        fonts.getOrPut(getHash(family, size, style, layout, rotated)) {
            FontImpl(size, style, family, layout, rotated)
        }
    }

    override fun family(vararg families: String) = families.map { "\"$it\"" } .joinToString(",")

    private val fonts = HashMap<Int, Font>()

    private fun getHash(family: String, size: Int, style: Set<Style>, layout: Layout, rotated: Boolean) = arrayOf(family, size, style, layout, rotated).contentHashCode()
}

private class FontImpl(
        override val size     : Int,
        override val style    : Set<Style>,
        override val family   : String,
        override val layout   : Layout,
        override val isRotated: Boolean): Font {

    override val isVertical = layout == TOP_BOTTOM || layout == BOTTOM_TOP
}