package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.event.KeyState.Type
import com.nectar.doodle.system.SystemInputEvent.Modifier

class KeyState private constructor(
        val code     : Int,
        val char     : Char,
        val modifiers: Set<Modifier>,
        val type     : Type) {

    enum class Type {
        Up, Down, Press
    }

    override fun hashCode(): Int {
        return code + char.toInt() + modifiers.hashCode() + type.ordinal
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
                keyCode  : Int,
                keyChar  : Char,
                modifiers: Set<Modifier>,
                type     : Type): KeyState {
            val hashKey = keyCode + keyChar.toInt() + modifiers.hashCode() + type.ordinal

            return sHashValues.getOrPut(hashKey) {
                KeyState(keyCode, keyChar, modifiers, type)
            }
        }

        private val sHashValues = HashMap<Int, KeyState>(256)
    }
}


class KeyEvent(source: Gizmo, val code: Int, val char: Char, modifiers: Set<Modifier>, val type: Type): InputEvent(source, modifiers) {

    constructor(source: Gizmo, keyState: KeyState): this(source, keyState.code, keyState.char, keyState.modifiers, keyState.type)

    companion object {

        // Key Codes

        val VK_BACKSPACE    : Int = 8
        val VK_TAB          : Int = 9

        val VK_RETURN       : Int = 13
        val VK_SHIFT        : Int = 16
        val VK_CONTROL      : Int = 17
        val VK_ALT          : Int = 18

        val VK_CAPSLOCK     : Int = 20

        val VK_ESCAPE       : Int = 27
        val VK_SPACE        : Int = 32
        val VK_PAGEUP       : Int = 33
        val VK_PAGEDOWN     : Int = 34
        val VK_END          : Int = 35
        val VK_HOME         : Int = 36
        val VK_LEFT         : Int = 37
        val VK_UP           : Int = 38
        val VK_RIGHT        : Int = 39
        val VK_DOWN         : Int = 40

        val VK_INSERT       : Int = 45
        val VK_DELETE       : Int = 46

        val VK_0            : Int = 48
        val VK_1            : Int = 49
        val VK_2            : Int = 50
        val VK_3            : Int = 51
        val VK_4            : Int = 52
        val VK_5            : Int = 53
        val VK_6            : Int = 54
        val VK_7            : Int = 55
        val VK_8            : Int = 56
        val VK_9            : Int = 57

        val VK_SEMICOLAN    : Int = 59

        val VK_EQUAL        : Int = 61

        val VK_A            : Int = 65
        val VK_B            : Int = 66
        val VK_C            : Int = 67
        val VK_D            : Int = 68
        val VK_E            : Int = 69
        val VK_F            : Int = 70
        val VK_G            : Int = 71
        val VK_H            : Int = 72
        val VK_I            : Int = 73
        val VK_J            : Int = 74
        val VK_K            : Int = 75
        val VK_L            : Int = 76
        val VK_M            : Int = 77
        val VK_N            : Int = 78
        val VK_O            : Int = 79
        val VK_P            : Int = 80
        val VK_Q            : Int = 81
        val VK_R            : Int = 82
        val VK_S            : Int = 83
        val VK_T            : Int = 84
        val VK_U            : Int = 85
        val VK_V            : Int = 86
        val VK_W            : Int = 87
        val VK_X            : Int = 88
        val VK_Y            : Int = 89
        val VK_Z            : Int = 90
        val VK_WINDOWS      : Int = 91
        val VK_CONTEXT      : Int = 93

        val VK_NUM_0        : Int = 96
        val VK_NUM_1        : Int = 97
        val VK_NUM_2        : Int = 98
        val VK_NUM_3        : Int = 99
        val VK_NUM_4        : Int = 100
        val VK_NUM_5        : Int = 101
        val VK_NUM_6        : Int = 102
        val VK_NUM_7        : Int = 103
        val VK_NUM_8        : Int = 104
        val VK_NUM_9        : Int = 105
        val VK_MULTIPLY     : Int = 106
        val VK_PLUS         : Int = 107
        val VK_MINUS        : Int = 109
        val VK_DECIMAL      : Int = 110
        val VK_DIVIDE       : Int = 111
        val VK_F1           : Int = 112
        val VK_F2           : Int = 113
        val VK_F3           : Int = 114
        val VK_F4           : Int = 115
        val VK_F5           : Int = 116
        val VK_F6           : Int = 117
        val VK_F7           : Int = 118
        val VK_F8           : Int = 119
        val VK_F9           : Int = 120
        val VK_F10          : Int = 121
        val VK_F11          : Int = 122
        val VK_F12          : Int = 123
        val VK_NUMLOCK      : Int = 144

        val VK_COMMA        : Int = 188

        val VK_PERIOD       : Int = 190
        val VK_FORWARD_SLASH: Int = 191
        val VK_ACUTE        : Int = 192

        val VK_LEFT_BRACKET : Int = 219
        val VK_BACK_SLASH   : Int = 220
        val VK_RIGHT_BRACKET: Int = 221
        val VK_APOSTROPHE   : Int = 222
    }
}
