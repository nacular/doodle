# Creating Views
----------------

## App building blocks

Views are the building blocks of Doodle apps. They encapsulate state, display content on the screen and respond to user input. Apps
typically contain many View classes and have lots of View instances at runtime.

You create a new View by extending the [`View`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L61)
base class or instantiating an inline object:

```kotlin
class MyView: View() {
    // ...
}

val view = object: View() {
    // ...
}
```

## State and rendering

Here is more useful example of a View that holds some user data (a name and age). This View tracks this state and monitors it for changes. It
also keeps the user informed of its state by rendering any changes to its internal state.

```kotlin
class UserInfo(
        private val textMetrics: TextMetrics,
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
        canvas.text("name: $name", Origin, ColorFill(Black))
        canvas.text("age : $age", Point(0.0, textMetrics.height(name)),
            ColorFill(Black))
    }
}
```

```doodle
{
    "border": false,
    "height": "120px",
    "run"   : "DocApps.userInfo"
}
```

?> Don't worry about the `render` method yet; we cover that [later](rendering.md).

---
## View Hierarchy

### Apps can have any number of top-level Views

Doodle apps contain a hierarchy of Views working together; with the [**Display**](display.md?id=the-display-is-an-apps-root-container)
being the root ancestor--though it is not a View. You display a top-level View like this:

```kotlin
class MyApp(display: Display): View() {
    init {
        display += view
    }
    // ...
}
```

### Views can also have children

Most apps consist of hierarchies with Views nested inside one another. Doodle apps are no different: Views
support nesting [`children`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L360).
However, this list--and other traits related to being a container--is `protected` to improve encapsulation and API control.

Consider a split panel. It is reasonable to think about it as having a left and right child (ignoring orientation for now).
However, an implementation of this concept might choose to have an additional child to represent the splitter. This choice is an
internal detail of the implementation that would be leaked if the `children` list were public. Worse, a caller could remove the splitter
or add more children than expected and break the behavior.

Doodle helpds with these design challenges by letting you selectively expose a View's internals to callers.

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
        // ...

    private val handle: View // private View for splitter

    init {
        children += handle // add handle to children
    }
    
    // ...
}
```

This design prevents direct access to the panel's `children`, which side-steps many issues. It also presents are more
intuitive and reliable API. `left` and `right` are fairly self-documenting compared to `children[n]` and `children[m]`. Moreover,
the panel is able to encapsulate the fact that it uses additional Views for presentation. 