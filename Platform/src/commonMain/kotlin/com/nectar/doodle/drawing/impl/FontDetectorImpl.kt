package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.Font.Style
import com.nectar.doodle.drawing.Font.Weight
import com.nectar.doodle.drawing.FontDetector
import com.nectar.doodle.drawing.FontInfo
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.impl.State.Found
import com.nectar.doodle.scheduler.Scheduler

/**
 * Created by Nicholas Eddy on 10/4/18.
 */
private class FontImpl(override val size: Int, override val weight: Weight, override val style: Set<Style>, override val family: String): Font

private const val DEFAULT_FAMILY = "monospace"
private const val TEXT           = "abcdefghijklmnopqrstuvwxyz01234567890~!@#$%^&*()_+{}[]:\'\",./<>?\\|"

private enum class State { Pending, Found }

class FontDetectorImpl(
        private val textFactory : TextFactory,
        private val elementRuler: ElementRuler,
        private val scheduler   : Scheduler): FontDetector {

    private val fonts = HashMap<Int, State>()

    private fun getHash(family: String, weight: Weight, size: Int, style: Set<Style>) = arrayOf(family, weight, size, style).contentHashCode()

    override suspend operator fun invoke(info: FontInfo.() -> Unit): Font {
        FontInfo().apply(info).apply {
            val hash = getHash(family, weight, size, style)

            when (fonts[hash]) {
                Found -> return FontImpl(size, weight, style, family)
                else  -> {

                    if (family == DEFAULT_FAMILY || family.isBlank()) {
                        return FontImpl(size, weight, style, family)
                    }

                    val text        = textFactory.create(TEXT, FontImpl(size, weight, style, "$family, $DEFAULT_FAMILY"))
                    val defaultSize = elementRuler.size(textFactory.create(TEXT, FontImpl(size, weight, style, DEFAULT_FAMILY)))

                    scheduler.delayUntil { elementRuler.size(text) != defaultSize }

                    fonts[hash] = Found

                    return FontImpl(size, weight, style, family)
                }
            }
        }
    }
}