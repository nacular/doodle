package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.SystemStyler
import io.nacular.doodle.dom.styleText
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontInfo
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.impl.FontLoaderLegacy.State.Found
import io.nacular.doodle.drawing.impl.FontLoaderLegacy.State.Pending
import io.nacular.doodle.scheduler.Scheduler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * This implementation is intended to be replaced with [FontLoaderImpl] once there is wider browser adoption of the FontFaceSet APIs (https://caniuse.com/?search=fontfaceset).
 */
internal class FontLoaderLegacy(
        private val systemStyler: SystemStyler,
        private val textFactory : TextFactory,
        private val elementRuler: ElementRuler,
        private val scheduler   : Scheduler): FontLoader {

    private val loadedFonts = mutableSetOf<Pair<String, FontInfo>>()

    override suspend fun invoke(source: String, info: FontInfo.() -> Unit): Font = FontInfo().apply(info).let {
        if (source to it !in loadedFonts) {
            systemStyler.insertRule("""
            @font-face {
                font-family: "${it.family}";
                font-style: ${it.style.styleText};
                font-weight: ${it.weight};
                src: url($source)
            }""".trimIndent())
        }

        this(info)
    }

    override suspend operator fun invoke(info: FontInfo.() -> Unit): Font {
        FontInfo().apply(info).apply {
            val hash = getHash(family, weight, size, style)

            return when (waitIfLoading(hash)) {
                // Only one coroutine will get here at a time. It will either load the Font or be canceled.  On success, all queued
                // loads will be resolved and return.  Otherwise, the next in line is allowed to take a shot.
                Found -> FontImpl(size, weight, style, family)
                else  -> {
                    try {
                        if (family == DEFAULT_FAMILY || family.isBlank()) {
                            return FontImpl(size, weight, style, family)
                        }

                        fonts[hash] = Pending

                        val text        = textFactory.create(TEXT, FontImpl(size, weight, style, "$family, $DEFAULT_FAMILY"))
                        val defaultSize = elementRuler.size(textFactory.create(TEXT, FontImpl(size, weight, style, DEFAULT_FAMILY)))

                        scheduler.delayUntil { elementRuler.size(text) != defaultSize } // FIXME: Use approach that adds element and observes size/scroll like: https://github.com/bramstein/fontfaceobserver/blob/master/src/ruler.js

                        fonts[hash] = Found

                        return FontImpl(size, weight, style, family).also {
                            suspended[hash]?.forEach { it.resume(Found) }
                            suspended -= hash
                        }
                    } finally {
                        // Handle case that this coroutine was canceled or errored out
                        suspended[hash]?.let { items ->
                            items.firstOrNull()?.let {
                                it.resume(fonts[hash])
                                items -= it
                            }

                            if (items.isEmpty()) {
                                suspended -= hash
                            }
                        }
                    }
                }
            }
        }
    }

    private enum class State { Pending, Found }

    private val fonts     = mutableMapOf<Int, State>()
    private val suspended = mutableMapOf<Int, MutableList<Continuation<State?>>>()

    private fun getHash(family: String, weight: Int, size: Int, style: Font.Style) = arrayOf(family, weight, size, style).contentHashCode()

    private suspend fun waitIfLoading(hash: Int): State? = suspendCoroutine {
        when (fonts[hash]) {
            Found   -> it.resume(Found)
            Pending -> {
                // Enqueue for resolution later when the current coroutine finds the font, or fails and dequeues the next in line
                suspended.getOrPut(hash) { mutableListOf() }.add(it)
            }
            else    -> it.resume(null) // No attempts yet, so proceed to resolve right away
        }
    }

    private companion object {
        private const val DEFAULT_FAMILY = "monospace"
        private const val TEXT           = "abcdefghijklmnopqrstuvwxyz01234567890~!@#$%^&*()_+{}[]:\'\",./<>?\\|"
    }
}