//package io.nacular.doodle.dom
//
//
//private val SIZE_OBSERVERS = mutableMapOf<HTMLElement, dynamic>()
//
//internal actual fun HTMLElement.startMonitoringSize() {
//    SIZE_OBSERVERS.getOrPut(this) {
//        try {
//            val observer = js(
//                """new ResizeObserver(function(entries) {
//                for (var i = 0; i < entries.length; ++i) {
//                    var entry = entries[i]
//                    if (entry.target.onresize) { entry.target.onresize(new Event("onresize")) }
//                }
//            })""")
//
//            observer.observe(this)
//
//        } catch (ignored: Throwable) {}
//    }
//}