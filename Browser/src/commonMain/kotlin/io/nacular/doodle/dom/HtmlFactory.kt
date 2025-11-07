package io.nacular.doodle.dom

import io.nacular.doodle.utils.Orientation

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

internal interface HtmlFactory {
    val root: HTMLElement

    fun <T: HTMLElement> create(           ): T
    fun <T: HTMLElement> create(tag: String): T

    fun createText (text  : String                        ): Text
    fun createImage(source: String                        ): HTMLImageElement
    fun createOrUse(tag   : String, possible: HTMLElement?): HTMLElement

    fun createInput     (                                                 ): HTMLInputElement
    fun createButton    (                                                 ): HTMLButtonElement
    fun createOption    (                                                 ): HTMLOptionElement
    fun createDataList  (                                                 ): HTMLDataListElement
    fun createRangeInput(orientation: Orientation = Orientation.Horizontal): HTMLInputElement
}