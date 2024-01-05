package io.nacular.doodle.dom

internal actual fun MutationObserver.startObserve(target: Node, config: MutationObserverConfig) {
    this.observe(
        target,
        jsObject {
            childList = config.childList
            config.attributeFilter?.let { attributeFilter = it }
        }
    )
}