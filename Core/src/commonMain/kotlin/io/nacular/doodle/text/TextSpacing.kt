package io.nacular.doodle.text

/**
 * @property wordSpacing of text
 * @property letterSpacing of text
 */
public data class TextSpacing(val wordSpacing: Double = 0.0, val letterSpacing: Double = 0.0) {
    public companion object {
        public val default: TextSpacing = TextSpacing()
    }
}