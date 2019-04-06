package com.nectar.doodle.event

import com.nectar.doodle.core.View
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


class KeyEvent(source: View, val code: Int, val char: Char, modifiers: Set<Modifier>, val type: Type): InputEvent(source, modifiers) {

    constructor(source: View, keyState: KeyState): this(source, keyState.code, keyState.char, keyState.modifiers, keyState.type)

    companion object {

        // Key Codes

        const val VK_BACKSPACE    : Int =   8
        const val VK_TAB          : Int =   9

        const val VK_RETURN       : Int =  13
        const val VK_SHIFT        : Int =  16
        const val VK_CONTROL      : Int =  17
        const val VK_ALT          : Int =  18

        const val VK_CAPSLOCK     : Int =  20

        const val VK_ESCAPE       : Int =  27
        const val VK_SPACE        : Int =  32
        const val VK_PAGEUP       : Int =  33
        const val VK_PAGEDOWN     : Int =  34
        const val VK_END          : Int =  35
        const val VK_HOME         : Int =  36
        const val VK_LEFT         : Int =  37
        const val VK_UP           : Int =  38
        const val VK_RIGHT        : Int =  39
        const val VK_DOWN         : Int =  40

        const val VK_INSERT       : Int =  45
        const val VK_DELETE       : Int =  46

        const val VK_0            : Int =  48
        const val VK_1            : Int =  49
        const val VK_2            : Int =  50
        const val VK_3            : Int =  51
        const val VK_4            : Int =  52
        const val VK_5            : Int =  53
        const val VK_6            : Int =  54
        const val VK_7            : Int =  55
        const val VK_8            : Int =  56
        const val VK_9            : Int =  57

        const val VK_SEMICOLAN    : Int =  59

        const val VK_EQUAL        : Int =  61

        const val VK_A            : Int =  65
        const val VK_B            : Int =  66
        const val VK_C            : Int =  67
        const val VK_D            : Int =  68
        const val VK_E            : Int =  69
        const val VK_F            : Int =  70
        const val VK_G            : Int =  71
        const val VK_H            : Int =  72
        const val VK_I            : Int =  73
        const val VK_J            : Int =  74
        const val VK_K            : Int =  75
        const val VK_L            : Int =  76
        const val VK_M            : Int =  77
        const val VK_N            : Int =  78
        const val VK_O            : Int =  79
        const val VK_P            : Int =  80
        const val VK_Q            : Int =  81
        const val VK_R            : Int =  82
        const val VK_S            : Int =  83
        const val VK_T            : Int =  84
        const val VK_U            : Int =  85
        const val VK_V            : Int =  86
        const val VK_W            : Int =  87
        const val VK_X            : Int =  88
        const val VK_Y            : Int =  89
        const val VK_Z            : Int =  90
        const val VK_WINDOWS      : Int =  91
        const val VK_CONTEXT      : Int =  93

        const val VK_NUM_0        : Int =  96
        const val VK_NUM_1        : Int =  97
        const val VK_NUM_2        : Int =  98
        const val VK_NUM_3        : Int =  99
        const val VK_NUM_4        : Int = 100
        const val VK_NUM_5        : Int = 101
        const val VK_NUM_6        : Int = 102
        const val VK_NUM_7        : Int = 103
        const val VK_NUM_8        : Int = 104
        const val VK_NUM_9        : Int = 105
        const val VK_MULTIPLY     : Int = 106
        const val VK_PLUS         : Int = 107
        const val VK_MINUS        : Int = 109
        const val VK_DECIMAL      : Int = 110
        const val VK_DIVIDE       : Int = 111
        const val VK_F1           : Int = 112
        const val VK_F2           : Int = 113
        const val VK_F3           : Int = 114
        const val VK_F4           : Int = 115
        const val VK_F5           : Int = 116
        const val VK_F6           : Int = 117
        const val VK_F7           : Int = 118
        const val VK_F8           : Int = 119
        const val VK_F9           : Int = 120
        const val VK_F10          : Int = 121
        const val VK_F11          : Int = 122
        const val VK_F12          : Int = 123
        const val VK_NUMLOCK      : Int = 144

        const val VK_COMMA        : Int = 188

        const val VK_PERIOD       : Int = 190
        const val VK_FORWARD_SLASH: Int = 191
        const val VK_ACUTE        : Int = 192

        const val VK_LEFT_BRACKET : Int = 219
        const val VK_BACK_SLASH   : Int = 220
        const val VK_RIGHT_BRACKET: Int = 221
        const val VK_APOSTROPHE   : Int = 222
    }
}
