package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 10/24/17.
 */
public interface SvgFactory {
    public val root: HTMLElement

    public operator fun <T: SVGElement> invoke(tag: String): T
}