package io.nacular.doodle.drawing

/**
 * Detects Browser's default line height for a Font.
 */
internal interface LineHeightDetector {
    /**
     * @param font to check
     * @returns the default line height for [font]
     */
    fun lineHeight(font: Font? = null): Float
}