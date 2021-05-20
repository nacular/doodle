rootProject.name = "doodle"

include(
"Animation",
"Browser",
"Controls",
"Core",
"Themes",
"Desktop"
)

project(":Animation").name = "animation"
project(":Browser"  ).name = "browser"
project(":Controls" ).name = "controls"
project(":Core"     ).name = "core"
project(":Themes"   ).name = "themes"
project(":Desktop"  ).name = "desktop"
