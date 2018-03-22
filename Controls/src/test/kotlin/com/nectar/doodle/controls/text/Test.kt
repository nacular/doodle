//package com.nectar.doodle.controls.text
//
//import com.nectar.doodle.drawing.FontFactoryImpl
//import com.nectar.doodle.drawing.TextMetrics
//import com.nectar.doodle.geometry.Size
//import com.nectar.doodle.text.StyledText
//import com.nectar.doodle.text.invoke
//import com.nectar.doodle.text.rangeTo
//import mockito.`when`
//import mockito.anything
//import mockito.instance
//import mockito.mock
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//
//class LabelTests {
//    @Test
//    fun setText() {
//        Label(createTextMetrics()).let { label ->
//            "foo bar some simple text".let {
//                label.text = it
//
//                assertEquals(it, label.text)
//            }
//        }
//    }
//
//    @Test
//    fun setStyledText() {
//        Label(createTextMetrics()).let { label ->
//            styledText().let {
//                label.styledText = it
//
//                assertEquals(it.text, label.text)
//                assertEquals(it, label.styledText)
//            }
//        }
//    }
//
//    @Test
//    fun setsSizeToTextSize() {
//        val textSize = Size(100.0, 345.0)
//
//        Label(createTextMetrics(textSize)).let {
//            it.styledText = styledText()
//
//            assertEquals(textSize, it.size)
//        }
//    }
//
//    @Test
//    fun keepsSizeToTextSize() {
//        val textSize = Size(100.0, 345.0)
//
//        Label(createTextMetrics(textSize)).let {
//            it.styledText = styledText()
//            it.size       = Size.Empty
//
//            assertEquals(textSize, it.size)
//        }
//    }
//
//    private fun styledText(): StyledText {
//        val fontFactory = FontFactoryImpl()
//        val font        = fontFactory { families += "verdana"; size = 24 }
//
//        return "foo bar "..font("some simple").." text"
//    }
//
//    private fun createTextMetrics(size: Size = Size.Empty): TextMetrics {
//        val textMetrics: TextMetrics = mock(TextMetrics::class.js)
//
//        `when`(textMetrics.size(anything())).thenReturn(size)
//
//        return instance(textMetrics)
//    }
//}