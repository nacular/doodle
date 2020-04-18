package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.Font.Style
import com.nectar.doodle.drawing.Font.Weight
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

    private class FontImpl(override val size: Int, override val weight: Weight, override val style: Set<Style>, override val family: String): Font

    private enum class State { Pending, Found }

    private val fonts     = mutableMapOf<Int, State>()
    private val suspended = mutableMapOf<Int, MutableList<Continuation<FontImpl>>>()

    private fun getHash(family: String, weight: Weight, size: Int, style: Set<Style>) = arrayOf(family, weight, size, style).contentHashCode()

    override suspend operator fun invoke(info: FontInfo.() -> Unit): Font {
        FontInfo().apply(info).apply {
            val hash = getHash(family, weight, size, style)

            return when (fonts[hash]) {
                Found   -> FontImpl(size, weight, style, family)
                Pending -> suspendCoroutine {
                    suspended.getOrPut(hash) { mutableListOf() }.add(it) // FIXME: Handle case where pending coroutine is canceled
                }
                else    -> {
                    if (family == DEFAULT_FAMILY || family.isBlank()) {
                        return FontImpl(size, weight, style, family)
                    }

                    fonts[hash] = Pending

                    val text        = textFactory.create(TEXT, FontImpl(size, weight, style, "$family, $DEFAULT_FAMILY"))
                    val defaultSize = elementRuler.size(textFactory.create(TEXT, FontImpl(size, weight, style, DEFAULT_FAMILY)))

                    var loadedSize  = defaultSize

                    text.onresize = {
                        loadedSize = elementRuler.size(text)
                        Unit
                    }

                    scheduler.delayUntil { loadedSize != defaultSize }

                    fonts[hash] = Found

                    FontImpl(size, weight, style, family).also { font ->
                        suspended[hash]?.forEach { it.resume(font) }
                        suspended -= hash
                    }
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_FAMILY = "monospace"
        private const val TEXT           = "abcdefghijklmnopqrstuvwxyz01234567890~!@#$%^&*()_+{}[]:\'\",./<>?\\|"
    }
}