package io.nacular.doodle.theme.native

import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.drawing.Font.Style.Normal
import io.nacular.doodle.drawing.Font.Style.Oblique
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.SystemInputEvent.Modifier.Alt
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.system.SystemPointerEvent.Button.Button2
import io.nacular.doodle.system.SystemPointerEvent.Button.Button3
import io.nacular.doodle.system.SystemPointerEvent.Type.Click
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.measured.units.Angle
import io.nacular.measured.units.div
import io.nacular.measured.units.normalize
import io.nacular.measured.units.times
import kotlinx.datetime.Clock
import org.jetbrains.skia.FontSlant.ITALIC
import org.jetbrains.skia.FontSlant.OBLIQUE
import org.jetbrains.skia.FontSlant.UPRIGHT
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.paragraph.BaselineMode.IDEOGRAPHIC
import org.jetbrains.skia.paragraph.TextStyle
import java.awt.Component
import java.awt.Dimension
import java.awt.event.InputEvent.ALT_DOWN_MASK
import java.awt.event.InputEvent.BUTTON1_DOWN_MASK
import java.awt.event.InputEvent.BUTTON2_DOWN_MASK
import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.InputEvent.META_DOWN_MASK
import java.awt.event.InputEvent.SHIFT_DOWN_MASK
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import java.awt.event.MouseEvent.BUTTON2
import java.awt.event.MouseEvent.BUTTON3
import java.awt.event.MouseEvent.BUTTON3_DOWN_MASK
import java.awt.event.MouseEvent.MOUSE_CLICKED
import java.awt.event.MouseEvent.MOUSE_DRAGGED
import java.awt.event.MouseEvent.MOUSE_ENTERED
import java.awt.event.MouseEvent.MOUSE_EXITED
import java.awt.event.MouseEvent.MOUSE_MOVED
import java.awt.event.MouseEvent.MOUSE_PRESSED
import java.awt.event.MouseEvent.MOUSE_RELEASED
import java.awt.event.MouseWheelEvent
import java.awt.font.TextAttribute.FAMILY
import java.awt.font.TextAttribute.POSTURE
import java.awt.font.TextAttribute.POSTURE_OBLIQUE
import java.awt.font.TextAttribute.POSTURE_REGULAR
import java.awt.font.TextAttribute.SIZE
import java.awt.font.TextAttribute.WEIGHT
import org.jetbrains.skia.Font as SkiaFont
import java.awt.Color as AwtColor
import java.awt.Font as AwtFont
import java.awt.Point as AwtPoint
import java.awt.Rectangle as AwtRectangle

internal fun PointerEvent.toAwt(target: Component, at: Point = location): MouseEvent {
    val id = when (type) {
        Up    -> MOUSE_RELEASED
        Down  -> MOUSE_PRESSED
        Move  -> MOUSE_MOVED
        Exit  -> MOUSE_EXITED
        Drag  -> MOUSE_DRAGGED
        Click -> MOUSE_CLICKED
        Enter -> MOUSE_ENTERED
    }

    val button = when (buttons) {
        setOf(Button2) -> BUTTON2
        setOf(Button3) -> BUTTON3
        else           -> BUTTON1
    }

    // FIXME: Inject
    val time = Clock.System.now().toEpochMilliseconds()

    var modifiers = when (button) {
        BUTTON2 -> BUTTON2_DOWN_MASK
        BUTTON3 -> BUTTON3_DOWN_MASK
        else    -> BUTTON1_DOWN_MASK
    }

    if (Alt in this.modifiers) {
        modifiers = modifiers or ALT_DOWN_MASK
    }

    if (Ctrl in this.modifiers) {
        modifiers = modifiers or CTRL_DOWN_MASK
    }

    if (Shift in this.modifiers) {
        modifiers = modifiers or SHIFT_DOWN_MASK
    }

    if (Meta in this.modifiers) {
        modifiers = modifiers or META_DOWN_MASK
    }

    return MouseEvent(target, id, time, modifiers, at.x.toInt(), at.y.toInt(), clickCount, false, button)
}

