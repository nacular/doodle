package com.nectar.doodle.user

import com.nectar.doodle.utils.PropertyObservers

/**
 * Created by Nicholas Eddy on 9/22/19.
 */
interface UserPreferences {
    enum class ColorScheme { Light, Dark }

    val colorScheme       : ColorScheme
    val colorSchemeChanged: PropertyObservers<UserPreferences, ColorScheme>
}