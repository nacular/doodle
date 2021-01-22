package io.nacular.doodle.focus

import io.nacular.doodle.core.View
import io.nacular.doodle.utils.PropertyObservers

/**
 * Created by Nicholas Eddy on 3/2/18.
 */
public interface FocusManager {
    public val focusOwner    : View?
    public val focusCycleRoot: View?
    public val focusChanged  : PropertyObservers<FocusManager, View?>

    public fun focusable(view: View): Boolean

    public fun requestFocus      (view: View)
    public fun clearFocus        (          )
    public fun moveFocusForward  (          )
    public fun moveFocusForward  (from: View)
    public fun moveFocusBackward (          )
    public fun moveFocusBackward (from: View)
    public fun moveFocusUpward   (          )
    public fun moveFocusUpward   (from: View)
    public fun moveFocusDownward (          )
    public fun moveFocusDownward (from: View)
    public fun moveFocusToDefault(          )
}

public interface FocusCycleRoot {
    public val children: List<View>
}