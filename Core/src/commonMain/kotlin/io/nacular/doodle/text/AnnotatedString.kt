package io.nacular.doodle.text

import io.nacular.doodle.core.Internal
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.text.AnnotatedString.Annotation
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

public class AnnotatedString private constructor(internal val data: MutableList<MutablePair<String, Annotation>>): Iterable<Pair<String, Annotation>> {
    public constructor(text: String, annotation: Annotation): this(mutableListOf(MutablePair(text, annotation)))

    public constructor(
        text      : String,
        font      : Font?           = null,
        foreground: Paint?          = null,
        background: Paint?          = null,
        decoration: TextDecoration? = null,
        stroke    : Stroke?         = null,
        selectable: Boolean         = false
    ): this(mutableListOf(MutablePair(text, Annotation(
        font       = font,
        foreground = foreground,
        background = background,
        decoration = decoration,
        stroke     = stroke,
        selectable = selectable
    ))))

    /**
     * Annotation for a segment of [AnnotatedString].
     *
     * @property font       for that segment
     * @property foreground for that segment
     * @property background for that segment
     * @property decoration for that segment
     * @property stroke     for that segment
     * @property selectable for that segment
     */
    public class Annotation(
        font      : Font?           = null,
        foreground: Paint?          = null,
        background: Paint?          = null,
        decoration: TextDecoration? = null,
        stroke    : Stroke?         = null,
        selectable: Boolean         = false
    ) {
        internal fun copy(): Annotation = Annotation(
            font       = font,
            foreground = foreground,
            background = background,
            decoration = decoration,
            stroke     = stroke,
            selectable = selectable,
        )

        public var font      : Font?           = font;       internal set
        public var foreground: Paint?          = foreground; internal set
        public var background: Paint?          = background; internal set
        public var decoration: TextDecoration? = decoration; internal set
        public var stroke    : Stroke?         = stroke;     internal set
        public var selectable: Boolean         = selectable; internal set
    }

    internal data class MutablePair<A, B>(var first: A, var second: B) {
        override fun toString(): String = "($first, $second)"
    }

    public val text : String get() = data.joinToString(separator = "") { it.first }
    public val count: Int    get() = data.size

    private var hashCode = data.hashCode()

    private class IteratorMapper<R, T>(private val delegate: Iterator<R>, private val block: (R) -> T): Iterator<T> {
        override fun hasNext() = delegate.hasNext()
        override fun next   () = block(delegate.next())
    }

    override fun iterator(): Iterator<Pair<String, Annotation>> = IteratorMapper(data.iterator()) { it.first to it.second }

    public operator fun plus(other: AnnotatedString): AnnotatedString = this.also { other.data.forEach { style -> add(style) } }
    public operator fun plus(text : String         ): AnnotatedString = this .. text

    public operator fun rangeTo(text : String    ): AnnotatedString = this.also { add(MutablePair(text, Annotation())) }
    public operator fun rangeTo(text : AnnotatedString): AnnotatedString = this.also { text.data.forEach { add(MutablePair(it.first, it.second)) } }

    public fun copy(): AnnotatedString = AnnotatedString(MutableList(data.size) { MutablePair(data[it].first, data[it].second.copy()) })

    public fun isBlank   (): Boolean = data.all { it.first.isBlank() }
    public fun isNotBlank(): Boolean = !isBlank()

    public override fun toString(): String = data.toString()

    /** @suppress */
    @Internal
    public fun subString(range: IntRange): AnnotatedString {
        var wordBoundary = 0

        val newData = mutableListOf<MutablePair<String, Annotation>>()

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

        return AnnotatedString(newData)
    }

    /** @suppress */
    @Internal
    public fun mapStyle(block: (Annotation) -> Annotation): AnnotatedString = AnnotatedString(data.mapTo(mutableListOf()) {
        MutablePair(it.first, it.second)
    })

    private fun add(pair: MutablePair<String, Annotation>) {
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
        if (other !is AnnotatedString) return false

        if (data != other.data) return false

        return true
    }

//    internal data class AnnotationImpl(
//        override var font      : Font?           = null,
//        override var foreground: Paint?          = null,
//        override var background: Paint?          = null,
//        override var decoration: TextDecoration? = null,
//        override var stroke    : Stroke?         = null,
//        override var selectable: Boolean         = false,
//    ): Annotation {
//        constructor(other: Annotation): this(
//            font       = other.font,
//            foreground = other.foreground,
//            background = other.background,
//            decoration = other.decoration,
//            stroke     = other.stroke,
//            selectable = other.selectable
//        )
//    }
}

