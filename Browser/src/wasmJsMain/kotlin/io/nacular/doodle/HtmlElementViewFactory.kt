package io.nacular.doodle

import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.SystemStyler
import io.nacular.doodle.utils.IdGenerator
import org.w3c.dom.HTMLElement

/**
 * Creates new [View]s that host an HTML element. This allows hosting of existing
 * web components (i.e. React) within an app.
 */
public interface HtmlElementViewFactory {
    /**
     * Creates a new [View] that hosts the given element. The element provided
     * will be made to scale, so it matches the View's size.
     *
     * @param element to be hosted
     * @param autoScale will make [element] fit the View's size when set to `true`
     * @return a new [View] that renders the given element.
     */
    public operator fun invoke(element: HTMLElement, autoScale: Boolean = true): View
}

internal class HtmlElementViewFactoryImpl(
    private val htmlFactory : HtmlFactory,
    private val idGenerator : IdGenerator,
    private val systemStyler: SystemStyler,
): HtmlElementViewFactory {
    override fun invoke(element: HTMLElement, autoScale: Boolean): View = HtmlElementView(
        htmlFactory,
        idGenerator,
        systemStyler,
        element as io.nacular.doodle.dom.HTMLElement,
        autoScale
    )
}