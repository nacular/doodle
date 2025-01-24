package io.nacular.doodle.text

import io.nacular.doodle.core.Internal
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.text.Target.Background
import io.nacular.doodle.text.Target.Foreground
import io.nacular.doodle.text.TextDecoration.Line.Through
import io.nacular.doodle.text.TextDecoration.Line.Under
import io.nacular.doodle.text.TextDecoration.Style.Solid
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
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

    public companion object {
        /** [Underline][io.nacular.doodle.text.TextDecoration.UnderLine] style with defaults */
        public val UnderLine  : TextDecoration = TextDecoration(lines = setOf(Under  ))

        /** [LineThrough][io.nacular.doodle.text.TextDecoration.LineThrough] style with defaults */
        public val LineThrough: TextDecoration = TextDecoration(lines = setOf(Through))

        /**
         * Utility for creating [Underline][io.nacular.doodle.text.TextDecoration.UnderLine] [io.nacular.doodle.text.TextDecoration].
         */
        public fun underLine(color: Color, style: Style = Solid, thickness: Thickness? = null): TextDecoration = TextDecoration(
            lines     = setOf(Under),
            color     = color,
            style     = style,
            thickness = thickness
        )

        /**
         * Utility for creating [LineThrough][io.nacular.doodle.text.TextDecoration.LineThrough] [io.nacular.doodle.text.TextDecoration].
         */
        public fun lineThrough(color: Color, style: Style = Solid, thickness: Thickness? = null): TextDecoration = TextDecoration(
            lines     = setOf(Through),
            color     = color,
            style     = style,
            thickness = thickness
        )
    }
}

/**
 * Style for a segment of [StyledText].
 *
 * @property font for that segment
 * @property foreground for that segment
 * @property background for that segment
 * @property decoration for that segment
 * @property stroke for that segment
 */
public interface Style {
    public val font      : Font?
    public val foreground: Paint?
    public val background: Paint?
    public val decoration: TextDecoration?
    public val stroke    : Stroke?
}

public class StyledText private constructor(internal val data: MutableList<MutablePair<String, StyleImpl>>): Iterable<Pair<String, Style>> {
    public constructor(text: String, style: Style): this(mutableListOf(MutablePair(text, StyleImpl(
        font       = style.font,
        foreground = style.foreground,
        background = style.background,
        decoration = style.decoration,
        stroke     = style.stroke,
    ))))

    public constructor(
        text      : String,
        font      : Font?           = null,
        foreground: Paint?          = null,
        background: Paint?          = null,
        decoration: TextDecoration? = null,
        stroke    : Stroke?         = null): this(mutableListOf(MutablePair(text, StyleImpl(
            font       = font,
            foreground = foreground,
            background = background,
            decoration = decoration,
            stroke     = stroke
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
    public operator fun plus(text : String    ): StyledText = this .. text

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

    internal data class StyleImpl(
        override var font      : Font?           = null,
        override var foreground: Paint?          = null,
        override var background: Paint?          = null,
        override var decoration: TextDecoration? = null,
        override var stroke    : Stroke?         = null,
    ): Style {
        constructor(other: Style): this(
            font       = other.font,
            foreground = other.foreground,
            background = other.background,
            decoration = other.decoration,
            stroke     = other.stroke
        )
    }
}

/**
 * Indicates whether a [Paint] is applied to the foreground or background
 *
 * @property Background for the [Style.background] property
 * @property Foreground for the [Style.foreground] property
 */
public enum class Target {
    Background,
    Foreground
}

// region Font

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun Font?.invoke(text: () -> String    ): StyledText = StyledText(text = text(), font = this)

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun Font?.invoke(text: () -> StyledText): StyledText = text().apply {
    data.forEach { (_, style) ->
        if (style.font == null) {
            style.font = this@invoke
        }
    }
}

// endregion

// region Color


@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun Color?.invoke(target: Target = Foreground, text: () -> String): StyledText = this?.let { ColorPaint(it) }.invoke(target, text)

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun Color?.invoke(target: Target = Foreground, text: () -> StyledText): StyledText = this?.let { ColorPaint(it) }.invoke(target, text)

// endregion

// region Paint

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun Paint?.invoke(target: Target = Foreground, text:() -> String): StyledText = this.let {
    StyledText(text = text(), background = if (target == Background) it else null, foreground = if (target == Foreground) it else null)
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun Paint?.invoke(target: Target = Foreground, text: () -> StyledText): StyledText = text().apply {
    data.forEach { (_, style) ->
        when {
            style.foreground == null && target == Foreground && this@invoke != null -> style.foreground = this@invoke
            style.background == null && target == Background && this@invoke != null -> style.background = this@invoke
        }
    }
}

// endregion

// region TextDecoration

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun TextDecoration?.invoke(text: () -> String): StyledText = StyledText(text = text(), decoration = this)

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun TextDecoration?.invoke(text: () -> StyledText): StyledText = text().apply {
    data.forEach { (_, style) ->
        if (style.decoration == null) {
            style.decoration = this@invoke
        }
    }
}

// endregion

// region Stroke

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun Stroke?.invoke(text:() -> String): StyledText = this.let {
    StyledText(text = text(), stroke = this)
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun Stroke?.invoke(text: () -> StyledText): StyledText = text().apply {
    data.forEach { (_, style) ->
        if (style.stroke == null) {
            style.stroke = this@invoke
        }
    }
}

// endregion

public operator fun String.rangeTo(styled: StyledText): StyledText = StyledText(this) + styled