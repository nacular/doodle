package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.system.SystemInputEvent.Modifier


class KeyEvent(source: Gizmo, val keyCode: Short, val keyChar: Char, modifiers: Set<Modifier>, val type: Type): InputEvent(source, modifiers) {

    enum class Type {
        UP, DOWN, PRESS
    }

    companion object {

        // Key Codes

        val VK_BACKSPACE    : Short = 8
        val VK_TAB          : Short = 9

        val VK_RETURN       : Short = 13
        val VK_SHIFT        : Short = 16
        val VK_CONTROL      : Short = 17
        val VK_ALT          : Short = 18

        val VK_CAPSLOCK     : Short = 20

        val VK_ESCAPE       : Short = 27
        val VK_SPACE        : Short = 32
        val VK_PAGEUP       : Short = 33
        val VK_PAGEDOWN     : Short = 34
        val VK_END          : Short = 35
        val VK_HOME         : Short = 36
        val VK_LEFT         : Short = 37
        val VK_UP           : Short = 38
        val VK_RIGHT        : Short = 39
        val VK_DOWN         : Short = 40

        val VK_INSERT       : Short = 45
        val VK_DELETE       : Short = 46

        val VK_0            : Short = 48
        val VK_1            : Short = 49
        val VK_2            : Short = 50
        val VK_3            : Short = 51
        val VK_4            : Short = 52
        val VK_5            : Short = 53
        val VK_6            : Short = 54
        val VK_7            : Short = 55
        val VK_8            : Short = 56
        val VK_9            : Short = 57

        val VK_SEMICOLAN    : Short = 59

        val VK_EQUAL        : Short = 61

        val VK_A            : Short = 65
        val VK_B            : Short = 66
        val VK_C            : Short = 67
        val VK_D            : Short = 68
        val VK_E            : Short = 69
        val VK_F            : Short = 70
        val VK_G            : Short = 71
        val VK_H            : Short = 72
        val VK_I            : Short = 73
        val VK_J            : Short = 74
        val VK_K            : Short = 75
        val VK_L            : Short = 76
        val VK_M            : Short = 77
        val VK_N            : Short = 78
        val VK_O            : Short = 79
        val VK_P            : Short = 80
        val VK_Q            : Short = 81
        val VK_R            : Short = 82
        val VK_S            : Short = 83
        val VK_T            : Short = 84
        val VK_U            : Short = 85
        val VK_V            : Short = 86
        val VK_W            : Short = 87
        val VK_X            : Short = 88
        val VK_Y            : Short = 89
        val VK_Z            : Short = 90
        val VK_WINDOWS      : Short = 91
        val VK_CONTEXT      : Short = 93

        val VK_NUM_0        : Short = 96
        val VK_NUM_1        : Short = 97
        val VK_NUM_2        : Short = 98
        val VK_NUM_3        : Short = 99
        val VK_NUM_4        : Short = 100
        val VK_NUM_5        : Short = 101
        val VK_NUM_6        : Short = 102
        val VK_NUM_7        : Short = 103
        val VK_NUM_8        : Short = 104
        val VK_NUM_9        : Short = 105
        val VK_MULTIPLY     : Short = 106
        val VK_PLUS         : Short = 107
        val VK_MINUS        : Short = 109
        val VK_DECIMAL      : Short = 110
        val VK_DIVIDE       : Short = 111
        val VK_F1           : Short = 112
        val VK_F2           : Short = 113
        val VK_F3           : Short = 114
        val VK_F4           : Short = 115
        val VK_F5           : Short = 116
        val VK_F6           : Short = 117
        val VK_F7           : Short = 118
        val VK_F8           : Short = 119
        val VK_F9           : Short = 120
        val VK_F10          : Short = 121
        val VK_F11          : Short = 122
        val VK_F12          : Short = 123
        val VK_NUMLOCK      : Short = 144

        val VK_COMMA        : Short = 188

        val VK_PERIOD       : Short = 190
        val VK_FORWARD_SLASH: Short = 191
        val VK_ACUTE        : Short = 192

        val VK_LEFT_BRACKET : Short = 219
        val VK_BACK_SLASH   : Short = 220
        val VK_RIGHT_BRACKET: Short = 221
        val VK_APOSTROPHE   : Short = 222
    }
}
