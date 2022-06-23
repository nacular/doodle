package io.nacular.doodle.drawing.impl

import io.nacular.doodle.Document
import io.nacular.doodle.FontSerializer
import io.nacular.doodle.dom.styleText
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.drawing.FontInfo
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.jsObject
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

private external class FontFace(family: String, source: String, descriptors: dynamic) {
    fun load(): Promise<FontFace>
    val family: String
    val weight: String
    val style : String
}

private external interface FontFaceSet {
    val ready: Promise<dynamic>
    fun add(value: dynamic)
    fun load(font: String): Promise<Array<FontFace>>
}

internal class FontLoaderImpl(private val document: Document, private val fontSerializer: FontSerializer): FontLoader {
    override suspend fun invoke(source: String, info: FontInfo.() -> Unit): Font = FontInfo().apply(info).let {
        val loadedFont = FontFace(it.families.joinToString(), "url($source)", jsObject {
            family = it.families
            size   = it.size
            style  = it.style.styleText
            weight = "${it.weight}"
        }).load().await()

        document.fonts.add(loadedFont)

        document.fonts.ready.await()

        loadedFont.toFont(it.size)
    }

    override suspend operator fun invoke(info: FontInfo.() -> Unit): Font = suspendCoroutine { cont ->
        FontInfo().apply(info).let { info ->
            document.fonts.load(fontSerializer(info)).then({
                it.firstOrNull()?.let {
                    cont.resume(it.toFont(info.size))
                }
            }, {
                cont.resumeWithException(it)
            })
        }
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private inline val Document.fonts: FontFaceSet get() = asDynamic().fonts as FontFaceSet

    private suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
        then({ cont.resume(it) }, { cont.resumeWithException(it) })
    }

    private fun FontFace.toFont(size: Int): Font {
        val w = when (weight.lowercase()) {
            "bold"   -> 700
            "normal" -> 400
            else     -> weight.toInt()
        }

        val lowerCaseStyle = style.lowercase()

        val s = when {
            lowerCaseStyle == "italic"           -> Italic
            lowerCaseStyle.startsWith("oblique") -> {
                val angle = Regex("oblique ([0-9]+)deg").find(lowerCaseStyle)?.groupValues?.get(0)?.toDouble()?.times(degrees)
                Font.Style.Oblique(angle)
            }
            else                                 -> Font.Style.Normal
        }

        return FontImpl(size, w, s, family)
    }
}