internal fun MouseEvent.update(target: Component, location: AwtPoint): MouseEvent = when (this) {
    is MouseWheelEvent -> MouseWheelEvent(
                        target,
                        id,
                        `when`,
                        modifiersEx,
                        location.x, location.y,
                        clickCount, isPopupTrigger, scrollType, scrollAmount, wheelRotation)
    else -> MouseEvent(target, id, `when`, modifiersEx, location.x, location.y, clickCount, isPopupTrigger)
}

private const val MAX_SKIA_FONT_WEIGHT = 1000f

internal fun AwtFont.skiaStyle(): FontStyle {
    val w = attributes[WEIGHT] as? Float?

    val weight = when {
        w == null || w > 1f -> when (style) {
            AwtFont.PLAIN                 -> FontStyle.NORMAL
            AwtFont.BOLD                  -> FontStyle.BOLD
            AwtFont.ITALIC                -> FontStyle.ITALIC
            AwtFont.BOLD + AwtFont.ITALIC -> FontStyle.BOLD_ITALIC
            else                          -> FontStyle.NORMAL
        }.weight
        else -> (w * MAX_SKIA_FONT_WEIGHT).toInt()
    }

    return FontStyle(weight, size, (attributes[POSTURE] as? Float).let {
        when (it) {
            POSTURE_OBLIQUE -> ITALIC
            else            -> UPRIGHT
        }
    })
}

internal fun SkiaFont.toAwt() = AwtFont(mutableMapOf(
        SIZE    to size,
        FAMILY  to typefaceOrDefault.familyName,
        WEIGHT  to typefaceOrDefault.fontStyle.weight / MAX_SKIA_FONT_WEIGHT,
        POSTURE to typefaceOrDefault.fontStyle.slant.run {
            when (this) {
                ITALIC  -> POSTURE_OBLIQUE
                OBLIQUE -> POSTURE_OBLIQUE
                else    -> POSTURE_REGULAR
            }
        }
))

internal fun Font?.toAwt(default: SkiaFont) = when (this) {
    null -> default.toAwt()
    else -> AwtFont(mutableMapOf(
        SIZE    to size,
        FAMILY  to family,
        WEIGHT  to weight / MAX_SKIA_FONT_WEIGHT,
        POSTURE to style.run {
            when (this) {
                Normal     -> POSTURE_REGULAR
                Italic     -> POSTURE_OBLIQUE
                is Oblique -> angle?.normalize()?.div(360 * Angle.degrees) ?: POSTURE_OBLIQUE
            }
        }
    ))
}

internal fun AwtFont?.toDoodle(): Font? = when (this) {
    null -> null
    else -> object: Font {
        override val size  : Int    get() = this@toDoodle.size
        override val style : Style  get() = when {
            this@toDoodle.isItalic      -> Italic
            this@toDoodle.isTransformed -> Oblique()
            else                        -> Normal
        }
        override val weight: Int    get() = this@toDoodle.attributes[WEIGHT] as? Int ?: 0
        override val family: String get() = this@toDoodle.family
    }
}

internal fun SkiaFont.textStyle() = TextStyle().apply {
    fontSize     = size
    typeface     = typefaceOrDefault
    fontStyle    = typefaceOrDefault.fontStyle
    fontFamilies = typefaceOrDefault.familyNames.map { it.name }.toTypedArray()
    baselineMode = IDEOGRAPHIC
}

internal fun Color.toAwt() = AwtColor(red.toInt(), green.toInt(), blue.toInt(), (opacity * 255).toInt())
internal fun AwtColor.toDoodle() = Color(red.toUByte(), green.toUByte(), blue.toUByte(), transparency.toFloat() / 255)

internal fun Point.toAwt() = AwtPoint(x.toInt(), y.toInt())
internal fun AwtPoint.toDoodle() = Point(x, y)

internal fun Size.toAwt() = Dimension(width.toInt(), height.toInt())
internal fun Dimension.toDoodle() = Size(width, height)

internal fun Rectangle.toAwt() = AwtRectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt())
internal fun AwtRectangle.toDoodle() = Rectangle(x, y, width, height)