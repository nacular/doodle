package com.nectar.doodle.dom

import com.nectar.doodle.HTMLButtonElement
import com.nectar.doodle.HTMLElement
import com.nectar.doodle.HTMLImageElement
import com.nectar.doodle.HTMLInputElement
import com.nectar.doodle.Node
import com.nectar.doodle.Text

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