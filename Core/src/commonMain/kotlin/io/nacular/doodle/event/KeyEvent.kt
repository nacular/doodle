package io.nacular.doodle.event

import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyState.Type
import io.nacular.doodle.system.SystemInputEvent.Modifier
import kotlin.jvm.JvmInline


@JvmInline
public value class KeyText(public val text: String) {
    public companion object {
        // Key Text from https://www.w3.org/TR/uievents-key/#key-attribute-value
        public val Backspace : KeyText = KeyText("Backspace" )
        public val Tab       : KeyText = KeyText("Tab"       )
        public val Enter     : KeyText = KeyText("Enter"     )
        public val Escape    : KeyText = KeyText("Escape"    )
        public val Delete    : KeyText = KeyText("Delete"    )

        public val Alt       : KeyText = KeyText("Alt"       )
        public val AltGraph  : KeyText = KeyText("AltGraph"  )
        public val CapsLock  : KeyText = KeyText("CapsLock"  )
        public val Control   : KeyText = KeyText("Control"   )
        public val Fn        : KeyText = KeyText("Fn"        )
        public val FnLock    : KeyText = KeyText("FnLock"    )
        public val Meta      : KeyText = KeyText("Meta"      )
        public val NumLock   : KeyText = KeyText("NumLock"   )
        public val ScrollLock: KeyText = KeyText("ScrollLock")
        public val Shift     : KeyText = KeyText("Shift"     )
        public val Symbol    : KeyText = KeyText("Symbol"    )
        public val SymbolLock: KeyText = KeyText("SymbolLock")

        public val ArrowDown : KeyText = KeyText("ArrowDown" )
        public val ArrowLeft : KeyText = KeyText("ArrowLeft" )
        public val ArrowRight: KeyText = KeyText("ArrowRight")
        public val ArrowUp   : KeyText = KeyText("ArrowUp"   )
        public val End       : KeyText = KeyText("End"       )
        public val Home      : KeyText = KeyText("Home"      )
        public val PageDown  : KeyText = KeyText("PageDown"  )
        public val PageUp    : KeyText = KeyText("PageUp"    )
    }
}

