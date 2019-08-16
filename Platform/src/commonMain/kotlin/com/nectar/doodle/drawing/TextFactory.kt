package com.nectar.doodle.drawing

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 10/30/17.
 */

interface TextFactory {
    fun create (text: String,     font: Font? = null,                                      possible: HTMLElement? = null): HTMLElement
    fun create (text: StyledText,                                                          possible: HTMLElement? = null): HTMLElement
    fun wrapped(text: String,     font: Font? = null, width: Double, indent: Double = 0.0, possible: HTMLElement? = null): HTMLElement
    fun wrapped(text: StyledText,                     width: Double, indent: Double = 0.0, possible: HTMLElement? = null): HTMLElement
}