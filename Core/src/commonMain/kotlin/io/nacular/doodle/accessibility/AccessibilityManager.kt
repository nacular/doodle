package io.nacular.doodle.accessibility

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.observable
import kotlin.properties.ReadWriteProperty

/**
 * Created by Nicholas Eddy on 3/28/20.
 */
@Internal
public interface AccessibilityManager {
    public fun labelChanged  (view: View)
    public fun enabledChanged(view: View)
    public fun roleAdopted   (view: View)
    public fun roleAbandoned (view: View)
    public fun roleUpdated   (view: View)
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

internal class spinbutton: RangeRole()
internal class alert: AccessibilityRole()
internal class alertdialog: AccessibilityRole()
internal class dialog: AccessibilityRole()
internal class gridcell: AccessibilityRole()
internal class link: AccessibilityRole()
internal class log: AccessibilityRole()
internal class marquee: AccessibilityRole()
internal class menuitem: AccessibilityRole()
internal class menuitemcheckbox: AccessibilityRole()
internal class menuitemradio: AccessibilityRole()
internal class option: AccessibilityRole()

internal class scrollbar: RangeRole()

internal class status: AccessibilityRole()
internal class tab: AccessibilityRole()
internal class tabpanel: AccessibilityRole()
internal class textbox: AccessibilityRole()
internal class timer: AccessibilityRole()
internal class tooltip: AccessibilityRole()
internal class treeitem: AccessibilityRole()

internal class combobox: AccessibilityRole()
internal class grid: AccessibilityRole()
internal class listbox: AccessibilityRole()
internal class menu: AccessibilityRole()
internal class menubar: AccessibilityRole()
internal class radiogroup: AccessibilityRole()
internal class tablist: AccessibilityRole()
internal class tree: AccessibilityRole()
internal class treegrid: AccessibilityRole()

internal class article: AccessibilityRole()
internal class columnheader: AccessibilityRole()
internal class definition: AccessibilityRole()
internal class directory: AccessibilityRole()
internal class document: AccessibilityRole()
internal class group: AccessibilityRole()
internal class heading: AccessibilityRole()
internal class list: AccessibilityRole()
internal class listitem: AccessibilityRole()
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