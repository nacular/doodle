package io.nacular.doodle.dom

internal actual fun MutationObserver.startObserve(
    target: Node,
    config: MutationObserverConfig
): Unit = this.observe(target, jsObject {
    childList       = config.childList
    config.attributeFilter?.let { attributeFilter = it }
})