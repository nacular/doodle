package io.nacular.doodle.dom

import io.nacular.doodle.HTMLButtonElement
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.HTMLImageElement
import io.nacular.doodle.HTMLInputElement
import io.nacular.doodle.Node
import io.nacular.doodle.Text

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

interface HtmlFactory {
    val root: HTMLElement

    fun <T: HTMLElement> create(           ): T
    fun <T: HTMLElement> create(tag: String): T

    fun createText (text  : String                 ): Text
    fun createImage(source: String                 ): HTMLImageElement
    fun createOrUse(tag   : String, possible: Node?): HTMLElement

    fun createInput (): HTMLInputElement
    fun createButton(): HTMLButtonElement
}