package io.nacular.doodle.system.impl

import io.nacular.doodle.HTMLButtonElement
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.HTMLInputElement
import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.dom.hasScrollOverflow


internal fun isNativeElement  (target: EventTarget?) = target is HTMLElement && (target.hasScrollOverflow || target is HTMLButtonElement || target is HTMLInputElement)
internal fun nativeScrollPanel(target: EventTarget?) = target is HTMLElement &&  target.hasScrollOverflow