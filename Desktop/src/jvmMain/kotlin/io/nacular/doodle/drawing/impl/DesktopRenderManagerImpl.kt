package io.nacular.doodle.drawing.impl

import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.theme.InternalThemeManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Created by Nicholas Eddy on 6/2/21.
 */
internal class DesktopRenderManagerImpl(
        display             : InternalDisplay,
        scheduler           : AnimationScheduler,
        themeManager        : InternalThemeManager?,
        accessibilityManager: AccessibilityManager?,
        graphicsDevice      : GraphicsDevice<*>): RenderManagerImpl(display, scheduler, themeManager, accessibilityManager, graphicsDevice) {
    override val dirtyViews         : MutableSet<View>                   = ConcurrentHashMap.newKeySet()
    override val displayTree        : MutableMap<View?, DisplayRectNode> = ConcurrentHashMap()
    override val neverRendered      : MutableSet<View>                   = ConcurrentHashMap.newKeySet()
    override val pendingLayout      : MutableSet<View>                   = ConcurrentSkipListSet(AncestorComparator)
//    override val pendingRender      : MutableSet<View>                   = ConcurrentSkipListSet()
    override val pendingCleanup     : MutableMap<View, MutableSet<View>> = ConcurrentHashMap    ()
    override val addedInvisible     : MutableSet<View>                   = ConcurrentHashMap.newKeySet()
    override val visibilityChanged  : MutableSet<View>                   = ConcurrentHashMap.newKeySet()
    override val pendingBoundsChange: MutableSet<View>                   = ConcurrentHashMap.newKeySet()
}