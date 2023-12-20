package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

public interface HtmlFactory {
    public val root: HTMLElement

    public fun <T: HTMLElement> create(           ): T
    public fun <T: HTMLElement> create(tag: String): T

    public fun createText (text  : String                        ): Text
    public fun createImage(source: String                        ): HTMLImageElement
    public fun createOrUse(tag   : String, possible: HTMLElement?): HTMLElement

    public fun createInput (): HTMLInputElement
    public fun createButton(): HTMLButtonElement
}