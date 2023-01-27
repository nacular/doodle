package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.drawing.Font.Style.Oblique
import io.nacular.doodle.drawing.FontInfo
import io.nacular.doodle.drawing.FontLoader
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontSlant.OBLIQUE
import org.jetbrains.skia.FontSlant.UPRIGHT
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.TypefaceFontProvider

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
internal class FontLoaderImpl(private val fontCollection: FontCollection): FontLoader {
    private val loadedFonts = mutableMapOf<FontInfo, Font>()

    private val typefaceFontProvider = TypefaceFontProvider()

    init {
        fontCollection.setAssetFontManager(typefaceFontProvider)
    }

    override suspend fun invoke(info: FontInfo.() -> Unit): Font? = FontInfo().apply(info).let { info ->
        var result = loadedFonts[info]

        if (result == null) {
            val slant = when (info.style) {
                Italic     -> OBLIQUE
                is Oblique -> OBLIQUE
                else       -> UPRIGHT
            }

            result = fontCollection.findTypefaces(info.families.toTypedArray(), FontStyle(info.weight, 5, slant)).firstOrNull()?.toFont(info)?.also {
                loadedFonts[info] = it
            }
        }

        return result
    }

    override suspend fun invoke(source: String, info: FontInfo.() -> Unit): Font? = FontInfo().apply(info).let { modifiedInfo ->
        loadedFonts[modifiedInfo] ?: Thread.currentThread().contextClassLoader.getResourceAsStream(source)?.let { file ->
            val typeface = Typeface.makeFromData(Data.makeFromBytes(file.readBytes()))
            typefaceFontProvider.registerTypeface(typeface, modifiedInfo.family)

            typeface.toFont(modifiedInfo).also { loadedFonts[modifiedInfo] = it }
        }
    }

    private val Font.Style.skia get() = when (this) {
        Italic     -> OBLIQUE
        is Oblique -> OBLIQUE
        else       -> UPRIGHT
    }

    private fun Typeface.toFont(info: FontInfo): Font = FontImpl(TextStyle().also {
        it.fontSize     = info.size.toFloat()
        it.typeface     = this
        it.fontStyle    = fontStyle.withWeight(info.weight).withSlant(info.style.skia)
        it.fontFamilies = info.families.toTypedArray()
        it.baselineMode = BaselineMode.IDEOGRAPHIC
    })
}