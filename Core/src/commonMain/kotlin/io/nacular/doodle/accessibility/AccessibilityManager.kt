package io.nacular.doodle.accessibility

import io.nacular.doodle.core.View

/**
 * Created by Nicholas Eddy on 3/28/20.
 */
public interface AccessibilityManager {
    public fun roleAdopted  (view: View)
    public fun roleAbandoned(view: View)
}

public sealed class AccessibilityRole {
    public var busy    : Boolean? = null
    public var disabled: Boolean? = null
}

public open class RangeRole: AccessibilityRole() {
    public var valueMax : Int?    = null
    public var valueMin : Int?    = null
    public var valueNow : Int?    = null
    public var valueText: String? = null
}

public class alert: AccessibilityRole()
public class alertdialog: AccessibilityRole()
public class button: AccessibilityRole()
public class checkbox: AccessibilityRole()
public class dialog: AccessibilityRole()
public class gridcell: AccessibilityRole()
public class link: AccessibilityRole()
public class log: AccessibilityRole()
public class marquee: AccessibilityRole()
public class menuitem: AccessibilityRole()
public class menuitemcheckbox: AccessibilityRole()
public class menuitemradio: AccessibilityRole()
public class option: AccessibilityRole()

public class progressbar: RangeRole()

public class radio: AccessibilityRole()

public class scrollbar: RangeRole()

public class slider: RangeRole()

public class spinbutton: RangeRole()

public class status: AccessibilityRole()
public class tab: AccessibilityRole()
public class tabpanel: AccessibilityRole()
public class textbox: AccessibilityRole()
public class timer: AccessibilityRole()
public class tooltip: AccessibilityRole()
public class treeitem: AccessibilityRole()

public class combobox: AccessibilityRole()
public class grid: AccessibilityRole()
public class listbox: AccessibilityRole()
public class menu: AccessibilityRole()
public class menubar: AccessibilityRole()
public class radiogroup: AccessibilityRole()
public class tablist: AccessibilityRole()
public class tree: AccessibilityRole()
public class treegrid: AccessibilityRole()

public class article: AccessibilityRole()
public class columnheader: AccessibilityRole()
public class definition: AccessibilityRole()
public class directory: AccessibilityRole()
public class document: AccessibilityRole()
public class group: AccessibilityRole()
public class heading: AccessibilityRole()
public class img: AccessibilityRole()
public class list: AccessibilityRole()
public class listitem: AccessibilityRole()
public class math: AccessibilityRole()
public class note: AccessibilityRole()
public class presentation: AccessibilityRole()
public class region: AccessibilityRole()
public class row: AccessibilityRole()
public class rowgroup: AccessibilityRole()
public class rowheader: AccessibilityRole()
public class separator: AccessibilityRole()
public class toolbar: AccessibilityRole()

public class application: AccessibilityRole()
public class banner: AccessibilityRole()
public class complementary: AccessibilityRole()
public class contentinfo: AccessibilityRole()
public class form: AccessibilityRole()
public class main: AccessibilityRole()
public class navigation: AccessibilityRole()
public class search: AccessibilityRole()