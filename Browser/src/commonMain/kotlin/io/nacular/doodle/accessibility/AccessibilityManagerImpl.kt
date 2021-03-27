package io.nacular.doodle.accessibility

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.impl.NativeEventHandlerFactory
import io.nacular.doodle.drawing.impl.NativeEventListener
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.role
import io.nacular.doodle.system.impl.KeyInputServiceImpl
import io.nacular.doodle.system.impl.KeyInputServiceImpl.RawListener

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

    override fun labelChanged(view: View) = labelChanged(view, root(view))

    override fun enabledChanged(view: View) = enabledChanged(view, root(view))

    override fun roleAdopted(view: View) {
        view.accessibilityRole?.let { role ->
            role.manager = this
            role.view    = view

            root(view).let {
                elementToView[it  ] = view
                viewToElement[view] = it

                it.role = role.name
                labelChanged  (view, it)
                enabledChanged(view, it)

                roleUpdated(it, role)
            }
        }
    }

    override fun roleAbandoned(view: View) {
        view.accessibilityRole?.manager = null

        viewToElement[view]?.let {
            elementToView -= it

            it.role = ""
        }

        viewToElement -= view
    }

    override fun roleUpdated(view: View) {
        view.accessibilityRole?.let { role ->
            roleUpdated(root(view), role)
        }
    }

    override fun invoke(keyState: KeyState, target: EventTarget?): Boolean {
        view(target)?.let {
            println("key changed: $it")

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
        view(target)?.let {
            focusManager.requestFocus(it)
        }

        return false
    }

    override fun onFocusLost(target: EventTarget?): Boolean {
        view(target)?.let {
            if (it === focusManager.focusOwner) {
                focusManager.clearFocus()
            }
        }

        return false
    }

    private fun root(view: View) = device[view].rootElement

    private fun labelChanged(view: View, root: HTMLElement) {
        root.updateAttribute("aria-label", view.accessibilityLabel)
    }

    private fun enabledChanged(view: View, root: HTMLElement) {
        root.updateAttribute("aria-disabled", if (view.enabled) null else true)
    }

    private fun <T> HTMLElement.updateAttribute(name: String, value: T?) {
        when (value) {
            null -> removeAttribute(name          )
            else -> setAttribute   (name, "$value")
        }
    }

    private fun roleUpdated(viewRoot: HTMLElement, role: AccessibilityRole) {
        viewRoot.apply {
            when (role) {
                is RangeRole    -> {
                    setAttribute("aria-valuenow", "${role.valueNow}")
                    setAttribute("aria-valuemin", "${role.valueMin}")
                    setAttribute("aria-valuemax", "${role.valueMax}")
                }
                is radio        -> setAttribute("aria-checked", "${role.pressed}")
                is switch       -> setAttribute("aria-checked", "${role.pressed}")
                is checkbox     -> setAttribute("aria-checked", "${role.pressed}")
                is togglebutton -> setAttribute("aria-pressed", "${role.pressed}")
            }
        }
    }

    private fun view(target: EventTarget?) = when (target) {
        is HTMLElement -> elementToView[target]
        else           -> null
    }
}