package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Size
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 10/30/17.
 */

interface TextMetrics {
    fun width (font: Font, text: String): Double
    fun width (text: StyledText        ): Double
    fun height(font: Font, text: String): Double
    fun height(text: StyledText        ): Double

    fun size(font: Font, text: String                                     ) = Size(width(font, text), height(font, text))
    fun size(font: Font, text: String, width: Double, indent: Double = 0.0): Size

    fun size(text: StyledText                                     ) = Size(width(text), height(text))
    fun size(text: StyledText, width: Double, indent: Double = 0.0): Size
}