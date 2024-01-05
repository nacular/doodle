package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/8/24.
 */
internal actual external class MutationRecord: JsAny {
    internal actual val attributeName: String
    internal actual val removedNodes : NodeList
}

internal actual external class MutationObserver actual constructor(init: (JsArray<MutationRecord>) -> Unit): JsAny {
    internal actual fun observe(target: Node, config: MutationObserverInit)
    internal actual fun disconnect()
}

internal actual external interface MutationObserverInit: JsAny {
    actual var childList: Boolean?
    actual var attributeFilter: JsArray<JsString>?
}