package io.nacular.doodle.application

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.WindowGroupImpl
import io.nacular.doodle.theme.Scene
import io.nacular.doodle.theme.SingleDisplayScene

internal class MultiDisplayScene(private val windowGroup: () -> WindowGroupImpl): Scene() {
    private val windows by lazy { windowGroup() }

    override fun forEachView(block: (View) -> Unit) {
        windows.displays.forEach {
            SingleDisplayScene(it).forEachView(block)
        }
    }

    override fun forEachDisplay(block: (Display) -> Unit) {
        windows.displays.forEach(block)
    }
}