@file:Suppress("FunctionName")

package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Cyan
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.Color.Companion.Yellow
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/19/18.
 */
class ColorTests {
    @Test
    fun `red != blue`() = expect(false) { Red == Blue }

    @Test
    fun `invalid hex fails`() {
        assertFailsWith(IllegalArgumentException::class) { Color(0xfffffffu) }
    }

    @Test
    fun `invalid opacity fails`() {
        assertFailsWith(IllegalArgumentException::class) { Color(0xffu, 3f) }
    }

    @Test
    fun `transparent invisible`() = listOf(Color(0xaaaaaau, 0f), Color(10u, 10u, 10u, 0f), Transparent).forEach { expect(false) { it.visible } }

    @Test
    fun `inversion works`() = listOf(White to Black, Red to Cyan, Yellow to Blue, Green to Color(0xff00ffu)).forEach {
        expect(it.first ) { it.second.inverted }
        expect(it.second) { it.first.inverted  }
    }

    @Test
    fun `hexString correct`() = listOf(Red to "FF0000", Green to "00FF00", Blue to "0000FF").forEach {
        expect(it.second) { it.first.hexString }
    }

    @Test
    fun `rgb to hsl`() = listOf(Color(0xccccccu) to HslColor(0 * degrees, 0f, 0.8f)).forEach {
        expect(it.second, "${it.first} -> ${it.second}") { HslColor(it.first) }
    }

    @Test
    fun `hsl to rgb`() = listOf(Color(0xccccccu) to HslColor(0 * degrees, 0f, 0.8f)).forEach {
        expect(it.first, "${it.second} -> ${it.first}") { it.second.toRgb() }
    }

//    @Test
//    fun `rgb to hsv`() = listOf(Color(0xf2ffe6u) to HsvColor(90 * degrees, 0.1f, 1f)).forEach {
//        expect(it.second, "${it.first} -> ${it.second}") { HsvColor(it.first) }
//    }
//
//    @Test
//    fun `hsv to rgb`() = listOf(Color(0xf2ffe6u) to HsvColor(190 * degrees, 0.1f, 1f)).forEach {
//        expect(it.first, "${it.second} -> ${it.first}") { it.second.toRgb() }
//    }

//    @Test
//    fun `lighter`() = listOf(red, green, blue, Color(0x454547), white, black).forEach {
//        it.lighter().also { lighter ->
//            expect(true, "${lighter.red  } >= ${it.red  }") { lighter.red   >= it.red   }
//            expect(true, "${lighter.green} >= ${it.green}") { lighter.green >= it.green }
//            expect(true, "${lighter.blue } >= ${it.blue }") { lighter.blue  >= it.blue  }
//        }
//    }
//
//    @Test
//    fun `darker`() = listOf(red, green, blue, Color(0x454547), white, black).forEach {
//        it.darker().also { darker ->
//            expect(true) { darker.red   <= it.red   }
//            expect(true) { darker.green <= it.green }
//            expect(true) { darker.blue  <= it.blue  }
//        }
//    }
}