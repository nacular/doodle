package com.zinoti.jaz.event

import com.zinoti.jaz.core.Gizmo
import com.zinoti.jaz.geometry.Rectangle


class DisplayRectEvent(aGizmo: Gizmo, val oldValue: Rectangle?, val newValue: Rectangle?): Event<Gizmo>(aGizmo)

/**
 * Called when the source's display rectangle has changed.
 *
 * @param aEvent The event
 */
typealias DisplayRectListener = (DisplayRectEvent) -> Unit