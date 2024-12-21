package io.nacular.doodle.user.impl

import io.nacular.doodle.dom.Window
import io.nacular.doodle.user.UserPreferences
import io.nacular.doodle.user.UserPreferences.ColorScheme
import io.nacular.doodle.user.UserPreferences.ColorScheme.Dark
import io.nacular.doodle.user.UserPreferences.ColorScheme.Light
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl

/**
 * Created by Nicholas Eddy on 9/22/19.
 */
internal class UserPreferencesImpl(window: Window): UserPreferences {
    private val isDark = window.matchMedia("(prefers-color-scheme: dark)")
    private var old    = colorScheme

    init {
        isDark.addListener {
            (colorSchemeChanged as PropertyObserversImpl)(old, colorScheme)

            old = colorScheme
        }
    }

    override val colorScheme: ColorScheme
        get() = when {
            isDark.matches -> Dark
            else           -> Light
        }

    override val colorSchemeChanged: PropertyObservers<UserPreferences, ColorScheme> = PropertyObserversImpl<UserPreferences, ColorScheme>(this)
}