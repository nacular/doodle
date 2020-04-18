package com.nectar.doodle.accessibility

import com.nectar.doodle.core.View

/**
 * Created by Nicholas Eddy on 3/28/20.
 */
interface AccessibilityManager {
    fun roleAdopted  (view: View)
    fun roleAbandoned(view: View)
}

sealed class AccessibilityRole {
    var busy    : Boolean? = null
    var disabled: Boolean? = null
}

open class RangeRole: AccessibilityRole() {
    var valueMax : Int?    = null
    var valueMin : Int?    = null
    var valueNow : Int?    = null
    var valueText: String? = null
}

class alert: AccessibilityRole()
class alertdialog: AccessibilityRole()
class button: AccessibilityRole()
class checkbox: AccessibilityRole()
class dialog: AccessibilityRole()
class gridcell: AccessibilityRole()
class link: AccessibilityRole()
class log: AccessibilityRole()
class marquee: AccessibilityRole()
class menuitem: AccessibilityRole()
class menuitemcheckbox: AccessibilityRole()
class menuitemradio: AccessibilityRole()
class option: AccessibilityRole()

class progressbar: RangeRole()

class radio: AccessibilityRole()

class scrollbar: RangeRole()

class slider: RangeRole()

class spinbutton: RangeRole()

class status: AccessibilityRole()
class tab: AccessibilityRole()
class tabpanel: AccessibilityRole()
class textbox: AccessibilityRole()
class timer: AccessibilityRole()
class tooltip: AccessibilityRole()
class treeitem: AccessibilityRole()

class combobox: AccessibilityRole()
class grid: AccessibilityRole()
class listbox: AccessibilityRole()
class menu: AccessibilityRole()
class menubar: AccessibilityRole()
class radiogroup: AccessibilityRole()
class tablist: AccessibilityRole()
class tree: AccessibilityRole()
class treegrid: AccessibilityRole()

class article: AccessibilityRole()
class columnheader: AccessibilityRole()
class definition: AccessibilityRole()
class directory: AccessibilityRole()
class document: AccessibilityRole()
class group: AccessibilityRole()
class heading: AccessibilityRole()
class img: AccessibilityRole()
class list: AccessibilityRole()
class listitem: AccessibilityRole()
class math: AccessibilityRole()
class note: AccessibilityRole()
class presentation: AccessibilityRole()
class region: AccessibilityRole()
class row: AccessibilityRole()
class rowgroup: AccessibilityRole()
class rowheader: AccessibilityRole()
class separator: AccessibilityRole()
class toolbar: AccessibilityRole()

class application: AccessibilityRole()
class banner: AccessibilityRole()
class complementary: AccessibilityRole()
class contentinfo: AccessibilityRole()
class form: AccessibilityRole()
class main: AccessibilityRole()
class navigation: AccessibilityRole()
class search: AccessibilityRole()