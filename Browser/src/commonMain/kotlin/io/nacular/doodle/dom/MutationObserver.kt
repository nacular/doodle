@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

internal expect external class MutationRecord: JsAny {
    val attributeName: String
    val removedNodes : NodeList
}

internal class MutationObserverConfig(
    val childList      : Boolean? = null,
    val attributeFilter: JsArray<JsString>? = null
)

internal expect external interface MutationObserverInit: JsAny {
    var childList      : Boolean?
    var attributeFilter: JsArray<JsString>?
}

internal expect external class MutationObserver(init: (JsArray<MutationRecord>) -> Unit): JsAny {
    fun observe(target: Node, config: MutationObserverInit)
    fun disconnect()
}

internal expect fun MutationObserver.startObserve(target: Node, config: MutationObserverConfig)