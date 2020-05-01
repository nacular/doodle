package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.Font.Style
import com.nectar.doodle.drawing.FontDetector
import com.nectar.doodle.drawing.FontInfo
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.impl.FontDetectorImpl.State.Found
import com.nectar.doodle.drawing.impl.FontDetectorImpl.State.Pending
import com.nectar.doodle.scheduler.Scheduler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Nicholas Eddy on 10/4/18.
 */
class FontDetectorImpl(
        private val textFactory : TextFactory,
        private val elementRuler: ElementRuler,
        private val scheduler   : Scheduler): FontDetector {

    private class FontImpl(override val size: Int, override val weight: Int, override val style: Set<Style>, override val family: String): Font

    private enum class State { Pending, Found }

    private val fonts     = mutableMapOf<Int, State>()
    private val suspended = mutableMapOf<Int, MutableList<Continuation<State?>>>()

    private fun getHash(family: String, weight: Int, size: Int, style: Set<Style>) = arrayOf(family, weight, size, style).contentHashCode()

    override suspend operator fun invoke(info: FontInfo.() -> Unit): Font {
        FontInfo().apply(info).apply {
            val hash = getHash(family, weight, size, style)

            return when (waitIfLoading(hash)) {
                // Only one coroutine will get here at a time.  It will either load the Font or be canceled.  On success, all queued
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

//                        var loadedSize: Size

//                        text.onresize = {
//                            println("onresize")
//                            loadedSize = elementRuler.size(text)
//                            Unit
//                        }

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

    companion object {
        private const val DEFAULT_FAMILY = "monospace"
        private const val TEXT           = "abcdefghijklmnopqrstuvwxyz01234567890~!@#$%^&*()_+{}[]:\'\",./<>?\\|"
    }
}