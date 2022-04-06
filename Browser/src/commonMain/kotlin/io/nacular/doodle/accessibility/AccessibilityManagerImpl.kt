package io.nacular.doodle.accessibility

import io.nacular.doodle.HTMLButtonElement
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.Event
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
import io.nacular.doodle.utils.IdGenerator

/**
 * Created by Nicholas Eddy on 3/28/20.
 */
internal class AccessibilityManagerImpl(
        private val keyInputService          : KeyInputServiceImpl,
        private val device                   : GraphicsDevice<RealGraphicsSurface>,
        private val focusManager             : FocusManager,
        private val idGenerator              : IdGenerator,
                    nativeEventHandlerFactory: NativeEventHandlerFactory,
                    htmlFactory              : HtmlFactory
): AccessibilityManager, RawListener, NativeEventListener {

    private inner class IdRelationship(private val source: View, private val target: View, private val propertyName: String) {
        private val firstRender: (View) -> Unit = {
            when (it) {
                source -> sourceReady = true
                else   -> targetId    = id(target)
            }

            if (sourceReady && targetId != null) {
                root(source).updateAttribute(propertyName, targetId)
            }
        }

        private val displayChanged: (View, Boolean, Boolean) -> Unit = { view,_,displayed ->
            when (view) {
                source -> sourceReady = sourceReady && displayed
                else   -> targetId    = if (!displayed) null else targetId
            }
        }

        private var sourceReady = source.rendered
        private var targetId    = if (target.rendered) id(target) else null
            set(new) {
                if (field != new) {
                    field = new
                    if (field == null && sourceReady) {
                        root(this.source).updateAttribute(propertyName, null)
                    }
                }
            }

        init {
            idRelationships      += this
            source.firstRender   += firstRender
            source.displayChange += displayChanged
            target.firstRender   += firstRender
            target.displayChange += displayChanged

            if (sourceReady && targetId != null) {
                root(source).updateAttribute(propertyName, targetId)
            }
        }

        fun delete() {
            idRelationships      -= this
            source.firstRender   -= firstRender
            source.displayChange -= displayChanged
            target.firstRender   -= firstRender
            target.displayChange -= displayChanged

            if (sourceReady) {
                root(source).updateAttribute(propertyName, null)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is IdRelationship) return false

            if (source != other.source) return false
            if (target != other.target) return false
            if (propertyName != other.propertyName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = source.hashCode()
            result = 31 * result + target.hashCode()
            result = 31 * result + propertyName.hashCode()
            return result
        }
    }

    private val elementToView         = mutableMapOf<HTMLElement, View>()
    private val viewToElement         = mutableMapOf<View, HTMLElement>()
    private val eventHandler          = nativeEventHandlerFactory(htmlFactory.root, this)
    private val idRelationships       = mutableSetOf<IdRelationship>()
    private val idRelationshipSources = mutableMapOf<Pair<View, String>, IdRelationship>()

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

    override fun syncLabel(view: View) = syncLabel(view, root(view))

    override fun syncEnabled(view: View) = syncEnabled(view, root(view))

    override fun syncVisibility(view: View) = syncVisibility(view, root(view))

    override fun syncDescription(view: View) = syncDescription(view, root(view))

    override fun syncNextReadOrder(view: View) {
        updateRelationship(view, view.nextInAccessibleReadOrder, "aria-flowto")
    }

    override fun roleAdopted(view: View) {
        view.accessibilityRole?.let {
            it.manager = this
            it.view    = view

            registerRole(view, it)
        }
    }

    override fun roleAbandoned(view: View) {
        view.accessibilityRole?.let {
            it.manager = null
            it.view    = null

            unregisterRole(view)
        }
    }

    override fun roleUpdated(view: View) {
        view.accessibilityRole?.let { role ->
            roleUpdated(root(view), role)
        }
    }

    override fun addOwnership(owner: View, owned: View) {
        updateRelationship(owner, owned, "aria-controls")
    }

    override fun removeOwnership(owner: View, owned: View) {
        updateRelationship(owner, null, "aria-controls")
    }

    override fun invoke(keyState: KeyState, target: EventTarget?): Boolean {
        view(target)?.let {
            focusManager.requestFocus(it)
        }

        return false
    }

    @Suppress("UNUSED_PARAMETER")
    override fun onClick(event: Event): Boolean {
        if (event.target is HTMLButtonElement) {
            view(event.target)?.let {
                when (it) {
                    is Button -> it.click()
                }
            }
        }

        return false
    }

    override fun onFocusGained(event: Event): Boolean {
        view(event.target)?.let {
            focusManager.requestFocus(it)
        }

        return false
    }

    override fun onFocusLost(event: Event): Boolean {
        view(event.target)?.let {
            if (it === focusManager.focusOwner) {
                focusManager.clearFocus()
            }
        }

        return false
    }

    private fun updateRelationship(source: View, target: View?, name: String) {
        idRelationshipSources.remove(source to name)?.delete()

        if (target != null) {
            idRelationshipSources[source to name] = IdRelationship(source, target, name)
        }
    }

    private fun registerRole(view: View, role: AccessibilityRole) {
        root(view).let {
            elementToView[it  ] = view
            viewToElement[view] = it

            it.role = role.name

            roleUpdated(it, role)
        }
    }

    private fun unregisterRole(view: View) {
        viewToElement[view]?.let {
            elementToView -= it

            it.role = null
        }

        viewToElement -= view
    }

    private fun root(view: View) = device[view].rootElement

    private fun id(view: View?): String? = when (view) {
        null -> null
        else -> {
            val root = root(view)

            if (root.id.isBlank()) {
                root.id = idGenerator.nextId()
            }

            root.id
        }
    }

    private fun syncLabel(view: View, root: HTMLElement) {
        val provider = view.accessibilityLabelProvider

        if (provider == null) {
            root.updateAttribute("aria-label", view.accessibilityLabel)
        }

        // will clear field when provider is null
        updateRelationship(view, provider, "aria-labelledby")
    }

    private fun syncDescription(view: View, @Suppress("UNUSED_PARAMETER") root: HTMLElement) {
        val provider = view.accessibilityDescriptionProvider

        // will clear field when provider is null
        updateRelationship(view, provider, "aria-describedby")
    }

    private fun syncEnabled(view: View, root: HTMLElement) {
        root.updateAttribute("aria-disabled", if (view.enabled) null else true)
    }

    private fun syncVisibility(view: View, root: HTMLElement) {
        root.updateAttribute("aria-hidden", if (view.visible) null else true)
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
                is RangeRole -> {
                    updateAttribute("aria-valuenow",  role.value    )
                    updateAttribute("aria-valuemin",  role.min      )
                    updateAttribute("aria-valuemax",  role.max      )
                    updateAttribute("aria-valuetext", role.valueText)
                }
                is RadioRole    -> updateAttribute("aria-checked", role.pressed)
                is SwitchRole   -> updateAttribute("aria-checked", role.pressed)
                is CheckBoxRole -> updateAttribute("aria-checked", role.pressed)
                is ToggleButtonRole -> updateAttribute("aria-pressed", role.pressed)
                is ListItemRole -> {
                    updateAttribute("aria-setsize",  role.listSize)
                    updateAttribute("aria-posinset", role.index?.plus(1))
                }
                is TreeItemRole -> {
                    updateAttribute("aria-setsize",  role.treeSize)
                    updateAttribute("aria-posinset", role.index?.plus(1))
                    updateAttribute("aria-level",    role.depth)
                }
                is TextBoxRole -> updateAttribute("aria-placeholder", role.placeHolder?.takeIf { it.isNotBlank() })
                is TabRole     -> updateAttribute("aria-selected",    role.selected)
                is TabListRole -> role.tabToPanelMap.forEach { (tab, tabPanel) ->
                    addOwnership(tab, tabPanel)
                }
            }

            when (role) {
                is SliderRole -> updateAttribute("aria-orientation", role.orientation?.name?.lowercase())
            }
        }
    }

    @Suppress("USELESS_IS_CHECK")
    private fun view(target: EventTarget?) = when (target) {
        is HTMLElement -> elementToView[target]
        else           -> null
    }
}