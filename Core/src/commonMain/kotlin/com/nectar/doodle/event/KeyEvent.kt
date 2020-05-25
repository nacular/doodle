package com.nectar.doodle.event

import com.nectar.doodle.core.View
import com.nectar.doodle.event.KeyState.Type
import com.nectar.doodle.system.SystemInputEvent.Modifier


inline class KeyText(val text: String) {
    companion object {
        val Backspace = KeyText("Backspace")
        val Tab       = KeyText("Tab")
        val Enter     = KeyText("Enter")
        val Escape    = KeyText("Escape")
        val Delete    = KeyText("Delete")

        val Alt        = KeyText("Alt")
        val AltGraph   = KeyText("AltGraph")
        val CapsLock   = KeyText("CapsLock")
        val Control    = KeyText("Control")
        val Fn         = KeyText("Fn")
        val FnLock     = KeyText("FnLock")
        val Meta       = KeyText("Meta")
        val NumLock    = KeyText("NumLock")
        val ScrollLock = KeyText("ScrollLock")
        val Shift      = KeyText("Shift")
        val Symbol     = KeyText("Symbol")
        val SymbolLock = KeyText("SymbolLock")

        val ArrowDown  = KeyText("ArrowDown")
        val ArrowLeft  = KeyText("ArrowLeft")
        val ArrowRight = KeyText("ArrowRight")
        val ArrowUp    = KeyText("ArrowUp")
        val End        = KeyText("End")
        val Home       = KeyText("Home")
        val PageDown   = KeyText("PageDown")
        val PageUp     = KeyText("PageUp")
    }
}

inline class KeyCode(val key: String) {
    companion object {
        // Key Codes from https://w3c.github.io/uievents-code/
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
    }
}

class KeyState(
        val code     : KeyCode,
        val key      : KeyText,
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
}

class KeyEvent(source: View, val key: KeyText, val code: KeyCode, modifiers: Set<Modifier>, val type: Type): InputEvent(source, modifiers) {
    constructor(source: View, keyState: KeyState): this(source, keyState.key, keyState.code, keyState.modifiers, keyState.type)
}
