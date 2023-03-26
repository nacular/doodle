package io.dongxi.natty.tabbedpanel

import io.nacular.doodle.drawing.Color

// All view colors should be Color(0xe4dfdeu), but I want to
// see a different color for each tab's view for now.

enum class TabAttributes(val tabName: String, val color: Color) {
    CASA("Casa", Color.Black),
    ANEIS("Aneis", Color.Blue),
    COLARES("Colares", Color.Brown),
    ESCAPULARIOS("Escapulários", Color.Red),
    PULSEIRAS("Pulseiras", Color.Gray),
    BRINCOS("Brincos", Color.Green),
    SOBRE("Sobre", Color.Cyan)
}

