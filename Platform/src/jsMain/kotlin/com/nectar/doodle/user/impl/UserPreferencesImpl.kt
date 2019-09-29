package com.nectar.doodle.user.impl

import com.nectar.doodle.user.UserPreferences
import com.nectar.doodle.user.UserPreferences.ColorScheme
import com.nectar.doodle.user.UserPreferences.ColorScheme.Dark
import com.nectar.doodle.user.UserPreferences.ColorScheme.Light
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import org.w3c.dom.Window

/**
 * Created by Nicholas Eddy on 9/22/19.
 */
class UserPreferencesImpl(private val window: Window): UserPreferences {
    private val isDark = window.matchMedia("(prefers-color-scheme: dark)")
    private var old    = colorScheme

    init {
        println(isDark)

        isDark.addListener {
            println("isDark changed")
            (colorSchemeChanged as PropertyObserversImpl)(old, colorScheme)

            old = colorScheme
        }
    }

    override val colorScheme get() = when {
            isDark.matches -> Dark
            else           -> Light
        }

    override val colorSchemeChanged: PropertyObservers<UserPreferences, ColorScheme> by lazy { PropertyObserversImpl<UserPreferences, ColorScheme>(this) }
}