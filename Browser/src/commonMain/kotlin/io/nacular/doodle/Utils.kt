package io.nacular.doodle

import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.defaultFontFamily
import io.nacular.doodle.dom.defaultFontSize
import io.nacular.doodle.dom.defaultFontWeight
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontInfo
import io.nacular.doodle.utils.LeastRecentlyUsedCache

internal interface FontSerializer {
    operator fun invoke(font: Font?   ): String
    operator fun invoke(info: FontInfo): String
}

internal class FontSerializerImpl(htmlFactory: HtmlFactory, cacheLength: Int = 10): FontSerializer {
    private val element         = htmlFactory.create<HTMLElement>()
    private val serializedFonts = LeastRecentlyUsedCache<Font?, String> (maxSize = cacheLength)

    override fun invoke(font: Font?): String = serializedFonts.getOrPut(font) {
        when {
            font != null -> element.run {
                style.setFont(font)

                style.run { "$fontStyle $fontVariant $fontWeight $fontSize $fontFamily" }
            }
            else -> "$defaultFontWeight ${defaultFontSize}px $defaultFontFamily"
        }
    }

    override fun invoke(info: FontInfo) = invoke(object: Font {
        override val size   get() = info.size
        override val style  get() = info.style
        override val weight get() = info.weight
        override val family get() = info.families.joinToString()
    })
}
