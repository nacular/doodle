package com.zinoti.jaz.ui

import com.zinoti.jaz.core.Gizmo


interface UIManager {
    val installedTheme  : ThemeInfo
    val availableThemes : List<ThemeInfo>

    fun installUI   (gizmo: Gizmo, aResponse: UIResponse)
    fun revalidateUI(gizmo: Gizmo)
    fun installTheme(theme: ThemeInfo)

    interface ThemeInfo: Theme {
        val name      : String
//        val themeClass: Class<out Theme>
    }

    interface UIResponse {
//        fun setUI(aUI: UI<*>)
    }

    fun <T> getDefaultValue(aName: String): T
}
