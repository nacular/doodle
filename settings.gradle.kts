rootProject.name = "doodle"

include(
"Animation",
"Browser",
"Controls",
"Core",
"Themes",
"Desktop",
"Modal",
"Contacts"
)

project(":Animation").name = "animation"
project(":Browser"  ).name = "browser"
project(":Controls" ).name = "controls"
project(":Core"     ).name = "core"
project(":Themes"   ).name = "themes"
project(":Desktop"  ).name = "desktop"
// Slowly add tutorial example modules
project(":Modal").name = "modal"
project(":Contacts").name = "contacts"
