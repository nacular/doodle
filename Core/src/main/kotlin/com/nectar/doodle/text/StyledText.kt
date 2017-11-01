package com.nectar.doodle.text

import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Font

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
data class Style(val font: Font? = null, val foreground: Color = black, val background: Color? = null)

class StyledText private constructor(private val data: List<Pair<String, Style>>): Iterable<Pair<String, Style>> {

    constructor(
        text      : String,
        font      : Font?  = null,
        foreground: Color  = black,
        background: Color? = null): this(listOf(Pair(text, Style(font, foreground = foreground, background = background))))

    val count = data.size

    override fun iterator() = data.iterator()

    operator fun plus(other: StyledText) = StyledText(data + other.data)

    operator fun rangeTo(font : Font  ): StyledText = StyledText(data + Pair("", Style(font)))
    operator fun rangeTo(color: Color ): StyledText = StyledText(data + Pair("", Style(foreground = color)))
    operator fun rangeTo(text : String): StyledText {
        val last = data.last()

        return StyledText(data - data.last() + Pair(text, last.second))
    }

    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StyledText) return false

        if (data != other.data) return false

        return true
    }
}

//class StyledTextTree {
//
//}

/*
    font {
        + "foo bar"
    }

*/



//fun styled()

operator fun Font.rangeTo(text : String ) = StyledText(text, this)
operator fun Font.rangeTo(color: Color  ) = StyledText("", this, foreground = color)
operator fun Color.rangeTo(text: String ) = StyledText(text, foreground = this)
operator fun Color.rangeTo(font: Font   ) = StyledText("", font = font, foreground = this)

operator fun String.rangeTo(font: Font  ) = StyledText(this) + StyledText("", font)
operator fun String.rangeTo(color: Color) = StyledText(this) + StyledText("", foreground = color)