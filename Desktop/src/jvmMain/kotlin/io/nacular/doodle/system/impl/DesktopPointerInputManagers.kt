package io.nacular.doodle.system.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.WindowGroupImpl
import io.nacular.doodle.deviceinput.EventPreprocessor
import io.nacular.doodle.deviceinput.PointerInputManagerImpl
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.system.PointerInputService

internal class DesktopPointerInputManagers(
                windowGroup      : WindowGroupImpl,
    private val inputService     : PointerInputService,
    private val viewFinder       : ViewFinder,
    private val eventPreprocessor: EventPreprocessor
) {
    private val pointerInputManagers = mutableMapOf<Display, PointerInputManagerImpl>()

    init {
        windowGroup.displays.forEach(::createInputManager)
        windowGroup.displaysChanged += { _, removed, added ->
            removed.forEach(::destroyInputManager)
            added.forEach  (::createInputManager )
        }
    }

    fun shutdown() {
        pointerInputManagers.keys.forEach(::destroyInputManager)
    }

    private fun createInputManager(display: Display) {
        pointerInputManagers[display] = PointerInputManagerImpl(
            display,
            inputService,
            viewFinder,
            eventPreprocessor
        )
    }

    private fun destroyInputManager(display: Display) {
        pointerInputManagers.remove(display)?.shutdown()
    }
}