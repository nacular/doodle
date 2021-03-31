package io.nacular.doodle.accessibility

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.observable
import kotlin.properties.ReadWriteProperty

/**
 * Created by Nicholas Eddy on 3/28/20.
 */
public interface AccessibilityManager {
    @Internal public fun syncLabel    (view: View)
    @Internal public fun syncEnabled  (view: View)
    @Internal public fun syncVisible  (view: View)
    @Internal public fun roleAdopted  (view: View)
    @Internal public fun roleUpdated  (view: View)
    @Internal public fun roleAbandoned(view: View)

    @Internal public fun addOwnership   (owner: View, owned: View)
    @Internal public fun removeOwnership(owner: View, owned: View)
}

public sealed class AccessibilityRole {
    @Internal public      var manager: AccessibilityManager? = null
    @Internal public      var view   : View?                 = null
    @Internal public open val name   : String?               = this::class.simpleName
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
public class progressbar: RangeRole()

/** Indicates a View that lets a user slide a value between a max and min value. */
public class slider: RangeRole() {
    /** visual orientation of the slider */
    public var orientation: Orientation? by roleProperty(null)
}

public class image: AccessibilityRole()

/** Indicates a View that a user can click to take an action. */
public open class button internal constructor(): AccessibilityRole() {
    public companion object {
        public operator fun invoke(): button = button()
    }
}

/** Indicates a button that toggles between selected/deselected when clicked. */
public open class togglebutton internal constructor(): button() {
    override val name: String? get() = super.name

    public var pressed: Boolean by roleProperty(false)

    public companion object {
        public operator fun invoke(): togglebutton = togglebutton()
    }
}

/** Indicates a toggle-button that toggles between checked/un-checked when clicked. */
public class checkbox: togglebutton()

/** Indicates a toggle-button used (usually within a group) to select a single option from many. */
public class radio: togglebutton()

public class switch: togglebutton()

public class link: button()

public class list: AccessibilityRole()

public class listitem: AccessibilityRole() {
    public var index   : Int? by roleProperty(null)
    public var listSize: Int? by roleProperty(null)
}

public class tree: AccessibilityRole()

public class treeitem: AccessibilityRole() {
    public var index   : Int?    by roleProperty(null )
    public var listSize: Int?    by roleProperty(null )
    public var depth   : Int     by roleProperty(0    )
    public var expanded: Boolean by roleProperty(false)
}

public class textbox: AccessibilityRole() {
    public var placeHolder: String? by roleProperty(null)
}

public class tab: AccessibilityRole() {
    public var selected: Boolean by roleProperty(false)
}

public class tablist: AccessibilityRole() {
    @Internal public val tabToPanelMap: MutableMap<View, View> = mutableMapOf()

    public operator fun set(tab: View, tabPanel: View) {
        tabToPanelMap.put(tab, tabPanel)?.also {
            manager?.removeOwnership(tab, it)
            it.accessibilityRole = null
        }

        manager?.addOwnership(tab, tabPanel)
        tabPanel.accessibilityRole = tabpanel()
    }

    public operator fun minusAssign(tab: View) {
        tabToPanelMap.remove(tab)?.also {
            manager?.removeOwnership(tab, it)
            it.accessibilityRole = null
        }
    }
}

public class tabpanel: AccessibilityRole()

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