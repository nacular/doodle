package io.nacular.doodle.system.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.CSSStyleDeclaration
import io.nacular.doodle.Document
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.setCursor
import io.nacular.doodle.system.Cursor
import kotlin.test.Test
import kotlin.test.expect

class PointerInputServiceStrategyWebkitTests {
    @Test fun `cursor defaults to null`() {
        expect(createStrategy().cursor) { null }
    }

    @Test fun `set cursor works`() {
        val style       = mockk<CSSStyleDeclaration>()
        val root        = mockk<HTMLElement>().apply { every { this@apply.style } returns style }
        val cursor  = mockk<Cursor>()
        val htmlFactory = mockk<HtmlFactory>().apply {
            every { this@apply.root } returns root
        }

        createStrategy(htmlFactory = htmlFactory).apply {
            startUp(mockk())
            this.cursor = cursor
        }

        verify(exactly = 1) {
            style.setCursor(cursor)
        }
    }

    @Test fun `default tooltip blank`() {
        expect(true) { createStrategy().toolTipText.isBlank() }
    }

    @Test fun `set tooltip works`() {
        val style       = mockk<CSSStyleDeclaration>()
        val root        = mockk<HTMLElement>().apply { every { this@apply.style } returns style }
        val htmlFactory = mockk<HtmlFactory>().apply {
            every { this@apply.root } returns root
        }

        val value = "some tooltip text"

        createStrategy(htmlFactory = htmlFactory).apply {
            startUp(mockk())
            toolTipText = value
        }

        verify(exactly = 1) {
            root.title = value
        }
    }

    private fun createStrategy(
        document               : Document                = mockk(),
        htmlFactory            : HtmlFactory             = mockk(),
        pointerLocationResolver: PointerLocationResolver = mockk()
    ) = PointerInputServiceStrategyWebkit(document, htmlFactory, pointerLocationResolver)
}