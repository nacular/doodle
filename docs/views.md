# Creating Views
----------------

### Views are the building blocks of Doodle apps.

A View displays content to the screen and enables interactions with the user. You create a one by extending
the [`View`]() base class or instantiating an inline object:

```kotlin
class MyView: View() {
    // ...
}

val view = object: View() {
    // ...
}
```

### Views encapsulate state and handle rendering updates to the screen.

Here is more useful example of holding some user data

```kotlin
class UserInfo(
        private val textMetrics:
        TextMetrics,
        name: String,
        age: Int): View() {

    var name = name; set(value) {
        field = value
        rerender()
    }

    var age = age; set(value) {
        field = value
        rerender()
    }
    
    override fun render(canvas: Canvas) {
        canvas.text("name: $name", Origin, ColorBrush(black))
        canvas.text("age : $age", Point(0.0, textMetrics.height(name)),
            ColorBrush(black))
    }
}
```
?> Don't worry about the `render` method yet; we'll cover that later.

### Top-level Views

Doodle apps contain a hierarchy of Views working together; with the [`Display`]() being the root ancestor. You display
a top-level View like this:

```kotlin
class MyApp(display: Display): View() {
    init {
        display.children += view
    }
    // ...
}
```

The `display` may be associated with the page or element for stand-alone apps, or an [`ApplicationView`]() for nested apps.

### Views Can Have Children

Apps build hierarchies by nesting Views within one another. The [`View`]() class keeps its children `protected`. This helps
encapsulate state better and avoids misuse for more complex views.

Let's consider a tabbed panel. An implementation might use a list of Views to represent the tab row along with those representing
each tab's contents. Doodle naturally handles this by letting you selectively expose the View internals to callers.

```kotlin
class MyBasicTabbedPanel: View() {
    private val tabHeader = Box() // simple container

    var selectedTab: View? = null
    
    init {
        children += tabHeader // children accessible to sub-class
        
        // setup layout
    }

    fun add(tab: View) {/* add to tabHeader, update selection ... */}

    fun remove(tab: View) {/* remove from tabHeader, update selection ... */}
}
```

This class keeps the `children` property internal and exposes a more robust API via `add`, `remove`, and `selectedTab`.