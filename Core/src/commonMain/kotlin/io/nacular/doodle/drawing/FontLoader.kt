package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Font.Companion.Normal
import io.nacular.doodle.drawing.Font.Style

/**
 * @property size of the Font
 * @property style of the Font
 * @property weight of the Font
 * @property families of the Font, in priority order (where subsequent names are fallbacks if previous ones cannot load)
 */
public class FontInfo(
        public var size    : Int          = 12,
        public var style   : Style        = Style.Normal,
        public var weight  : Int          = Normal,
        public var families: List<String> = emptyList()) {

    /**
     * Single family name of the Font. Note this should not be a comma-separated list. Use [families] to get
     * "fall-back" behavior for family names.
     */
    public var family: String
        get(   ) = families.firstOrNull() ?: ""
        set(new) {
            families = listOf(new)
        }

    public companion object {
        public operator fun invoke(
                size  : Int    = 12,
                style : Style  = Style.Normal,
                weight: Int    = Normal,
                family: String = ""): FontInfo = FontInfo(size, style, weight, listOf(family))
    }
}

/**
 * Provides a mechanism to load or lookup [Font]s.
 */
public interface FontLoader {
    /**
     * Tries to find a loaded font matching the given info.
     *
     * @param info of the font
     * @return the font IFF found
     */
    public suspend operator fun invoke(info: FontInfo.() -> Unit): Font

    /**
     * Tries to find a loaded font matching the given font with info overrides.
     *
     * ```kotlin
     * val font = ...
     *
     * loader(font) {
     *  weight = 700 // looks for variant of font with weight == 700
     * }
     * ```
     *
     * @param info of the font
     * @return the font IFF found
     */
    public suspend operator fun invoke(font: Font, info: FontInfo.() -> Unit): Font = invoke {
        size   = font.size
        style  = font.style
        weight = font.weight
        family = font.family
        apply(info)
    }

    /**
     * Tries to loaded a font matching the given info.
     *
     * @param source where the font is located (i.e. a filename or url)
     * @param info of the font
     * @return the font IFF found
     */
    public suspend operator fun invoke(source: String, info: FontInfo.() -> Unit): Font
}