rootProject.name = "doodle"

include(
    "Animation",
    "Browser",
    "Controls",
    "Core",
    "Themes",
    "Desktop",
    "Modal",
    "Contacts",
    "ContactsRunner",
    "Photos",
    "PhotosRunner",
    "TabStrip",
    "TabStripRunner"
)

project(":Animation").name = "animation"
project(":Browser").name = "browser"
project(":Controls").name = "controls"
project(":Core").name = "core"
project(":Themes").name = "themes"
project(":Desktop").name = "desktop"
// Slowly add tutorial example modules
project(":Modal").name = "modal"
project(":Contacts").name = "contacts"
project(":ContactsRunner").name = "contactsrunner"
project(":Photos").name = "photos"
project(":PhotosRunner").name = "photosrunner"
project(":TabStrip").name = "tabstrip"
project(":TabStripRunner").name = "tabstriprunner"

