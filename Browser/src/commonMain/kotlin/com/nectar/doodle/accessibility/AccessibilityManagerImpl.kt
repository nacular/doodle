package com.nectar.doodle.accessibility

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.EventTarget
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.impl.NativeEventHandlerFactory
import com.nectar.doodle.drawing.impl.NativeEventListener
import com.nectar.doodle.drawing.impl.RealGraphicsSurface
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.role
import com.nectar.doodle.system.impl.KeyInputServiceImpl
import com.nectar.doodle.system.impl.KeyInputServiceImpl.RawListener

/**
 * Created by Nicholas Eddy on 3/28/20.
 */
internal class AccessibilityManagerImpl(
        private val keyInputService          : KeyInputServiceImpl,
        private val device                   : GraphicsDevice<RealGraphicsSurface>,
        private val focusManager             : FocusManager,
                    nativeEventHandlerFactory: NativeEventHandlerFactory,
                    htmlFactory              : HtmlFactory): AccessibilityManager, RawListener, NativeEventListener {
    private val elementToView = mutableMapOf<HTMLElement, View>()
    private val viewToElement = mutableMapOf<View, HTMLElement>()
    private val eventHandler  = nativeEventHandlerFactory(htmlFactory.root, this)

    init {
        keyInputService += this

        eventHandler.registerFocusInListener()
        eventHandler.registerClickListener  ()
    }

    fun shutdown() {
        keyInputService -= this

        eventHandler.unregisterClickListener  ()
        eventHandler.unregisterFocusInListener()
    }

    override fun roleAdopted(view: View) {
        view.accessibilityRole?.let { role ->
            device[view].rootElement.let {
                elementToView[it  ] = view
                viewToElement[view] = it

                it.role = role::class.simpleName

                when (role) {
                    is RangeRole -> {
                        it.setAttribute("aria-valuenow", "${role.valueNow}")
                        it.setAttribute("aria-valuemin", "${role.valueMin}")
                        it.setAttribute("aria-valuemax", "${role.valueMax}")
                    }
                }
            }
        }
    }

    override fun roleAbandoned(view: View) {
        viewToElement[view]?.let {
            elementToView -= it

            it.role = ""
        }

        viewToElement -= view
    }

    override fun invoke(keyState: KeyState, target: EventTarget?): Boolean {
        println("key state")

        view(target)?.let {
            focusManager.requestFocus(it)
        }

        return false
    }

    override fun onClick(target: EventTarget?): Boolean {
        view(target)?.let {
            when (it) {
                is Button -> it.click()
            }
        }

        return false
    }

    override fun onFocusGained(target: EventTarget?): Boolean {
        println("focus gained")

        view(target)?.let {
            println("focus gained 2")

            focusManager.requestFocus(it)
        }

        return false
    }

    override fun onFocusLost(target: EventTarget?): Boolean {
        println("focus lost")

        view(target)?.let {
            if (it === focusManager.focusOwner) {
                focusManager.clearFocus()
            }
        }

        return false
    }

    private fun view(target: EventTarget?) = when (target) {
        is HTMLElement -> elementToView[target]
        else           -> null
    }
}