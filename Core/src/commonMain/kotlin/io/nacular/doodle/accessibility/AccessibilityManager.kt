package io.nacular.doodle.accessibility

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.observable
import kotlin.properties.ReadWriteProperty

/**
 * Manages all accessibility interactions within an application.
 * @suppress
 */
public interface AccessibilityManager {
    @Internal public fun syncLabel        (view: View)
    @Internal public fun syncEnabled      (view: View)
    @Internal public fun syncVisibility   (view: View)
    @Internal public fun syncDescription  (view: View)
    @Internal public fun syncNextReadOrder(view: View)
    @Internal public fun roleAdopted      (view: View)
    @Internal public fun roleUpdated      (view: View)
    @Internal public fun roleAbandoned    (view: View)

    @Internal public fun addOwnership   (owner: View, owned: View)
    @Internal public fun removeOwnership(owner: View, owned: View)
}

/**
 * Base class for all accessible roles a [View] can take.
 * @see [View.accessibilityRole]
 * @suppress
 */
public sealed class AccessibilityRole {
    @Internal public      var manager: AccessibilityManager? = null
    @Internal public      var view   : View?                 = null
    @Internal public open val name   : String?               = when {
        this::class.simpleName?.lowercase()?.endsWith("role") == true -> this::class.simpleName?.lowercase()?.dropLast(4)
        else                                                          -> this::class.simpleName?.lowercase()
    }
}

/**
 * Role for items that represent a range with a specified value within it.
 * The properties must be kept in sync with the View with this role.
 */
public open class RangeRole internal constructor(): AccessibilityRole() {
    /** max value for the range */
    public var max: Double by roleProperty(0.0)

    /** min value for the range */
    public var min: Double by roleProperty(0.0)

    /** current value for the range */
    public var value: Double by roleProperty(0.0)

    /** text representation for [value] */
    public var valueText: String? by roleProperty(null)
}

/** Indicates a View that shows progress. */
public class ProgressBarRole: RangeRole()

/** Indicates a View that lets a user slide a value between a max and min value. */
public class SliderRole: RangeRole() {
    /** visual orientation of the slider */
    public var orientation: Orientation? by roleProperty(null)
}

public class ImageRole: AccessibilityRole() {
    override val name: String = "img"
}

/** Indicates a View that a user can click to take an action. */
public open class ButtonRole internal constructor(): AccessibilityRole() {
    override val name: String? = "button"

    public companion object {
        public operator fun invoke(): ButtonRole = ButtonRole()
    }
}

/** Indicates a button that toggles between selected/deselected when clicked. */
public open class ToggleButtonRole internal constructor(): ButtonRole() {
    public var pressed: Boolean by roleProperty(false)

    public companion object {
        public operator fun invoke(): ToggleButtonRole = ToggleButtonRole()
    }
}

/** Indicates a toggle-button that toggles between checked/un-checked when clicked. */
public class CheckBoxRole: ToggleButtonRole() {
    override val name: String = "checkbox"
}

/** Indicates a toggle-button used (usually within a group) to select a single option from many. */
public class RadioRole: ToggleButtonRole() {
    override val name: String = "radio"
}

/** A toggle-button that generally indicates an on/off option */
public class SwitchRole: ToggleButtonRole() {
    override val name: String = "switch"
}

/** A hyperlink that navigates to a url. */
public class LinkRole: ButtonRole()

/** A container with a sequential set of [ListItemRole]. */
public class ListRole: AccessibilityRole()

/** An item in a [ListRole]. */
public class ListItemRole: AccessibilityRole() {
    /** The index of the item within its list. */
    public var index: Int? by roleProperty(null)

    /** The size of the list this item belongs to. */
    public var listSize: Int? by roleProperty(null)
}

/** A container with a hierarchy of nested [TreeItemRole]. */
public class TreeRole: AccessibilityRole()

/** A node within a [TreeRole] */
public class TreeItemRole: AccessibilityRole() {
    /** The index of the item within its tree. */
    public var index: Int? by roleProperty(null)

