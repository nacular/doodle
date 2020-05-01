package com.nectar.doodle.text

import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.text.Fill.Background
import com.nectar.doodle.text.Fill.Foreground

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
interface Style {
    val font      : Font?
    val foreground: Brush?
    val background: Brush?
}

class StyledText private constructor(val data: MutableList<MutablePair<String, StyleImpl>>): Iterable<Pair<String, Style>> {
    constructor(
        text      : String,
        font      : Font?  = null,
        foreground: Brush? = null,
        background: Brush? = null): this(mutableListOf(MutablePair(text, StyleImpl(font, foreground = foreground, background = background))))

    data class MutablePair<A, B>(var first: A, var second: B) {
        override fun toString() = "($first, $second)"
    }

    val text  get() = data.joinToString { it.first }
    val count get() = data.size

    private var hashCode = data.hashCode()

    override fun iterator() = data.map { it.first to it.second }.iterator()

    operator fun plus(other: StyledText) = this.also { other.data.forEach { style -> add(style) } }

    operator fun rangeTo(font : Font  ) = this.also { add(MutablePair("",   StyleImpl(font))) }
    operator fun rangeTo(color: Color ) = this.also { add(MutablePair("",   StyleImpl(foreground = ColorBrush(color)))) }
    operator fun rangeTo(text : String) = this.also { add(MutablePair(text, StyleImpl())) }

    private fun add(pair: MutablePair<String, StyleImpl>) {
        val (_, style) = data.last()

        return when (style) {
            pair.second -> data.last().first += pair.first
            else        -> data.plusAssign(pair)
        }.also {
            hashCode = data.hashCode()
        }
    }

    override fun hashCode() = hashCode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StyledText) return false

        if (data != other.data) return false

        return true
    }

    operator fun Font.invoke(text: StyledText): StyledText {
        text.data.forEach { (_, style) ->
            if (style.font == null) {
                style.font = this
            }
        }

        return text
    }

    data class StyleImpl(override var font: Font? = null, override var foreground: Brush? = null, override var background: Brush? = null): Style
}

// TODO: Change to invoke(text: () -> String) when fixed (https://youtrack.jetbrains.com/issue/KT-22119)
operator fun Font.invoke(text: String          ) = StyledText(text = text, font = this)
operator fun Font.invoke(text: () -> StyledText) = text().apply {
    data.forEach { (_, style) ->
        if (style.font == null) {
            style.font = this@invoke
        }
    }
}

//operator fun Font.get(text: String    ) = StyledText(text = text, font = this)
//operator fun Font.get(text: StyledText) = text.apply {
//    data.forEach { (_, style) ->
//        if (style.font == null) {
//            style.font = this@get
//        }
//    }
//}


enum class Fill {
    Background,
    Foreground
}

// TODO: Change to invoke(text: () -> String) when fixed (https://youtrack.jetbrains.com/issue/KT-22119)
operator fun Color.invoke(text: String, fill: Fill = Foreground) = ColorBrush(this).let {
    StyledText(text = text, background = if (fill == Background) it else null, foreground = if (fill == Foreground) it else null)
}
operator fun Color.invoke(text: () -> StyledText) = text().apply {
    data.forEach { (_, style) ->
        if (style.foreground == null) {
            style.foreground = ColorBrush(this@invoke)
        }
    }
}

//operator fun Color.get(text: String, fill: Fill = Foreground) = ColorBrush(this).let {
//    StyledText(text = text, background = if (fill == Background) it else null, foreground = if (fill == Foreground) it else null)
//}
//operator fun Color.get(text: StyledText) = text.apply {
//    data.forEach { (_, style) ->
//        if (style.foreground == null) {
//            style.foreground = ColorBrush(this@get)
//        }
//    }
//}

operator fun String.rangeTo(styled: StyledText) = StyledText(this) + styled

// "foo" .. font {  } + color { }
