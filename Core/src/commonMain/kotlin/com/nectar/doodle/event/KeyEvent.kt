package com.nectar.doodle.event

import com.nectar.doodle.core.View
import com.nectar.doodle.event.KeyState.Type
import com.nectar.doodle.system.SystemInputEvent.Modifier


inline class KeyCode(val value: Int)

class KeyState private constructor(
        val code     : KeyCode,
        val char     : Char,
        val modifiers: Set<Modifier>,
        val type     : Type) {

    enum class Type {
        Up, Down, Press
    }

    override fun hashCode(): Int {
        return code.value + char.toInt() + modifiers.hashCode() + type.ordinal
    }

    override fun equals(other: Any?): Boolean {
        if (this === other    ) return true
        if (other !is KeyState) return false

        if (code      != other.code     ) return false
        if (char      != other.char     ) return false
        if (modifiers != other.modifiers) return false
        if (type      != other.type     ) return false

        return true
    }

    companion object {
        operator fun invoke(
                keyCode  : KeyCode,
                keyChar  : Char,
                modifiers: Set<Modifier>,
                type     : Type): KeyState {
            val hashKey = keyCode.value + keyChar.toInt() + modifiers.hashCode() + type.ordinal

            return sHashValues.getOrPut(hashKey) {
                KeyState(keyCode, keyChar, modifiers, type)
            }
        }

        private val sHashValues = HashMap<Int, KeyState>(256)
    }
}

class KeyEvent(source: View, val code: KeyCode, val char: Char, modifiers: Set<Modifier>, val type: Type): InputEvent(source, modifiers) {

    constructor(source: View, keyState: KeyState): this(source, keyState.code, keyState.char, keyState.modifiers, keyState.type)

    companion object {

        // Key Codes

        val VK_BACKSPACE    = KeyCode(  8)
        val VK_TAB          = KeyCode(  9)

        val VK_RETURN       = KeyCode( 13)
        val VK_SHIFT        = KeyCode( 16)
        val VK_CONTROL      = KeyCode( 17)
        val VK_ALT          = KeyCode( 18)

        val VK_CAPSLOCK     = KeyCode( 20)

        val VK_ESCAPE       = KeyCode( 27)
        val VK_SPACE        = KeyCode( 32)
        val VK_PAGEUP       = KeyCode( 33)
        val VK_PAGEDOWN     = KeyCode( 34)
        val VK_END          = KeyCode( 35)
        val VK_HOME         = KeyCode( 36)
        val VK_LEFT         = KeyCode( 37)
        val VK_UP           = KeyCode( 38)
        val VK_RIGHT        = KeyCode( 39)
        val VK_DOWN         = KeyCode( 40)

        val VK_INSERT       = KeyCode( 45)
        val VK_DELETE       = KeyCode( 46)

        val VK_0            = KeyCode( 48)
        val VK_1            = KeyCode( 49)
        val VK_2            = KeyCode( 50)
        val VK_3            = KeyCode( 51)
        val VK_4            = KeyCode( 52)
        val VK_5            = KeyCode( 53)
        val VK_6            = KeyCode( 54)
        val VK_7            = KeyCode( 55)
        val VK_8            = KeyCode( 56)
        val VK_9            = KeyCode( 57)

        val VK_SEMICOLAN    = KeyCode( 59)

        val VK_EQUAL        = KeyCode( 61)

        val VK_A            = KeyCode( 65)
        val VK_B            = KeyCode( 66)
        val VK_C            = KeyCode( 67)
        val VK_D            = KeyCode( 68)
        val VK_E            = KeyCode( 69)
        val VK_F            = KeyCode( 70)
        val VK_G            = KeyCode( 71)
        val VK_H            = KeyCode( 72)
        val VK_I            = KeyCode( 73)
        val VK_J            = KeyCode( 74)
        val VK_K            = KeyCode( 75)
        val VK_L            = KeyCode( 76)
        val VK_M            = KeyCode( 77)
        val VK_N            = KeyCode( 78)
        val VK_O            = KeyCode( 79)
        val VK_P            = KeyCode( 80)
        val VK_Q            = KeyCode( 81)
        val VK_R            = KeyCode( 82)
        val VK_S            = KeyCode( 83)
        val VK_T            = KeyCode( 84)
        val VK_U            = KeyCode( 85)
        val VK_V            = KeyCode( 86)
        val VK_W            = KeyCode( 87)
        val VK_X            = KeyCode( 88)
        val VK_Y            = KeyCode( 89)
        val VK_Z            = KeyCode( 90)
        val VK_WINDOWS      = KeyCode( 91)
        val VK_CONTEXT      = KeyCode( 93)

        val VK_NUM_0        = KeyCode( 96)
        val VK_NUM_1        = KeyCode( 97)
        val VK_NUM_2        = KeyCode( 98)
        val VK_NUM_3        = KeyCode( 99)
        val VK_NUM_4        = KeyCode(100)
        val VK_NUM_5        = KeyCode(101)
        val VK_NUM_6        = KeyCode(102)
        val VK_NUM_7        = KeyCode(103)
        val VK_NUM_8        = KeyCode(104)
        val VK_NUM_9        = KeyCode(105)
        val VK_MULTIPLY     = KeyCode(106)
        val VK_PLUS         = KeyCode(107)
        val VK_MINUS        = KeyCode(109)
        val VK_DECIMAL      = KeyCode(110)
        val VK_DIVIDE       = KeyCode(111)
        val VK_F1           = KeyCode(112)
        val VK_F2           = KeyCode(113)
        val VK_F3           = KeyCode(114)
        val VK_F4           = KeyCode(115)
        val VK_F5           = KeyCode(116)
        val VK_F6           = KeyCode(117)
        val VK_F7           = KeyCode(118)
        val VK_F8           = KeyCode(119)
        val VK_F9           = KeyCode(120)
        val VK_F10          = KeyCode(121)
        val VK_F11          = KeyCode(122)
        val VK_F12          = KeyCode(123)
        val VK_NUMLOCK      = KeyCode(144)

        val VK_COMMA        = KeyCode(188)

        val VK_PERIOD       = KeyCode(190)
        val VK_FORWARD_SLASH= KeyCode(191)
        val VK_ACUTE        = KeyCode(192)

        val VK_LEFT_BRACKET = KeyCode(219)
        val VK_BACK_SLASH   = KeyCode(220)
        val VK_RIGHT_BRACKET= KeyCode(221)
        val VK_APOSTROPHE   = KeyCode(222)
    }
}