    /** The nested leve of the item. */
    public var depth: Int by roleProperty(0)

    /** Size of the tree this item belongs to */
    public var treeSize: Int? by roleProperty(null)

    /** Expanded state of this item (if it has descendants) */
    public var expanded: Boolean by roleProperty(false)
}

/** A View that allows text entry. */
public class TextBoxRole: AccessibilityRole() {
    /** A short hint indicate the purpose of the field. */
    public var placeHolder: String? by roleProperty(null)
}

/** Item used as the interactive tab for a tabbed-panel. */
public class TabRole: AccessibilityRole() {
    public var selected: Boolean by roleProperty(false)
}

/** View that holds the [TabRole]s for a tabbed-panel. */
public class TabListRole: AccessibilityRole() {
    @Internal public val tabToPanelMap: MutableMap<View, View> = mutableMapOf()

    /** Creates a relationship between a tab's View and the panel it controls */
    public operator fun set(tab: View, tabPanel: View) {
        tabToPanelMap.put(tab, tabPanel)?.also {
            manager?.removeOwnership(tab, it)
            it.accessibilityRole = null
        }

        manager?.addOwnership(tab, tabPanel)
        tabPanel.accessibilityRole = TabPanelRole()
    }

    /** Removes the relationship between a tab's View and the panel it controls */
    public operator fun minusAssign(tab: View) {
        tabToPanelMap.remove(tab)?.also {
            manager?.removeOwnership(tab, it)
            it.accessibilityRole = null
        }
    }
}

/** Displayed item associated with a [TabRole] */
public class TabPanelRole: AccessibilityRole()

internal class spinbutton: RangeRole()
internal class alert: AccessibilityRole()
internal class alertdialog: AccessibilityRole()
internal class dialog: AccessibilityRole()
internal class gridcell: AccessibilityRole()
internal class log: AccessibilityRole()
internal class marquee: AccessibilityRole()
internal class menuitem: AccessibilityRole()
internal class menuitemcheckbox: AccessibilityRole()
internal class menuitemradio: AccessibilityRole()
internal class option: AccessibilityRole()

internal class scrollbar: RangeRole()

internal class status: AccessibilityRole()
internal class timer: AccessibilityRole()
internal class tooltip: AccessibilityRole()

internal class combobox: AccessibilityRole()
internal class grid: AccessibilityRole()
internal class listbox: AccessibilityRole()
internal class menu: AccessibilityRole()
internal class menubar: AccessibilityRole()
internal class radiogroup: AccessibilityRole()
internal class treegrid: AccessibilityRole()

internal class article: AccessibilityRole()
internal class columnheader: AccessibilityRole()
internal class definition: AccessibilityRole()
internal class directory: AccessibilityRole()
internal class document: AccessibilityRole()
internal class group: AccessibilityRole()
internal class heading: AccessibilityRole()
internal class math: AccessibilityRole()
internal class note: AccessibilityRole()
internal class presentation: AccessibilityRole()
internal class region: AccessibilityRole()
internal class row: AccessibilityRole()
internal class rowgroup: AccessibilityRole()
internal class rowheader: AccessibilityRole()
internal class separator: AccessibilityRole()
internal class toolbar: AccessibilityRole()

internal class application: AccessibilityRole()
internal class banner: AccessibilityRole()
internal class complementary: AccessibilityRole()
internal class contentinfo: AccessibilityRole()
internal class form: AccessibilityRole()
internal class main: AccessibilityRole()
internal class navigation: AccessibilityRole()
internal class search: AccessibilityRole()

private inline fun <reified R: AccessibilityRole, T> roleProperty(initial: T, noinline onChange: R.(old: T, new: T) -> Unit = { _,_ -> }): ReadWriteProperty<R, T> = observable(initial) { old, new ->
    view?.let { manager?.roleUpdated(it) }

    onChange(old, new)
}