/**
 * Indicates whether a [Paint] is applied to the foreground or background
 *
 * @property Background for the [Annotation.background] property
 * @property Foreground for the [Annotation.foreground] property
 */
public enum class Target {
    Background,
    Foreground
}

// region Font

/**
 * @return a new [AnnotatedString] with the contents of [text] and this font.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun Font?.invoke(text: () -> String    ): AnnotatedString = AnnotatedString(text = text(), font = this)

/**
 * @return sets the font for all segments without a font in [AnnotatedString] returned by [text] to this one.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun Font?.invoke(text: () -> AnnotatedString): AnnotatedString = text().apply {
    data.forEach { (_, annotation) ->
        if (annotation.font == null) {
            annotation.font = this@invoke
        }
    }
}

// endregion

// region Color


/**
 * @return a new [AnnotatedString] with the contents of [text] and painted (based on [target]) with this Color.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun Color?.invoke(target: Target = Foreground, text: () -> String): AnnotatedString = this?.let { ColorPaint(it) }.invoke(target, text)

/**
 * @return paints all unpainted segments (based on [target]) in [AnnotatedString] returned by [text] with this one.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun Color?.invoke(target: Target = Foreground, text: () -> AnnotatedString): AnnotatedString = this?.let { ColorPaint(it) }.invoke(target, text)

// endregion

// region Paint

/**
 * @return a new [AnnotatedString] with the contents of [text] and painted (based on [target]) with this one.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun Paint?.invoke(target: Target = Foreground, text:() -> String): AnnotatedString = this.let {
    AnnotatedString(text = text(), background = if (target == Background) it else null, foreground = if (target == Foreground) it else null)
}

/**
 * @return paints all unpainted segments (based on [target]) in [AnnotatedString] returned by [text] with this one.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun Paint?.invoke(target: Target = Foreground, text: () -> AnnotatedString): AnnotatedString = text().apply {
    data.forEach { (_, annotation) ->
        when {
            annotation.foreground == null && target == Foreground && this@invoke != null -> annotation.foreground = this@invoke
            annotation.background == null && target == Background && this@invoke != null -> annotation.background = this@invoke
        }
    }
}

// endregion

// region TextDecoration

/**
 * @return a new [AnnotatedString] with the contents of [text] and decorated with this one.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun TextDecoration?.invoke(text: () -> String): AnnotatedString = AnnotatedString(text = text(), decoration = this)

/**
 * @return decorates all undecorated segments in [AnnotatedString] returned by [text] with this one.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun TextDecoration?.invoke(text: () -> AnnotatedString): AnnotatedString = text().apply {
    data.forEach { (_, annotation) ->
        if (annotation.decoration == null) {
            annotation.decoration = this@invoke
        }
    }
}

// endregion

// region Stroke

/**
 * @return a new [AnnotatedString] with the contents of [text] and stroked with this one.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("invokeString")
public operator fun Stroke?.invoke(text: () -> String): AnnotatedString = AnnotatedString(text = text(), stroke = this)

/**
 * @return strokes all unstroked segments in [AnnotatedString] returned by [text] with this one.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public operator fun Stroke?.invoke(text: () -> AnnotatedString): AnnotatedString = text().apply {
    data.forEach { (_, annotation) ->
        if (annotation.stroke == null) {
            annotation.stroke = this@invoke
        }
    }
}

// endregion

// region selection

public operator fun String.rangeTo(styled: AnnotatedString): AnnotatedString = AnnotatedString(this) + styled

/**
 * @return [AnnotatedString] that is selectable with the contents returned by [text]
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("selectableString")
public fun selectable(text: () -> String): AnnotatedString = AnnotatedString(text = text(), selectable = true)

/**
 * @return makes the [AnnotatedString] returned by [text] selectable.
 */
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public fun selectable(text: () -> AnnotatedString): AnnotatedString = text().apply {
    data.forEach { (_, annotation) ->
        annotation.selectable = true
    }
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("nonselectableString")
public fun nonselectable(text: () -> String): AnnotatedString = AnnotatedString(text = text(), selectable = false)

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public fun nonselectable(text: () -> AnnotatedString): AnnotatedString = text().apply {
    data.forEach { (_, annotation) ->
        annotation.selectable = false
    }
}

// endregion