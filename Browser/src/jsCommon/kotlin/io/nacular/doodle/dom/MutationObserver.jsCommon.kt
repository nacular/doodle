@file:Suppress("EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE")

package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/8/24.
 */
internal actual external class MutationRecord: JsAny {
    actual val attributeName: String
    actual val removedNodes : NodeList
}

internal actual external class MutationObserver actual constructor(init: (JsArray<MutationRecord>) -> Unit): JsAny {
    actual fun observe(target: Node, config: MutationObserverInit)
    actual fun disconnect()
}

internal actual external interface MutationObserverInit: JsAny {
    actual var childList: Boolean?
    actual var attributeFilter: JsArray<JsString>?
}