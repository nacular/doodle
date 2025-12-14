package io.nacular.doodle.drawing

import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.text.AnnotatedString
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.TextAlignment

/**
 * Created by Nicholas Eddy on 10/30/17.
 */

internal interface TextFactory {
    fun create (text: String,     font: Font? = null,                                                                                    textSpacing: TextSpacing, possible: HTMLElement? = null): HTMLElement
    fun create (text: String,     font: Font? = null,                                                                lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement? = null): HTMLElement
    fun create (text: AnnotatedString, textSpacing: TextSpacing, possible: HTMLElement? = null): HTMLElement
    fun create (text: AnnotatedString, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement? = null): HTMLElement
    fun wrapped(text: String,     font: Font? = null, width: Double, indent: Double = 0.0, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement? = null): HTMLElement
    fun wrapped(text: String,     font: Font? = null,                indent: Double = 0.0, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement? = null): HTMLElement
    fun wrapped(text: AnnotatedString, width: Double, indent: Double = 0.0, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement? = null): HTMLElement
    fun wrapped(text: AnnotatedString, indent: Double = 0.0, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement? = null): HTMLElement
}