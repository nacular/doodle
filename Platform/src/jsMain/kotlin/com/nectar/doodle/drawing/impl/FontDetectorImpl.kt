package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.FontDetector
import com.nectar.doodle.drawing.FontInfo
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.impl.State.Found
import com.nectar.doodle.drawing.impl.State.Pending
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.time.Timer
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.seconds
import com.nectar.measured.units.times
import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 10/4/18.
 */

private class FontImpl(
        override val size  : Int,
        override val weight: Font.Weight,
        override val style : Set<Font.Style>,
        override val family: String): Font

private const val DEFAULT_FAMILY = "monospace"
private const val TEXT           = "abcdefghijklmnopqrstuvwxyz01234567890~!@#$%^&*()_+{}[]:\'\",./<>?\\|"

private enum class State { Pending, Found }

class FontDetectorImpl(
        private val timer       : Timer,
        private val textFactory : TextFactory,
        private val elementRuler: ElementRuler,
        private val scheduler   : Scheduler): FontDetector {

    private val fonts = HashMap<Int, State>()

    private fun getHash(family: String, size: Int, style: Set<Font.Style>) = arrayOf(family, size, style).contentHashCode()

    override fun invoke(info: FontInfo.() -> Unit, result: (Font) -> Unit) {
        FontInfo().apply(info).apply {
            val hash = getHash(family, size, style)

            when (fonts[hash]) {
                Found   -> result(FontImpl(size, weight, style, family))
                Pending -> {}
                else    -> scheduler.now {
                    val text = textFactory.create(TEXT, FontImpl(size, weight, style, "$family, $DEFAULT_FAMILY"))
                    val defaultSize = elementRuler.size(textFactory.create(TEXT, FontImpl(size, weight, style, DEFAULT_FAMILY)))

                    if (family == DEFAULT_FAMILY || family.isBlank()) {
                        result(FontImpl(size, weight, style, family))
                    } else {
                        check(timer.now,
                                text,
                                defaultSize,
                                success = { fonts[hash] = Found; result(FontImpl(size, weight, style, family)) },
                                failure = { println("Font search failed: ${this.family}") })
                    }
                }
            }
        }
    }

    override fun invoke(font: Font, info: FontInfo.() -> Unit, result: (Font) -> Unit) {
        invoke({
            size   = font.size
            style  = font.style
            weight = font.weight
            family = font.family
            apply(info)
        }, result)
    }

    private fun check(start: Measure<Time>, text: HTMLElement, defaultSize: Size, success: () -> Unit, failure: () -> Unit) {
        when {
            elementRuler.size(text) != defaultSize -> success()
            timer.now - start >= 1 * seconds       -> failure()
            else                                   -> scheduler.after(100 * milliseconds) {
                check(start, text, defaultSize, success, failure)
            }
        }
    }
}