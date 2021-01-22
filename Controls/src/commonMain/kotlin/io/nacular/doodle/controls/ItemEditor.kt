package io.nacular.doodle.controls

import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.core.View

/**
 * Created by Nicholas Eddy on 5/29/19.
 */
public interface EditOperation<T> {
    public operator fun invoke(): View?
    public fun complete(): T?
    public fun cancel()
}

public interface ItemEditor<T> {
    public operator fun invoke(item: T, previous: View? = null): View
}

public class TextItemEditor: ItemEditor<String> {
    override fun invoke(item: String, previous: View?): TextField = when (previous) {
        is TextField ->                   { previous.text = item; previous }
        else         -> TextField().apply {          text = item           }
    }
}

public class BooleanItemEditor: ItemEditor<Boolean> {
    override fun invoke(item: Boolean, previous: View?): CheckBox = when (previous) {
        is CheckBox ->                  { previous.selected = item; previous }
        else        -> CheckBox().apply {          selected = item;          }
    }
}
