package io.nacular.doodle

import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.SystemStyler
import io.nacular.doodle.utils.IdGenerator
import org.w3c.dom.HTMLElement

public interface HtmlElementViewFactory {
    public operator fun invoke(element: HTMLElement): View
}

internal class HtmlElementViewFactoryImpl(
    private val htmlFactory : HtmlFactory,
    private val idGenerator : IdGenerator,
    private val systemStyler: SystemStyler,
): HtmlElementViewFactory {
    override fun invoke(element: HTMLElement): View = HtmlElementView(
        htmlFactory,
        idGenerator,
        systemStyler,
        element as io.nacular.doodle.dom.HTMLElement
    )
}