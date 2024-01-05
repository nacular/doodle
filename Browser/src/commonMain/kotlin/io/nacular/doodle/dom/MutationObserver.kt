@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/8/24.
 */
internal expect external class MutationRecord: JsAny {
    internal val attributeName: String
    internal val removedNodes : NodeList
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
    internal fun observe(target: Node, config: MutationObserverInit)
    internal fun disconnect()
}

internal expect fun MutationObserver.startObserve(target: Node, config: MutationObserverConfig)