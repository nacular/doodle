package io.nacular.doodle.user

import io.nacular.doodle.utils.PropertyObservers

/**
 * Created by Nicholas Eddy on 9/22/19.
 */
public interface UserPreferences {
    public enum class ColorScheme { Light, Dark }

    public val colorScheme       : ColorScheme
    public val colorSchemeChanged: PropertyObservers<UserPreferences, ColorScheme>
}