@JvmInline
public value class KeyCode(public val key: String) {
    public companion object {
        // Key Codes from https://w3c.github.io/uievents-code/
        public val Backquote           : KeyCode = KeyCode("Backquote")
        public val Backslash           : KeyCode = KeyCode("Backslash")
        public val BracketLeft         : KeyCode = KeyCode("BracketLeft")
        public val BracketRight        : KeyCode = KeyCode("BracketRight")
        public val Comma               : KeyCode = KeyCode("Comma")
        public val Digit0              : KeyCode = KeyCode("Digit0")
        public val Digit1              : KeyCode = KeyCode("Digit1")
        public val Digit2              : KeyCode = KeyCode("Digit2")
        public val Digit3              : KeyCode = KeyCode("Digit3")
        public val Digit4              : KeyCode = KeyCode("Digit4")
        public val Digit5              : KeyCode = KeyCode("Digit5")
        public val Digit6              : KeyCode = KeyCode("Digit6")
        public val Digit7              : KeyCode = KeyCode("Digit7")
        public val Digit8              : KeyCode = KeyCode("Digit8")
        public val Digit9              : KeyCode = KeyCode("Digit9")
        public val Equal               : KeyCode = KeyCode("Equal")
        public val IntlBackslash       : KeyCode = KeyCode("IntlBackslash")
        public val IntlRo              : KeyCode = KeyCode("IntlRo")
        public val IntlYen             : KeyCode = KeyCode("IntlYen")
        public val KeyA                : KeyCode = KeyCode("KeyA")
        public val KeyB                : KeyCode = KeyCode("KeyB")
        public val KeyC                : KeyCode = KeyCode("KeyC")
        public val KeyD                : KeyCode = KeyCode("KeyD")
        public val KeyE                : KeyCode = KeyCode("KeyE")
        public val KeyF                : KeyCode = KeyCode("KeyF")
        public val KeyG                : KeyCode = KeyCode("KeyG")
        public val KeyH                : KeyCode = KeyCode("KeyH")
        public val KeyI                : KeyCode = KeyCode("KeyI")
        public val KeyJ                : KeyCode = KeyCode("KeyJ")
        public val KeyK                : KeyCode = KeyCode("KeyK")
        public val KeyL                : KeyCode = KeyCode("KeyL")
        public val KeyM                : KeyCode = KeyCode("KeyM")
        public val KeyN                : KeyCode = KeyCode("KeyN")
        public val KeyO                : KeyCode = KeyCode("KeyO")
        public val KeyP                : KeyCode = KeyCode("KeyP")
        public val KeyQ                : KeyCode = KeyCode("KeyQ")
        public val KeyR                : KeyCode = KeyCode("KeyR")
        public val KeyS                : KeyCode = KeyCode("KeyS")
        public val KeyT                : KeyCode = KeyCode("KeyT")
        public val KeyU                : KeyCode = KeyCode("KeyU")
        public val KeyV                : KeyCode = KeyCode("KeyV")
        public val KeyW                : KeyCode = KeyCode("KeyW")
        public val KeyX                : KeyCode = KeyCode("KeyX")
        public val KeyY                : KeyCode = KeyCode("KeyY")
        public val KeyZ                : KeyCode = KeyCode("KeyZ")
        public val Minus               : KeyCode = KeyCode("Minus")
        public val Period              : KeyCode = KeyCode("Period")
        public val Quote               : KeyCode = KeyCode("Quote")
        public val Semicolon           : KeyCode = KeyCode("Semicolon")
        public val Slash               : KeyCode = KeyCode("Slash")

        public val AltLeft             : KeyCode = KeyCode("AltLeft")
        public val AltRight            : KeyCode = KeyCode("AltRight")
        public val Backspace           : KeyCode = KeyCode("Backspace")
        public val CapsLock            : KeyCode = KeyCode("CapsLock")
        public val ContextMenu         : KeyCode = KeyCode("ContextMenu")
        public val ControlLeft         : KeyCode = KeyCode("ControlLeft")
        public val ControlRight        : KeyCode = KeyCode("ControlRight")
        public val Enter               : KeyCode = KeyCode("Enter")
        public val MetaLeft            : KeyCode = KeyCode("MetaLeft")
        public val MetaRight           : KeyCode = KeyCode("MetaRight")
        public val ShiftLeft           : KeyCode = KeyCode("ShiftLeft")
        public val ShiftRight          : KeyCode = KeyCode("ShiftRight")
        public val Space               : KeyCode = KeyCode("Space")
        public val Tab                 : KeyCode = KeyCode("Tab")

        public val Convert             : KeyCode = KeyCode("Convert")
        public val KanaMode            : KeyCode = KeyCode("KanaMode")
        public val Lang1               : KeyCode = KeyCode("Lang1")
        public val Lang2               : KeyCode = KeyCode("Lang2")
        public val Lang3               : KeyCode = KeyCode("Lang3")
        public val Lang4               : KeyCode = KeyCode("Lang4")
        public val Lang5               : KeyCode = KeyCode("Lang5")
        public val NonConvert          : KeyCode = KeyCode("NonConvert")

        public val Delete              : KeyCode = KeyCode("Delete")
        public val End                 : KeyCode = KeyCode("End")
        public val Help                : KeyCode = KeyCode("Help")
        public val Home                : KeyCode = KeyCode("Home")
        public val Insert              : KeyCode = KeyCode("Insert")
        public val PageDown            : KeyCode = KeyCode("PageDown")
        public val PageUp              : KeyCode = KeyCode("PageUp")

        public val ArrowDown           : KeyCode = KeyCode("ArrowDown")
        public val ArrowLeft           : KeyCode = KeyCode("ArrowLeft")
        public val ArrowRight          : KeyCode = KeyCode("ArrowRight")
        public val ArrowUp             : KeyCode = KeyCode("ArrowUp")

        public val NumLock             : KeyCode = KeyCode("NumLock")
        public val Numpad0             : KeyCode = KeyCode("Numpad0")
        public val Numpad1             : KeyCode = KeyCode("Numpad1")
        public val Numpad2             : KeyCode = KeyCode("Numpad2")
        public val Numpad3             : KeyCode = KeyCode("Numpad3")
        public val Numpad4             : KeyCode = KeyCode("Numpad4")
        public val Numpad5             : KeyCode = KeyCode("Numpad5")
        public val Numpad6             : KeyCode = KeyCode("Numpad6")
        public val Numpad7             : KeyCode = KeyCode("Numpad7")
        public val Numpad8             : KeyCode = KeyCode("Numpad8")
        public val Numpad9             : KeyCode = KeyCode("Numpad9")
        public val NumpadAdd           : KeyCode = KeyCode("NumpadAdd")
        public val NumpadBackspace     : KeyCode = KeyCode("NumpadBackspace")
        public val NumpadClear         : KeyCode = KeyCode("NumpadClear")
        public val NumpadClearEntry    : KeyCode = KeyCode("NumpadClearEntry")
        public val NumpadComma         : KeyCode = KeyCode("NumpadComma")
        public val NumpadDecimal       : KeyCode = KeyCode("NumpadDecimal")
        public val NumpadDivide        : KeyCode = KeyCode("NumpadDivide")
        public val NumpadEnter         : KeyCode = KeyCode("NumpadEnter")
        public val NumpadEqual         : KeyCode = KeyCode("NumpadEqual")
        public val NumpadHash          : KeyCode = KeyCode("NumpadHash")
        public val NumpadMemoryAdd     : KeyCode = KeyCode("NumpadMemoryAdd")
        public val NumpadMemoryClear   : KeyCode = KeyCode("NumpadMemoryClear")
        public val NumpadMemoryRecall  : KeyCode = KeyCode("NumpadMemoryRecall")
        public val NumpadMemoryStore   : KeyCode = KeyCode("NumpadMemoryStore")
        public val NumpadMemorySubtract: KeyCode = KeyCode("NumpadMemorySubtract")
        public val NumpadMultiply      : KeyCode = KeyCode("NumpadMultiply")
        public val NumpadParenLeft     : KeyCode = KeyCode("NumpadParenLeft")
        public val NumpadParenRight    : KeyCode = KeyCode("NumpadParenRight")
        public val NumpadStar          : KeyCode = KeyCode("NumpadStar")
        public val NumpadSubtract      : KeyCode = KeyCode("NumpadSubtract")

        public val Escape              : KeyCode = KeyCode("Escape")
        public val F1                  : KeyCode = KeyCode("F1")
        public val F2                  : KeyCode = KeyCode("F2")
        public val F3                  : KeyCode = KeyCode("F3")
        public val F4                  : KeyCode = KeyCode("F4")
        public val F5                  : KeyCode = KeyCode("F5")
        public val F6                  : KeyCode = KeyCode("F6")
        public val F7                  : KeyCode = KeyCode("F7")
        public val F8                  : KeyCode = KeyCode("F8")
        public val F9                  : KeyCode = KeyCode("F9")
        public val F10                 : KeyCode = KeyCode("F10")
        public val F11                 : KeyCode = KeyCode("F11")
        public val F12                 : KeyCode = KeyCode("F12")
        public val Fn                  : KeyCode = KeyCode("Fn")
        public val FnLock              : KeyCode = KeyCode("FnLock")
        public val PrintScreen         : KeyCode = KeyCode("PrintScreen")
        public val ScrollLock          : KeyCode = KeyCode("ScrollLock")
        public val Pause               : KeyCode = KeyCode("Pause")
    }
}

public class KeyState(
        public val code     : KeyCode,
        public val key      : KeyText,
        public val modifiers: Set<Modifier>,
        public val type     : Type) {

    public enum class Type {
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

public class KeyEvent(source: View, public val key: KeyText, public val code: KeyCode, modifiers: Set<Modifier>, public val type: Type): InputEvent(source, modifiers) {
    public constructor(source: View, keyState: KeyState): this(source, keyState.key, keyState.code, keyState.modifiers, keyState.type)
}
