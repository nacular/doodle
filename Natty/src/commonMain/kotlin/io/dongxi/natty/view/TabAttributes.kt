package io.dongxi.natty.view

import io.nacular.doodle.drawing.Color

// All view colors should be Color(0xe4dfdeu), but I want to
// see a different color for each tab's view for now.

enum class TabAttributes(
    val tabName: String,
    val leftViewTitle: String,
    val centerViewTitle: String,
    val rightViewTitle: String,
    val color: Color
) {
    CASA(
        tabName = "Casa",
        leftViewTitle = "Casa",
        centerViewTitle = "",
        rightViewTitle = "",
        color = Color.Black
    ),
    ANEIS(
        tabName = "Aneis",
        leftViewTitle = "Aneis",
        centerViewTitle = "Anel",
        rightViewTitle = "Pedras",
        color = Color.Blue
    ),
    COLARES(
        tabName = "Colares",
        leftViewTitle = "Colares",
        centerViewTitle = "Colar",
        rightViewTitle = "Pingentes",
        color = Color.Brown
    ),
    ESCAPULARIOS(
        tabName = "Escapulários",
        leftViewTitle = "Escapulários",
        centerViewTitle = "Escapulário",
        rightViewTitle = "Que Texto?",
        color = Color.Red
    ),
    PULSEIRAS(
        tabName = "Pulseiras",
        leftViewTitle = "Pulseiras",
        centerViewTitle = "Pulseira",
        rightViewTitle = "Que Texto?",
        color = Color.Gray
    ),
    BRINCOS(
        tabName = "Brincos",
        leftViewTitle = "Brincos",
        centerViewTitle = "Brinco",
        rightViewTitle = "Que Texto?",
        color = Color.Green
    ),
    SOBRE(
        tabName = "Sobre",
        leftViewTitle = "Sobre",
        centerViewTitle = "",
        rightViewTitle = "",
        color = Color.Cyan
    )
}

