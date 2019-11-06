package com.nectar.doodle.system.impl

import com.nectar.doodle.HTMLButtonElement
import com.nectar.doodle.HTMLElement
import com.nectar.doodle.HTMLInputElement
import com.nectar.doodle.dom.hasAutoOverflow
import org.w3c.dom.events.EventTarget


internal fun isNativeElement  (target: EventTarget?) = target is HTMLElement && (target.hasAutoOverflow || target is HTMLButtonElement || target is HTMLInputElement)
internal fun nativeScrollPanel(target: EventTarget?) = target is HTMLElement && target.hasAutoOverflow