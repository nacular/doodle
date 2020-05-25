package com.nectar.doodle.event

import com.nectar.doodle.core.View
import com.nectar.doodle.event.KeyState.Type
import com.nectar.doodle.system.SystemInputEvent.Modifier


inline class KeyCode(val key: String)

class KeyState private constructor(
        val code     : KeyCode,
        val key      : String,
        val modifiers: Set<Modifier>,
        val type     : Type) {

    enum class Type {
        Up, Down
    }

    override fun hashCode(): Int {
        return code.key.hashCode() + key.hashCode() + modifiers.hashCode() + type.ordinal
    }

    override fun equals(other: Any?): Boolean {
        if (this === other    ) return true
        if (other !is KeyState) return false

        if (code      != other.code     ) return false
        if (key       != other.key      ) return false
        if (modifiers != other.modifiers) return false
        if (type      != other.type     ) return false

        return true
    }

    companion object {
        operator fun invoke(
                code     : KeyCode,
                key      : String,
                modifiers: Set<Modifier>,
                type     : Type): KeyState {
            val hashKey = code.key.hashCode() + key.hashCode() + modifiers.hashCode() + type.ordinal

            return sHashValues.getOrPut(hashKey) {
                KeyState(code, key, modifiers, type)
            }
        }

        private val sHashValues = HashMap<Int, KeyState>(256)
    }
}

class KeyEvent(source: View, val code: KeyCode, val key: String, modifiers: Set<Modifier>, val type: Type): InputEvent(source, modifiers) {

    constructor(source: View, keyState: KeyState): this(source, keyState.code, keyState.key, keyState.modifiers, keyState.type)

