package com.nectar.doodle.controls

import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.core.View

/**
 * Created by Nicholas Eddy on 5/29/19.
 */
interface EditOperation<T> {
    operator fun invoke(): View?
    fun complete(): T?
    fun cancel()
}

interface ItemEditor<T> {
    operator fun invoke(item: T, previous: View? = null): View
}

class TextItemEditor: ItemEditor<String> {
    override fun invoke(item: String, previous: View?) = when (previous) {
        is TextField ->                   { previous.text = item; previous }
        else         -> TextField().apply {          text = item           }
    }
}

class BooleanItemEditor: ItemEditor<Boolean> {
    override fun invoke(item: Boolean, previous: View?) = when (previous) {
        is CheckBox ->                  { previous.selected = item; previous }
        else        -> CheckBox().apply {          selected = item;          }
    }
}
