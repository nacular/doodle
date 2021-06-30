package io.nacular.doodle.user.impl

import io.nacular.doodle.user.UserPreferences
import io.nacular.doodle.user.UserPreferences.ColorScheme
import io.nacular.doodle.user.UserPreferences.ColorScheme.Light
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl

// FIXME: Implement properly
public class UserPreferencesImpl: UserPreferences {
    override val colorScheme: ColorScheme = Light

    override val colorSchemeChanged: PropertyObservers<UserPreferences, ColorScheme> by lazy { PropertyObserversImpl<UserPreferences, ColorScheme>(this) }
}