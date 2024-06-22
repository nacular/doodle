package io.nacular.doodle.text

import io.nacular.doodle.core.Internal
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.text.Target.Background
import io.nacular.doodle.text.Target.Foreground
import io.nacular.doodle.text.TextDecoration.Line.Through
import io.nacular.doodle.text.TextDecoration.Line.Under
import io.nacular.doodle.text.TextDecoration.Style.Solid
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 10/31/17.
 */
public class TextDecoration(
        public val lines    : Set<Line>  = emptySet(),
        public val color    : Color?     = null,
        public val style    : Style      = Solid,
        public val thickness: Thickness? = null
) {
    public enum class Line  { Under, Over, Through }
    public enum class Style { Solid, Double, Dotted, Dashed, Wavy }

    public sealed class Thickness {
        public object FromFont: Thickness()
        public class Absolute(public val value: Double): Thickness()
        public class Percent (public val value: Float ): Thickness()
    }

    public companion object {
        public val UnderLine  : TextDecoration = TextDecoration(lines = setOf(Under  ))
        public val LineThrough: TextDecoration = TextDecoration(lines = setOf(Through))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextDecoration) return false

        if (lines     != other.lines    ) return false
        if (color     != other.color    ) return false
        if (style     != other.style    ) return false
        if (thickness != other.thickness) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lines.hashCode()
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + style.hashCode()
        result = 31 * result + (thickness?.hashCode() ?: 0)
        return result
    }
}

public interface Style {
    public val font      : Font?
    public val foreground: Paint?
    public val background: Paint?
    public val decoration: TextDecoration?
}

public class StyledText private constructor(internal val data: MutableList<MutablePair<String, StyleImpl>>): Iterable<Pair<String, Style>> {
    public constructor(text: String, style: Style): this(mutableListOf(MutablePair(text, StyleImpl(
        font       = style.font,
        foreground = style.foreground,
        background = style.background,
        decoration = style.decoration
    ))))

    public constructor(
        text      : String,
        font      : Font?           = null,
        foreground: Paint?          = null,
        background: Paint?          = null,
        decoration: TextDecoration? = null): this(mutableListOf(MutablePair(text, StyleImpl(
            font,
            foreground = foreground,
            background = background,
            decoration = decoration
    ))))

    public data class MutablePair<A, B>(var first: A, var second: B) {
        override fun toString(): String = "($first, $second)"
    }

    public val text : String get() = data.joinToString(separator = "") { it.first }
    public val count: Int    get() = data.size

    private var hashCode = data.hashCode()

    private class IteratorMapper<R, T>(private val delegate: Iterator<R>, private val block: (R) -> T): Iterator<T> {
        override fun hasNext() = delegate.hasNext()
        override fun next   () = block(delegate.next())
    }

    override fun iterator(): Iterator<Pair<String, Style>> = IteratorMapper(data.iterator()) { it.first to it.second }

    public operator fun plus(other: StyledText): StyledText = this.also { other.data.forEach { style -> add(style) } }

    public operator fun rangeTo(font : Font      ): StyledText = this.also { add(MutablePair("",   StyleImpl(font))) }
    public operator fun rangeTo(color: Color     ): StyledText = this.also { add(MutablePair("",   StyleImpl(foreground = ColorPaint(color)))) }
    public operator fun rangeTo(text : String    ): StyledText = this.also { add(MutablePair(text, StyleImpl())) }
    public operator fun rangeTo(text : StyledText): StyledText = this.also { text.data.forEach { add(MutablePair(it.first, it.second)) } }

    public fun copy(): StyledText = StyledText(MutableList(data.size) { MutablePair(data[it].first, data[it].second.copy()) })

    public fun isBlank   (): Boolean = data.all { it.first.isBlank() }
    public fun isNotBlank(): Boolean = !isBlank()

    public override fun toString(): String = data.toString()

    /** @suppress */
    @Internal
    public fun subString(range: IntRange): StyledText {
        var wordBoundary = 0

        val newData = mutableListOf<MutablePair<String, StyleImpl>>()

        loop@ for (token in data) {
            val nextBoundary = wordBoundary + token.first.length

            if (range.first >= nextBoundary) {
                wordBoundary = nextBoundary
                continue@loop
            }

            val (startIndex, endIndex) = when {
                wordBoundary <= range.first -> range.first - wordBoundary to min(range.last + 1 - wordBoundary, token.first.length)
                range.last < nextBoundary   -> 0 to range.last + 1 - wordBoundary
                else                        -> 0 to token.first.length
            }

            newData += MutablePair(token.first.substring(startIndex, endIndex), token.second)

            if (range.last < nextBoundary) break

            wordBoundary = nextBoundary
        }

        return StyledText(newData)
    }

