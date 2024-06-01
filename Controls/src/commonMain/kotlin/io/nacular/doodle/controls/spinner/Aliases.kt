package io.nacular.doodle.controls.spinner

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.core.View

@Deprecated("Use MutableSpinButton", replaceWith = ReplaceWith("MutableSpinButton<T, M>", imports = arrayOf("io.nacular.doodle.controls.spinbutton.MutableSpinButton")))
public typealias MutableSpinner<T, M> = MutableSpinButton<T, M>

@Deprecated("Use MutableSpinButtonModel", replaceWith = ReplaceWith("MutableSpinButtonModel<T>", imports = arrayOf("io.nacular.doodle.controls.spinbutton.MutableSpinButtonModel")))
public typealias MutableSpinnerModel<T> = MutableSpinButtonModel<T>

@Deprecated("Use SpinButton", replaceWith = ReplaceWith("SpinButton<T, M>"))
public typealias Spinner<T, M> = SpinButton<T, M>

@Deprecated("Use SpinButtonModel", replaceWith = ReplaceWith("SpinButtonModel<T>"))
public typealias SpinnerModel<T> = SpinButtonModel<T>

@Deprecated("Use SpinButtonBehavior", replaceWith = ReplaceWith("SpinButtonBehavior<T, M>"))
public typealias SpinnerBehavior<T, M> = SpinButtonBehavior<T, M>

@Deprecated("Use SpinButtonEditor", replaceWith = ReplaceWith("SpinButtonEditor<T>", imports = arrayOf("io.nacular.doodle.controls.spinbutton.SpinButtonEditor")))
public typealias SpinnerEditor<T> = SpinButtonEditor<T>

@Deprecated("Use MutableSpinButtonBehavior", replaceWith = ReplaceWith("MutableSpinButtonBehavior<T, M>", imports = arrayOf("io.nacular.doodle.controls.spinbutton.MutableSpinButtonBehavior")))
public typealias MutableSpinnerBehavior<T, M> = MutableSpinButtonBehavior<T, M>

@Deprecated("Use CommonSpinButtonModel", replaceWith = ReplaceWith("CommonSpinButtonModel<T>", imports = arrayOf("io.nacular.doodle.controls.spinbutton.CommonSpinButtonModel")))
public typealias CommonSpinButtonModel<T> = CommonSpinnerModel<T>

@Deprecated("Use IntSpinButtonModel", replaceWith = ReplaceWith("IntSpinButtonModel", imports = arrayOf("io.nacular.doodle.controls.spinbutton.IntSpinButtonModel")))
public typealias IntSpinnerModel = IntSpinButtonModel

@Deprecated("Use MutableIntSpinButtonModel", replaceWith = ReplaceWith("MutableIntSpinButtonModel", imports = arrayOf("io.nacular.doodle.controls.spinbutton.MutableIntSpinButtonModel")))
public typealias MutableIntSpinnerModel = MutableIntSpinButtonModel

@Deprecated("Use LongSpinButtonModel", replaceWith = ReplaceWith("LongSpinButtonModel", imports = arrayOf("io.nacular.doodle.controls.spinbutton.LongSpinButtonModel")))
public typealias LongSpinnerModel = LongSpinButtonModel

@Deprecated("Use MutableLongSpinButtonModel", replaceWith = ReplaceWith("MutableLongSpinButtonModel", imports = arrayOf("io.nacular.doodle.controls.spinbutton.MutableLongSpinButtonModel")))
public typealias MutableLongSpinnerModel = MutableLongSpinButtonModel

@Deprecated("Use ListSpinButtonModel", replaceWith = ReplaceWith("ListSpinButtonModel<T, L>", imports = arrayOf("io.nacular.doodle.controls.spinbutton.ListSpinButtonModel")))
public typealias ListSpinnerModel<T, L> = ListSpinButtonModel<T, L>

@Deprecated("Use MutableListSpinButtonModel", replaceWith = ReplaceWith("MutableListSpinButtonModel<T>", imports = arrayOf("io.nacular.doodle.controls.spinbutton.MutableListSpinButtonModel")))
public typealias MutableListSpinnerModel<T> = MutableListSpinButtonModel<T>

@Deprecated("Use spinButtonEditor", replaceWith = ReplaceWith("spinButtonEditor(block)", imports = arrayOf("io.nacular.doodle.controls.spinbutton.spinButtonEditor")))
public inline fun <T> spinnerEditor(crossinline block: (spinner: MutableSpinButton<T, *>, value: T, current: View) -> EditOperation<T>): SpinButtonEditor<T> = spinButtonEditor(block)