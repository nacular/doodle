package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Rectangle


class DisplayRectEvent(aGizmo: Gizmo, val oldValue: Rectangle?, val newValue: Rectangle?): Event<Gizmo>(aGizmo)

/**
 * Called when the source's display rectangle has changed.
 *
 * @param aEvent The event
 */
typealias DisplayRectListener = (DisplayRectEvent) -> Unit