    /** @suppress */
    @Internal
    public fun mapStyle(block: (Style) -> Style): StyledText = StyledText(data.mapTo(mutableListOf()) {
        MutablePair(it.first, StyleImpl(block(it.second)))
    })

    private fun add(pair: MutablePair<String, StyleImpl>) {
        val (_, style) = data.last()

        return when (style) {
            pair.second -> data.last().first += pair.first
            else        -> data.plusAssign(pair)
        }.also {
            hashCode = data.hashCode()
        }
    }

    override fun hashCode(): Int = hashCode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StyledText) return false

        if (data != other.data) return false

        return true
    }

    public operator fun Font.invoke(text: StyledText): StyledText {
        text.data.forEach { (_, style) ->
            if (style.font == null) {
                style.font = this
            }
        }

        return text
    }

    internal data class StyleImpl(
            override var font      : Font?           = null,
            override var foreground: Paint?          = null,
            override var background: Paint?          = null,
            override var decoration: TextDecoration? = null
    ): Style {
        constructor(other: Style): this(other.font, other.foreground, other.background, other.decoration)
    }
}

// TODO: Change to invoke(text: () -> String) when fixed (https://youtrack.jetbrains.com/issue/KT-22119)
public operator fun Font?.invoke(text: String          ): StyledText = StyledText(text = text, font = this)
public operator fun Font?.invoke(text: () -> StyledText): StyledText = text().apply {
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


public enum class Target {
    Background,
    Foreground
}

// TODO: Change to invoke(text: () -> String) when fixed (https://youtrack.jetbrains.com/issue/KT-22119)
public operator fun Color?.invoke(text: String, target: Target = Foreground): StyledText = this?.let { ColorPaint(it) }.invoke(text, target)
public operator fun Color?.invoke(target: Target = Foreground, text: () -> StyledText): StyledText = this?.let { ColorPaint(it) }.invoke(target, text)

//operator fun Color.get(text: String, fill: Fill = Foreground) = ColorFill(this).let {
//    StyledText(text = text, background = if (fill == Background) it else null, foreground = if (fill == Foreground) it else null)
//}
//operator fun Color.get(text: StyledText) = text.apply {
//    data.forEach { (_, style) ->
//        if (style.foreground == null) {
//            style.foreground = ColorFill(this@get)
//        }
//    }
//}

// TODO: Change to invoke(text: () -> String) when fixed (https://youtrack.jetbrains.com/issue/KT-22119)
public operator fun Paint?.invoke(text: String, target: Target = Foreground): StyledText = this.let {
    StyledText(text = text, background = if (target == Background) it else null, foreground = if (target == Foreground) it else null)
}
public operator fun Paint?.invoke(target: Target = Foreground, text: () -> StyledText): StyledText = text().apply {
    data.forEach { (_, style) ->
        when {
            style.foreground == null && target == Foreground && this@invoke != null -> style.foreground = this@invoke
            style.background == null && target == Background && this@invoke != null -> style.background = this@invoke
        }
    }
}

//operator fun Color.get(text: String, fill: Fill = Foreground) = ColorFill(this).let {
//    StyledText(text = text, background = if (fill == Background) it else null, foreground = if (fill == Foreground) it else null)
//}
//operator fun Color.get(text: StyledText) = text.apply {
//    data.forEach { (_, style) ->
//        if (style.foreground == null) {
//            style.foreground = ColorFill(this@get)
//        }
//    }
//}

// TODO: Change to invoke(text: () -> String) when fixed (https://youtrack.jetbrains.com/issue/KT-22119)
public operator fun TextDecoration?.invoke(text: String          ): StyledText = StyledText(text = text, decoration = this)
public operator fun TextDecoration?.invoke(text: () -> StyledText): StyledText = text().apply {
    data.forEach { (_, style) ->
        if (style.decoration == null) {
            style.decoration = this@invoke
        }
    }
}


public operator fun String.rangeTo(styled: StyledText): StyledText = StyledText(this) + styled

// "foo" .. font {  } + color { }
