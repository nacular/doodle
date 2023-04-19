package io.nacular.doodle.controls.menu

import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 4/30/18.
 */
@Deprecated(message = "Use popupmenu.Menu instead")
public interface MenuSelectionManager {
    public var selectedPath    : Path<MenuItem>?
    public val selectionChanged: PropertyObservers<MenuSelectionManager, Path<MenuItem>?>
}

@Deprecated(message = "Use popupmenu.Menu instead")
public class MenuSelectionManagerImpl: MenuSelectionManager {
    override val selectionChanged: PropertyObservers<MenuSelectionManager, Path<MenuItem>?> by lazy { PropertyObserversImpl<MenuSelectionManager, Path<MenuItem>?>(this) }

    override var selectedPath: Path<MenuItem>? = null
        set(new) {
            if (new == selectedPath) {
                return
            }

            val old       = field
            var diffStart = 0

            if (new != null) {
                field?.let {
                    val minLength = min(it.depth + 1, new.depth + 1)

                    while (diffStart < minLength && it[diffStart] === new[diffStart]) {
                        diffStart++
                    }
                }
            }

            field?.let {
                for (i in it.depth - 1 downTo diffStart) {
                    it[i].menuSelected = false
                }
            }

            if (new != null) {
                for (i in diffStart until new.depth) {
                    new[i].menuSelected = true
                }
            }

            field = new

            (selectionChanged as PropertyObserversImpl<MenuSelectionManager, Path<MenuItem>?>)(old, new)
        }
}
