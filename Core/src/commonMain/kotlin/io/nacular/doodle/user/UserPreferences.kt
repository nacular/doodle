package io.nacular.doodle.user

import io.nacular.doodle.utils.PropertyObservers

/**
 * Created by Nicholas Eddy on 9/22/19.
 */
interface UserPreferences {
    enum class ColorScheme { Light, Dark }

    val colorScheme       : ColorScheme
    val colorSchemeChanged: PropertyObservers<UserPreferences, ColorScheme>
}