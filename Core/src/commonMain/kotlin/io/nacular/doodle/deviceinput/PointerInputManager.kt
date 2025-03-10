package io.nacular.doodle.deviceinput

import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.event.Interaction
import io.nacular.doodle.event.Pointer
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.with
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Type
import io.nacular.doodle.system.SystemPointerEvent.Type.Click
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.utils.fastMutableSetOf
import io.nacular.doodle.utils.fastSetOf

/** @suppress */
@Internal
public interface PointerInputManager {
    public fun shutdown()
}

/** @suppress */
@Internal
public interface EventPreprocessor {
    public operator fun invoke(pointerEvent: PointerEvent)
}

/** @suppress */
@Internal
@Suppress("NestedLambdaShadowedImplicitParameter")
public class PointerInputManagerImpl(
        private val display          : Display,
        private val inputService     : PointerInputService,
        private val viewFinder       : ViewFinder,
        private val eventPreprocessor: EventPreprocessor? = null): PointerInputManager, PointerInputService.Listener {
    private inner class ClickedViewMap {
        private val map: MutableMap<Int, View> = mutableMapOf()

        operator fun set(event: SystemPointerEvent, value: View): View? = map.put(event.id, value)

        operator fun get(event: SystemPointerEvent): View? = map[event.id]

        operator fun minusAssign(event: SystemPointerEvent) {
            map.remove(event.id)?.also {
                if (coveredView[event] != it) {
                    cleanupPointers(it, event)
                }
            }
        }
    }

    private inner class CoveredViewMap {
        private val map: MutableMap<Int, View> = mutableMapOf()

        operator fun set(event: SystemPointerEvent, value: View): View? = map.put(
            event.id,
            value.also { registerListeners(it) }
        )?.also {
            cleanupPointers(it, event)
        }

        operator fun get(event: SystemPointerEvent): View? = map[event.id]

        operator fun minusAssign(event: SystemPointerEvent) {
            map.remove(event.id)?.also {
                cleanupPointers(it, event)
            }
        }
    }

    private inner class ClickedPassThroughViewMap {
        private val map: MutableMap<Int, MutableSet<View>> = mutableMapOf()

        fun add   (event: SystemPointerEvent, value: View) {
            map.getOrPut(event.id) { mutableSetOf() }.add(value).also {
                passedThroughCoveredView.add(event, value)
            }
        }
        fun get   (event: SystemPointerEvent)              = map[event.id] ?: emptyList()
        fun remove(event: SystemPointerEvent, value: View): View? = map[event.id]?.let {
            val removed = it.remove(value)

            if (it.isEmpty()) {
                map.remove(event.id)
            }

            passedThroughCoveredView.remove(event, value)

            return if (removed) value else null
        }

        operator fun minusAssign(event: SystemPointerEvent) {
            map.remove(event.id)
        }
    }

    private inner class CoveredPassThroughViewMap {
        private val map: MutableMap<Int, MutableSet<View>> = mutableMapOf()

        fun add   (event: SystemPointerEvent, value: View) { map.getOrPut(event.id) { mutableSetOf() } }
        fun get   (event: SystemPointerEvent)              = map[event.id] ?: emptyList()
        fun remove(event: SystemPointerEvent, value: View): View? = map[event.id]?.let {
            val removed = it.remove(value)

            if (it.isEmpty()) {
                map.remove(event.id)
            }

            return if (removed) value else null
        }

        operator fun minusAssign(event: SystemPointerEvent) {
            map.remove(event.id)
        }
    }

    private val pressedPointers = mutableSetOf<Int>()
    private fun isPointerDown(event: SystemPointerEvent) = event.id in pressedPointers

    private val clickedView = ClickedViewMap()
    private val coveredView = CoveredViewMap()

    private val passedThroughCoveredView = CoveredPassThroughViewMap()
    private val passedThroughClickedView = ClickedPassThroughViewMap()

    private val targetedInteractions = mutableMapOf<View, MutableSet<Interaction>>()

    private fun cleanupPointers(view: View, event: SystemPointerEvent) {
        targetedInteractions[view]?.let {
            it.removeAll {
                it.pointer.id == event.id && view != clickedView[event]
            }

            if (it.isEmpty()) {
                targetedInteractions -= view
                unregisterListeners(view)
            }
        }
    }

    private var cursor = null as Cursor?; set(new) {
        field = new
        inputService.setCursor(display, cursor ?: display.cursor)
    }

    private var toolTipText = ""; set(new) {
        field = new
        inputService.setToolTipText(display, field)
    }

    private val displayCursorChanged = { _: Display, _: Cursor?, new: Cursor? -> cursor = new }

    private val enabledChanged = { view: View, _: Boolean, enabled: Boolean ->
        // FIXME: Send disabled view/parent exit/enter events for all existing interactions.
        // FIXME: Send enabled view/parent enter/exit events for all valid interactions.
        if (!enabled) {
            targetedInteractions -= view
        }
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { view,_,_ ->
        changedViewReferenceFrames += view
    }

    private val transformChanged: (View, AffineTransform, AffineTransform) -> Unit = { view,_,_ ->
        changedViewReferenceFrames += view
    }

    private val viewCursorChanged = { view: View, _: Cursor?, _: Cursor? ->
        cursor = cursor(of = view)
    }

    init {
        inputService.addListener(display, this)

        display.cursorChanged += displayCursorChanged

        cursor = display.cursor
    }

    override fun shutdown() {
        inputService.removeListener(display, this)

        display.cursorChanged -= displayCursorChanged
    }

    override fun invoke(event: SystemPointerEvent) {
        when (event.type) {
            Up          -> pointerUp  (event)
            Enter, Move -> pointerMove(event)
            Down        -> pointerDown(event)
            Exit        -> pointerExit(event)
            else        -> {}
        }
    }

    private fun pointerExit(event: SystemPointerEvent) {
        clickedView[event]?.let {
            deliver(event, it, Exit)
            clickedView -= event
        }

        coveredView[event]?.let {
            deliver(event, it, Exit)
            coveredView -= event
            cleanupPointers(it, event)
        }

        pressedPointers -= event.id
    }

    private fun pointerDown(event: SystemPointerEvent) {
        toolTipText = ""

        view(from = event)?.let { view ->
            if (view != coveredView[event]) {
                deliver(event, view, Enter)

                coveredView[event] = view
                cursor             = cursor(of = coveredView[event])
            }

            deliver(event, view)

            clickedView[event] = view
        }

        pressedPointers += event.id
    }

    private fun pointerUp(event: SystemPointerEvent) {
        val view = view(from = event)

        if (clickedView[event] != null || isPointerDown(event)) {
            clickedView[event]?.let {
                deliver(event, it)

                if (view === it) {
                    deliver(event, it, Click)
                }
            }

            if (view !== clickedView[event]) {
                clickedView[event]?.let {
                    // Avoid case where pointer-move hasn't been seen (possible if drag-drop happened)
                    if (coveredView[event] == it) {
                        coveredView -= event

                        deliver(event, it, Exit)
                    }
                }

                if (view != null) {
                    coveredView[event] = view
                    deliver(event, view, Enter)
                    deliver(event, view       )

                    cursor = cursor(of = view)
                } else {
                    cursor = display.cursor
                }
            } else {
                cursor = cursor(of = view)
            }

            clickedView -= event
        } else if (view != null) {
            coveredView[event] = view
            deliver(event, view, Enter)
            deliver(event, view       )

            cursor = cursor(of = view)
        } else {
            cursor = display.cursor
        }

        pressedPointers -= event.id
    }

//    private fun doubleClick(event: SystemPointerEvent) {
//        toolTipText = ""
//
//        view(from = event)?.let {
//            coveredView[event] = it
//            deliver(event, it, Up   )
//            deliver(event, it, Click)
//            clickedView -= event
//        }
//    }

    private fun pointerMove(event: SystemPointerEvent) {
        clickedView[event]?.let {
            // TODO: Deliver Drag to views that passed through pointer-down

            deliver(event, it, Drag)

            cursor = cursor(of = it)
        }

        val view = view(from = event)

        // TODO: Need to send synthetic EXIT for views that passed MOVE through and are not covered anymore

        if (view !== coveredView[event]) {
            coveredView[event]?.let {
                if (!isPointerDown(event) || it === clickedView[event]) {
                    deliver(event, it, Exit)
                }
            }

            when (view) {
                null -> coveredView        -= event
                else -> coveredView[event]  = view
            }

            if (view != null) {
                if (!isPointerDown(event) || view === clickedView[event]) {
                    deliver(event, view, Enter)

                    cursor = cursor(of = coveredView[event])
                }
            } else if (clickedView[event] == null) {
                toolTipText = ""

                cursor = null
            }
        } else if (!isPointerDown(event)) {
            coveredView[event]?.let {
                deliver(event, it, Move)
            }

            if (coveredView[event] == null) {
                toolTipText = ""
            }

            cursor = cursor(of = coveredView[event])
        }
    }

    private fun shouldHandleEvent(view: View, event: PointerEvent) = when (event.type) {
        Move, Drag -> view.shouldHandlePointerMotionEvent_(event)
        else       -> view.shouldHandlePointerEvent_      (event)
    }

    private fun deliver(systemEvent: SystemPointerEvent, target: View, type: Type = systemEvent.type): Boolean {
        val event = createPointerEvent(systemEvent, target, type)

        when (event.type) {
            Enter, Move, Up -> toolTipText = target.toolTipText(event)
            else -> {}
        }

        val chain = mutableListOf(event.target)

        var view = event.target.parent

        while (view != null) {
            if (view.enabled && view.visible) {
                chain += view
            }

            view = view.parent
        }

        // Sinking
        chain.asReversed().forEach {
            val newEvent = event.with(source = it)

            eventPreprocessor?.invoke(newEvent)

            if (!newEvent.consumed) {
                when (newEvent.type) {
                    Move, Drag -> it.filterPointerMotionEvent_(newEvent)
                    else       -> it.filterPointerEvent_      (newEvent)
                }
            }

            if (newEvent.consumed || newEvent.preventOsHandling) {
                systemEvent.consume()
            }

            if (newEvent.consumed) {
                return true
            }
        }

        // Floating
        chain.forEach {
            val newEvent = event.with(source = it)

            when (newEvent.type) {
                Move, Drag -> it.handlePointerMotionEvent_(newEvent)
                else       -> it.handlePointerEvent_      (newEvent)
            }

            if (newEvent.consumed || newEvent.preventOsHandling) {
                systemEvent.consume()
            }

            if (newEvent.consumed) {
                return true
            }
        }

        return false
    }

    private fun registerListeners(view: View) {
        view.cursorChanged    += viewCursorChanged
        view.enabledChanged   += enabledChanged
        view.boundsChanged    += boundsChanged
        view.transformChanged += transformChanged
    }

    private fun unregisterListeners(view: View) {
        view.cursorChanged    -= viewCursorChanged
        view.enabledChanged   -= enabledChanged
        view.boundsChanged    -= boundsChanged
        view.transformChanged -= transformChanged

        changedViewReferenceFrames -= view
    }

    private fun cursor(of: View?) = when (display.cursor) {
        null -> of?.cursor
        else -> display.cursor
    }

    private fun view(from: SystemPointerEvent): View? {
        var target        = adjustForNative(viewFinder.find(display, from.location), from)
        val passedThrough = mutableSetOf<View>()

        while (target != null) {
            val event = createPointerEvent(from, target, from.type)
            when {
                shouldHandleEvent(target, event) -> break
                else                             -> {
                    when (from.type) {
                        Down -> passedThroughClickedView.add   (from, target)
                        Up   -> passedThroughClickedView.remove(from, target)?.let {
                            // synthesize click event
                            it.notifyOfPassThrough(createPointerEvent(from, it, Click))
                        }
                        Exit  -> passedThroughCoveredView.remove(from, target)
                        Enter -> if (target in passedThroughClickedView.get(from)) {
                            passedThroughCoveredView.add(from, target)
                        }
                        else -> {}
                    }

                    passedThrough += target
                    target = view(from, target.parent) { it !in passedThrough }
                }
            }
        }

        return target
    }

    private fun view(from: SystemPointerEvent, starting: View?, filter: (View) -> Boolean): View? = adjustForNative(
        viewFinder.find(display, from.location, starting) { it: View -> it.enabled && filter(it) },
        from
    )

    private fun adjustForNative(target: View?, from: SystemPointerEvent): View? {
        var view = target

        return view?.let {
            // This handles cases when a native scroll panel's scroll bars, or blank areas.
            // We need to ensure the ScrollPanel is properly selected, even if the system thinks
            // a child is the proper target view
            if (from.nativeScrollPanel) {
                // Search for ScrollBar ancestor and use that if found
                var newView = view

                while(newView != null && newView !is ScrollPanel) {
                    newView = newView.parent
                }

                if (newView is ScrollPanel) {
                    view = newView
                }
            }

            view
        }
    }

    private val changedViewReferenceFrames = mutableSetOf<View>()

    private fun createPointerEvent(event: SystemPointerEvent, target: View, type: Type = event.type): PointerEvent {
        val interaction = createInteraction(target, event, type)

        targetedInteractions.getOrPut(target) { fastMutableSetOf() }.let { set ->
            set.removeAll { it.pointer == interaction.pointer }

            if (target in changedViewReferenceFrames) {
                updateInteractions(target)
            }

            set += interaction
        }

        return PointerEvent(
            source              = target,
            target              = target,
            buttons             = event.buttons,
            clickCount          = event.clickCount,
            targetInteractions  = targetedInteractions[target]!!,
            changedInteractions = fastSetOf(interaction),
            allInteractions     = { targetedInteractions.values.asSequence().flatten().toSet() },
            modifiers           = event.modifiers
        )
    }

    private fun createInteraction(target: View, event: SystemPointerEvent, type: Type = event.type) = Interaction(
        Pointer(event.id),
        target,
        type,
        target.fromAbsolute(event.location),
        event.location
    )

    private fun updateInteractions(view: View) {
        changedViewReferenceFrames -= view

        targetedInteractions[view]?.let { set ->
            val updated = set.map { interaction ->
                Interaction(
                    pointer          = interaction.pointer,
                    target           = interaction.target,
                    state            = interaction.state,
                    location         = interaction.target.fromAbsolute(interaction.absoluteLocation),
                    absoluteLocation = interaction.absoluteLocation
                )
            }

            set.clear()
            set.addAll(updated)
        }
    }
}