    companion object {

        // Key Codes

        val Backquote     = KeyCode("Backquote")
        val Backslash     = KeyCode("Backslash")
        val BracketLeft   = KeyCode("BracketLeft")
        val BracketRight  = KeyCode("BracketRight")
        val Comma         = KeyCode("Comma")
        val Digit0        = KeyCode("Digit0")
        val Digit1        = KeyCode("Digit1")
        val Digit2        = KeyCode("Digit2")
        val Digit3        = KeyCode("Digit3")
        val Digit4        = KeyCode("Digit4")
        val Digit5        = KeyCode("Digit5")
        val Digit6        = KeyCode("Digit6")
        val Digit7        = KeyCode("Digit7")
        val Digit8        = KeyCode("Digit8")
        val Digit9        = KeyCode("Digit9")
        val Equal         = KeyCode("Equal")
        val IntlBackslash = KeyCode("IntlBackslash")
        val IntlRo        = KeyCode("IntlRo")
        val IntlYen       = KeyCode("IntlYen")
        val KeyA          = KeyCode("KeyA")
        val KeyB          = KeyCode("KeyB")
        val KeyC          = KeyCode("KeyC")
        val KeyD          = KeyCode("KeyD")
        val KeyE          = KeyCode("KeyE")
        val KeyF          = KeyCode("KeyF")
        val KeyG          = KeyCode("KeyG")
        val KeyH          = KeyCode("KeyH")
        val KeyI          = KeyCode("KeyI")
        val KeyJ          = KeyCode("KeyJ")
        val KeyK          = KeyCode("KeyK")
        val KeyL          = KeyCode("KeyL")
        val KeyM          = KeyCode("KeyM")
        val KeyN          = KeyCode("KeyN")
        val KeyO          = KeyCode("KeyO")
        val KeyP          = KeyCode("KeyP")
        val KeyQ          = KeyCode("KeyQ")
        val KeyR          = KeyCode("KeyR")
        val KeyS          = KeyCode("KeyS")
        val KeyT          = KeyCode("KeyT")
        val KeyU          = KeyCode("KeyU")
        val KeyV          = KeyCode("KeyV")
        val KeyW          = KeyCode("KeyW")
        val KeyX          = KeyCode("KeyX")
        val KeyY          = KeyCode("KeyY")
        val KeyZ          = KeyCode("KeyZ")
        val Minus         = KeyCode("Minus")
        val Period        = KeyCode("Period")
        val Quote         = KeyCode("Quote")
        val Semicolon     = KeyCode("Semicolon")
        val Slash         = KeyCode("Slash")

        val AltLeft       = KeyCode("AltLeft")
        val AltRight      = KeyCode("AltRight")
        val Backspace     = KeyCode("Backspace")
        val CapsLock      = KeyCode("CapsLock")
        val ContextMenu   = KeyCode("ContextMenu")
        val ControlLeft   = KeyCode("ControlLeft")
        val ControlRight  = KeyCode("ControlRight")
        val Enter         = KeyCode("Enter")
        val MetaLeft      = KeyCode("MetaLeft")
        val MetaRight     = KeyCode("MetaRight")
        val ShiftLeft     = KeyCode("ShiftLeft")
        val ShiftRight    = KeyCode("ShiftRight")
        val Space         = KeyCode("Space")
        val Tab           = KeyCode("Tab")

        val Convert              = KeyCode("Convert")
        val KanaMode             = KeyCode("KanaMode")
        val Lang1                = KeyCode("Lang1")
        val Lang2                = KeyCode("Lang2")
        val Lang3                = KeyCode("Lang3")
        val Lang4                = KeyCode("Lang4")
        val Lang5                = KeyCode("Lang5")
        val NonConvert           = KeyCode("NonConvert")

        val Delete               = KeyCode("Delete")
        val End                  = KeyCode("End")
        val Help                 = KeyCode("Help")
        val Home                 = KeyCode("Home")
        val Insert               = KeyCode("Insert")
        val PageDown             = KeyCode("PageDown")
        val PageUp               = KeyCode("PageUp")

        val ArrowDown            = KeyCode("ArrowDown")
        val ArrowLeft            = KeyCode("ArrowLeft")
        val ArrowRight           = KeyCode("ArrowRight")
        val ArrowUp              = KeyCode("ArrowUp")

        val NumLock              = KeyCode("NumLock")
        val Numpad0              = KeyCode("Numpad0")
        val Numpad1              = KeyCode("Numpad1")
        val Numpad2              = KeyCode("Numpad2")
        val Numpad3              = KeyCode("Numpad3")
        val Numpad4              = KeyCode("Numpad4")
        val Numpad5              = KeyCode("Numpad5")
        val Numpad6              = KeyCode("Numpad6")
        val Numpad7              = KeyCode("Numpad7")
        val Numpad8              = KeyCode("Numpad8")
        val Numpad9              = KeyCode("Numpad9")
        val NumpadAdd            = KeyCode("NumpadAdd")
        val NumpadBackspace      = KeyCode("NumpadBackspace")
        val NumpadClear          = KeyCode("NumpadClear")
        val NumpadClearEntry     = KeyCode("NumpadClearEntry")
        val NumpadComma          = KeyCode("NumpadComma")
        val NumpadDecimal        = KeyCode("NumpadDecimal")
        val NumpadDivide         = KeyCode("NumpadDivide")
        val NumpadEnter          = KeyCode("NumpadEnter")
        val NumpadEqual          = KeyCode("NumpadEqual")
        val NumpadHash           = KeyCode("NumpadHash")
        val NumpadMemoryAdd      = KeyCode("NumpadMemoryAdd")
        val NumpadMemoryClear    = KeyCode("NumpadMemoryClear")
        val NumpadMemoryRecall   = KeyCode("NumpadMemoryRecall")
        val NumpadMemoryStore    = KeyCode("NumpadMemoryStore")
        val NumpadMemorySubtract = KeyCode("NumpadMemorySubtract")
        val NumpadMultiply       = KeyCode("NumpadMultiply")
        val NumpadParenLeft      = KeyCode("NumpadParenLeft")
        val NumpadParenRight     = KeyCode("NumpadParenRight")
        val NumpadStar           = KeyCode("NumpadStar")
        val NumpadSubtract       = KeyCode("NumpadSubtract")

        val Escape               = KeyCode("Escape")
        val F1                   = KeyCode("F1")
        val F2                   = KeyCode("F2")
        val F3                   = KeyCode("F3")
        val F4                   = KeyCode("F4")
        val F5                   = KeyCode("F5")
        val F6                   = KeyCode("F6")
        val F7                   = KeyCode("F7")
        val F8                   = KeyCode("F8")
        val F9                   = KeyCode("F9")
        val F10                  = KeyCode("F10")
        val F11                  = KeyCode("F11")
        val F12                  = KeyCode("F12")
        val Fn                   = KeyCode("Fn")
        val FnLock               = KeyCode("FnLock")
        val PrintScreen          = KeyCode("PrintScreen")
        val ScrollLock           = KeyCode("ScrollLock")
        val Pause                = KeyCode("Pause")

//        val VK_BACKSPACE    = KeyCode(  8)
//        val VK_TAB          = KeyCode(  9)
//
//        val VK_RETURN       = KeyCode( 13)
//        val VK_SHIFT        = KeyCode( 16)
//        val VK_CONTROL      = KeyCode( 17)
//        val VK_ALT          = KeyCode( 18)
//
//        val VK_CAPSLOCK     = KeyCode( 20)
//
//        val VK_ESCAPE       = KeyCode( 27)
//        val VK_SPACE        = KeyCode( 32)
//        val VK_PAGEUP       = KeyCode( 33)
//        val VK_PAGEDOWN     = KeyCode( 34)
//        val VK_END          = KeyCode( 35)
//        val VK_HOME         = KeyCode( 36)
//        val VK_LEFT         = KeyCode( 37)
//        val VK_UP           = KeyCode( 38)
//        val VK_RIGHT        = KeyCode( 39)
//        val VK_DOWN         = KeyCode( 40)
//
//        val VK_INSERT       = KeyCode( 45)
//        val VK_DELETE       = KeyCode( 46)
//
//        val VK_0            = KeyCode( 48)
//        val VK_1            = KeyCode( 49)
//        val VK_2            = KeyCode( 50)
//        val VK_3            = KeyCode( 51)
//        val VK_4            = KeyCode( 52)
//        val VK_5            = KeyCode( 53)
//        val VK_6            = KeyCode( 54)
//        val VK_7            = KeyCode( 55)
//        val VK_8            = KeyCode( 56)
//        val VK_9            = KeyCode( 57)
//
//        val VK_SEMICOLAN    = KeyCode( 59)
//
//        val VK_EQUAL        = KeyCode( 61)
//
//        val VK_A            = KeyCode( 65)
//        val VK_B            = KeyCode( 66)
//        val VK_C            = KeyCode( 67)
//        val VK_D            = KeyCode( 68)
//        val VK_E            = KeyCode( 69)
//        val VK_F            = KeyCode( 70)
//        val VK_G            = KeyCode( 71)
//        val VK_H            = KeyCode( 72)
//        val VK_I            = KeyCode( 73)
//        val VK_J            = KeyCode( 74)
//        val VK_K            = KeyCode( 75)
//        val VK_L            = KeyCode( 76)
//        val VK_M            = KeyCode( 77)
//        val VK_N            = KeyCode( 78)
//        val VK_O            = KeyCode( 79)
//        val VK_P            = KeyCode( 80)
//        val VK_Q            = KeyCode( 81)
//        val VK_R            = KeyCode( 82)
//        val VK_S            = KeyCode( 83)
//        val VK_T            = KeyCode( 84)
//        val VK_U            = KeyCode( 85)
//        val VK_V            = KeyCode( 86)
//        val VK_W            = KeyCode( 87)
//        val VK_X            = KeyCode( 88)
//        val VK_Y            = KeyCode( 89)
//        val VK_Z            = KeyCode( 90)
//        val VK_WINDOWS      = KeyCode( 91)
//        val VK_CONTEXT      = KeyCode( 93)
//
//        val VK_NUM_0        = KeyCode( 96)
//        val VK_NUM_1        = KeyCode( 97)
//        val VK_NUM_2        = KeyCode( 98)
//        val VK_NUM_3        = KeyCode( 99)
//        val VK_NUM_4        = KeyCode(100)
//        val VK_NUM_5        = KeyCode(101)
//        val VK_NUM_6        = KeyCode(102)
//        val VK_NUM_7        = KeyCode(103)
//        val VK_NUM_8        = KeyCode(104)
//        val VK_NUM_9        = KeyCode(105)
//        val VK_MULTIPLY     = KeyCode(106)
//        val VK_PLUS         = KeyCode(107)
//        val VK_MINUS        = KeyCode(109)
//        val VK_DECIMAL      = KeyCode(110)
//        val VK_DIVIDE       = KeyCode(111)
//        val VK_F1           = KeyCode(112)
//        val VK_F2           = KeyCode(113)
//        val VK_F3           = KeyCode(114)
//        val VK_F4           = KeyCode(115)
//        val VK_F5           = KeyCode(116)
//        val VK_F6           = KeyCode(117)
//        val VK_F7           = KeyCode(118)
//        val VK_F8           = KeyCode(119)
//        val VK_F9           = KeyCode(120)
//        val VK_F10          = KeyCode(121)
//        val VK_F11          = KeyCode(122)
//        val VK_F12          = KeyCode(123)
//        val VK_NUMLOCK      = KeyCode(144)
//
//        val VK_COMMA        = KeyCode(188)
//
//        val VK_PERIOD       = KeyCode(190)
//        val VK_FORWARD_SLASH= KeyCode(191)
//        val VK_ACUTE        = KeyCode(192)
//
//        val VK_LEFT_BRACKET = KeyCode(219)
//        val VK_BACK_SLASH   = KeyCode(220)
//        val VK_RIGHT_BRACKET= KeyCode(221)
//        val VK_APOSTROPHE   = KeyCode(222)
    }
}
