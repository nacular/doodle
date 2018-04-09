package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Rectangle


class DisplayRectEvent(source: Gizmo, val old: Rectangle, val new: Rectangle): Event<Gizmo>(source)