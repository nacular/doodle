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
import io.nacular.doodle.system.KeyInputService.KeyResponse
import io.nacular.doodle.system.KeyInputService.KeyResponse.Ignored
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

            update()
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
                        deleteRelationship()
                    }
                }
            }

        init {
            idRelationships      += this
            source.firstRender   += firstRender
            source.displayChange += displayChanged
            target.firstRender   += firstRender
            target.displayChange += displayChanged

            update()
        }

        fun delete() {
            idRelationships      -= this
            source.firstRender   -= firstRender
            source.displayChange -= displayChanged
            target.firstRender   -= firstRender
            target.displayChange -= displayChanged

            if (sourceReady) {
                deleteRelationship()
            }
        }

        private fun update() {
            if (sourceReady && targetId != null) {
                root(source).updateAttribute(propertyName, targetId)
                nativeElementLinks[source]?.updateAttribute(propertyName, targetId)
            }
        }

        private fun deleteRelationship() {
            root(this.source).updateAttribute(propertyName, null)
            nativeElementLinks[source]?.updateAttribute(propertyName, null)
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
        updateRelationship(view, view.nextInAccessibleReadOrder, ARIA_FLOW_TO)
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
        updateRelationship(owner, owned, ARIA_CONTROLS)
    }

    override fun removeOwnership(owner: View, owned: View) {
        updateRelationship(owner, null, ARIA_CONTROLS)
    }

    override fun invoke(keyState: KeyState, target: EventTarget?): KeyResponse {
        view(target)?.let {
            focusManager.requestFocus(it)
        }

        return Ignored
    }

    override fun onClick(event: Event): Boolean {
        if (event.target is HTMLButtonElement?) {
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

    private val nativeElementLinks = mutableMapOf<View, HTMLElement>()

    internal fun linkNativeElement(source: View, root: HTMLElement) {
        // FIXME: Handle case where IdRelationship already exists
        nativeElementLinks[source] = root
    }

    internal fun unlinkNativeElement(source: View, root: HTMLElement) {
        val existing = nativeElementLinks[source]

        if (existing == root) {
            nativeElementLinks -= source
        }
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
            root.updateAttribute(ARIA_LABEL, view.accessibilityLabel)
        }

        // will clear field when provider is null
        updateRelationship(view, provider, ARIA_LABELED_BY)
    }

    private fun syncDescription(view: View, @Suppress("UNUSED_PARAMETER") root: HTMLElement) {
        val provider = view.accessibilityDescriptionProvider

        // will clear field when provider is null
        updateRelationship(view, provider, ARIA_DESCRIBED_BY)
    }

    private fun syncEnabled(view: View, root: HTMLElement) {
        root.updateAttribute(ARIA_DISABLED, if (view.enabled) null else true)
    }

    private fun syncVisibility(view: View, root: HTMLElement) {
        root.updateAttribute(ARIA_HIDDEN, if (view.visible) null else true)
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
                    updateAttribute(ARIA_VALUE_NOW,  role.value    )
                    updateAttribute(ARIA_VALUE_MIN,  role.min      )
                    updateAttribute(ARIA_VALUE_MAX,  role.max      )
                    updateAttribute(ARIA_VALUE_TEXT, role.valueText)
                }
                is RadioRole        -> updateAttribute(ARIA_CHECKED, role.pressed)
                is SwitchRole       -> updateAttribute(ARIA_CHECKED, role.pressed)
                is CheckBoxRole     -> updateAttribute(ARIA_CHECKED, role.pressed)
                is ToggleButtonRole -> updateAttribute(ARIA_PRESSED, role.pressed)
                is ListItemRole -> {
                    updateAttribute(ARIA_SET_SIZE,     role.listSize)
                    updateAttribute(ARIA_POINT_IN_SET, role.index?.plus(1))
                }
                is TreeRole -> {
                    updateAttribute(ARIA_MULTISELECTABLE, "true")
                }
                is TreeItemRole -> {
                    updateAttribute(ARIA_LEVEL,        role.depth + 1     )
                    updateAttribute(ARIA_SET_SIZE,     role.treeSize      )
                    updateAttribute(ARIA_EXPANDED,     role.expanded      )
                    updateAttribute(ARIA_SELECTED,     role.selected      )
                    updateAttribute(ARIA_POINT_IN_SET, role.index?.plus(1))
                }
                is TextBoxRole -> updateAttribute(ARIA_PLACEHOLDER, role.placeHolder?.takeIf { it.isNotBlank() })
                is TabRole     -> updateAttribute(ARIA_SELECTED,    role.selected)
                is TabListRole -> role.tabToPanelMap.forEach { (tab, tabPanel) ->
                    addOwnership(tab, tabPanel)
                }
                else           -> {}
            }

            when (role) {
                is SliderRole -> updateAttribute(ARIA_ORIENTATION, role.orientation?.name?.lowercase())
                else          -> {}
            }
        }
    }

    @Suppress("USELESS_IS_CHECK")
    private fun view(target: EventTarget?) = when (target) {
        is HTMLElement -> elementToView[target]
        else           -> null
    }

    companion object {
        private const val ARIA_LABEL           = "aria-label"
        private const val ARIA_LEVEL           = "aria-level"
        private const val ARIA_HIDDEN          = "aria-hidden"
        private const val ARIA_CHECKED         = "aria-checked"
        private const val ARIA_FLOW_TO         = "aria-flowto"
        private const val ARIA_PRESSED         = "aria-pressed"
        private const val ARIA_EXPANDED        = "aria-expanded"
        private const val ARIA_SELECTED        = "aria-selected"
        private const val ARIA_DISABLED        = "aria-disabled"
        private const val ARIA_CONTROLS        = "aria-controls"
        private const val ARIA_SET_SIZE        = "aria-setsize"
        private const val ARIA_VALUE_NOW       = "aria-valuenow"
        private const val ARIA_VALUE_MIN       = "aria-valuemin"
        private const val ARIA_VALUE_MAX       = "aria-valuemax"
        private const val ARIA_VALUE_TEXT      = "aria-valuetext"
        private const val ARIA_LABELED_BY      = "aria-labelledby"
        private const val ARIA_ORIENTATION     = "aria-orientation"
        private const val ARIA_PLACEHOLDER     = "aria-placeholder"
        private const val ARIA_POINT_IN_SET    = "aria-posinset"
        private const val ARIA_DESCRIBED_BY    = "aria-describedby"
        private const val ARIA_MULTISELECTABLE = "aria-multiselectable"
    }
}