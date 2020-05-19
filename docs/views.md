# Creating Views
----------------

### Views are the building blocks of Doodle apps.

A View displays content to the screen and enables interactions with the user. You create a one by extending
the [`View`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L62)
base class or instantiating an inline object:

```kotlin
class MyView: View() {
    // ...
}

val view = object: View() {
    // ...
}
```

### Views encapsulate state and handle rendering updates to the screen.

Here is more useful example of holding and displaying some user data.

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
?> Don't worry about the `render` method yet; we cover that [later](rendering.md).

### Top-level Views

Doodle apps contain a hierarchy of Views working together; with the [**Display**](display.md?id=the-display-is-an-apps-root-container)
being the root ancestor. You display a top-level View like this:

```kotlin
class MyApp(display: Display): View() {
    init {
        display.children += view
    }
    // ...
}
```

### Views Can Have Children

Most apps consist of hierarchies with Views nested inside one another. Doodle apps are no different: Views
support nesting [`children`](https://github.com/pusolito/doodle/blob/master/Core/src/commonMain/kotlin/com/nectar/doodle/core/View.kt#L348).
However, this list--and other traits related to being a container of Views--is `protected` to improve encapsulate and API control.

Consider a split panel. Conceptually, it should have no more than 2 children; but it might make sense to have more: i.e. a handle.
Doodle makes this easy letting you selectively expose a View's internals to callers.

```kotlin
class VSplitPanel: View() {
    val left: View? = null
        set(new) {
            if (new == field) { return }

            field?.let { children -= it }
            field = new
            field?.let { children += it }
    
            // notify of change
        }
    
    val right: View? = null
        // ..

    private val handle: View // private View for splitter

    init {
        children += handle // add handle to children
    }
}
```

This design prevents direct access to the panel's `children`, which side-steps many usability issues.  It also presents are more
intuitive API: left, right are fairly self-documenting, and much better than children[0] and children[1].