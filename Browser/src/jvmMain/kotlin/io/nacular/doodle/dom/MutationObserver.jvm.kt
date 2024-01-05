package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 1/8/24.
 */
internal actual class MutationRecord: JsAny {
    internal actual val attributeName: String  = ""
    internal actual val removedNodes : NodeList = object: NodeList() {
        override val length: Int get() = 0
    }
}

internal actual class MutationObserver actual constructor(init: (JsArray<MutationRecord>) -> Unit): JsAny {
    internal actual fun disconnect() {}
    internal actual fun observe(target: Node, config: MutationObserverInit) {}
}

internal actual fun MutationObserver.startObserve(target: Node, config: MutationObserverConfig) {
    this.observe(target, object: MutationObserverInit {
        override var childList       = config.childList
        override var attributeFilter = config.attributeFilter
    })
}

internal actual interface MutationObserverInit: JsAny {
    actual var childList: Boolean?
    actual var attributeFilter: JsArray<JsString>?
}