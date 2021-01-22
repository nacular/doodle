package io.nacular.doodle.controls.menu

import io.nacular.doodle.utils.PropertyObservers

/**
 * Created by Nicholas Eddy on 4/30/18.
 */
public interface MenuItem {
    // TODO: Naming is sub-optimal here b/c of collisions w/ View
    public val parentMenu     : MenuItem?
    public val subMenus       : Iterator<MenuItem>
    public var menuSelected   : Boolean
    public val selectedChanged: PropertyObservers<MenuItem, Boolean>
}
