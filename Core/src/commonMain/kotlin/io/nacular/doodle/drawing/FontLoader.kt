package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Font.Companion.Normal
import io.nacular.doodle.drawing.Font.Style

/**
 * @property size of the Font
 * @property style of the Font
 * @property weight of the Font
 * @property family of the Font, NOTE: should only be a single family and NOT a comma-separated list
 */
class FontInfo(
        var size  : Int    = 12,
        var style : Style  = Style.Normal,
        var weight: Int    = Normal,
        var family: String = "")

/**
 * Provides a mechanism to load or lookup [Font]s.
 */
interface FontLoader {
    /**
     * Tries to find a loaded font matching the given info.
     *
     * @param info of the font
     * @return the font IFF found
     */
    suspend operator fun invoke(info: FontInfo.() -> Unit): Font

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
    suspend operator fun invoke(font: Font, info: FontInfo.() -> Unit): Font = invoke {
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
    suspend operator fun invoke(source: String, info: FontInfo.() -> Unit): Font
}