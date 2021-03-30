package io.nacular.doodle.accessibility

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.observable
import kotlin.properties.ReadWriteProperty

/**
 * Created by Nicholas Eddy on 3/28/20.
 */
public interface AccessibilityManager {
    @Internal public fun syncLabel    (view: View)
    @Internal public fun syncEnabled  (view: View)
    @Internal public fun roleAdopted  (view: View)
    @Internal public fun roleUpdated  (view: View)
    @Internal public fun roleAbandoned(view: View)

    @Internal public fun addOwnership   (owner: View, owned: View)
    @Internal public fun removeOwnership(owner: View, owned: View)
}

public sealed class AccessibilityRole {
    @Internal public open val name: String? = this::class.simpleName

    @Internal public var manager : AccessibilityManager? = null
    @Internal public var view    : View? = null
}

public open class RangeRole: AccessibilityRole() {
    public var valueMax : Double  by roleProperty(0.0 )
    public var valueMin : Double  by roleProperty(0.0 )
    public var valueNow : Double  by roleProperty(0.0 )
    public var valueText: String? by roleProperty(null)
}

public class img: AccessibilityRole()

public open class button internal constructor(): AccessibilityRole() {
    public companion object {
        public operator fun invoke(): button = button()
    }
}

public open class togglebutton internal constructor(): button() {
    override val name: String? get() = super.name

    public var pressed: Boolean by roleProperty(false)

    public companion object {
        public operator fun invoke(): togglebutton = togglebutton()
    }
}

public class checkbox: togglebutton()

public class radio: togglebutton()

public class switch: togglebutton()

public class progressbar: RangeRole()

public class slider: RangeRole()

public class list: AccessibilityRole()
public class listitem: AccessibilityRole() {
    public var index   : Int? by roleProperty(null)
    public var listSize: Int? by roleProperty(null)
}

public class tree: AccessibilityRole()
public class treeitem: AccessibilityRole() {
    public var index   : Int? by roleProperty(null)
    public var listSize: Int? by roleProperty(null)
    public var depth   : Int  by roleProperty(0   )
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

public class link: button()

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