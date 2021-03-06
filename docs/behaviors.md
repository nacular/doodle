# Render Delegation
-------------------

It is common to make a View's behavior and presentation configurable. In many cases this happens through properties like colors, fonts, etc.

```kotlin
val textField = TextField().apply {
    backgroundColor = Darkgray
    foregroundColor = White
    borderVisible   = false
}
```
---
## Deeper customization

Sometimes a View needs to support more complex customization. Take a TabbedPanel for example. The number of configurations is
fairly open-ended; and the API would be needlessly complex if it tried to encompass everything.

This is where a [`Behavior`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Behavior.kt#L6)
comes in handy. Views can offer deep customization by delegating rendering, hit detection and anything else to Behaviors. TabbedPanel--along
with Textfield and many other controls--actually does this.

### Implementing a Behavior

Behaviors offer a few common capabilities that help with View customization. You create one by implementing the [`Behavior`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Behavior.kt#L6)
interface, or a sub-type of it depending on the target View.

```kotlin
class MyBehavior: Behavior<Button> {
    override fun install(view: Button) {}
    override fun render(view: Button, canvas: Canvas) {}
    override fun contains(view: Button, point: Point) = point in view.bounds
    override fun clipCanvasToBounds(view: Button) = true
    override fun mirrorWhenRightToLeft(view: T) = view.mirrorWhenRightLeft
    override fun uninstall(view: Button) {}
}
``` 

?> The methods on `Behavior` are all optional

Behaviors support installation and uninstallation to and from Views. This gives each Behavior a chance to configure the target
View upon first assignment and cleanup when removed.

### Delegating to a Behavior

View subtypes need to manage behaviors directly. Kotlin does not have self types, so the `View` base class cannot have a
`behavior<Self>` to make this easier.

```kotlin
class MyView: View() {
// ...
    var behavior: Behavior<MyView>? by behavior()
}
```

However, View subtypes can use the [`behvaior`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L914)
delegate to guarantee proper installation and uninstallation. This delegate also ensures a Behavior's overrides for things like
`clipCanvasToBounds` or `mirrorWhenRightToLeft` are not missed during installation.

## Specialized Behaviors

As mentioned before, TabbedPanel delegates a lot to its Behavior. It actually exposes the fact that it is a container to it. This is done
using the [`TabbedPanelBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/panels/TabbedPanel.kt#L15)
sub interface.

**io.nacular.doodle.controls.panels.TabbedPanel.kt**

```kotlin
abstract class TabbedPanelBehavior<T>: Behavior<TabbedPanel<T>> {
    // 
    val TabbedPanel<T>.children         get() = this._children
    var TabbedPanel<T>.insets           get() = this._insets;           set(new) { _insets           = new }
    var TabbedPanel<T>.layout           get() = this._layout;           set(new) { _layout           = new }
    var TabbedPanel<T>.isFocusCycleRoot get() = this._isFocusCycleRoot; set(new) { _isFocusCycleRoot = new }
    
    inline operator fun TabbedPanel<T>.plusAssign (view: View) = children.plusAssign (view)
    inline operator fun TabbedPanel<T>.minusAssign(view: View) = children.minusAssign(view)
}

class TabbedPanel<T> {
    //...

    // Expose container APIs for behavior
    internal val _children         get() = children
    internal var _insets           get() = insets; set(new) { insets = new }
    internal var _layout           get() = layout; set(new) { layout = new }
    internal var _isFocusCycleRoot get() = isFocusCycleRoot; set(new) { isFocusCycleRoot = new }
    
    // ...
}
```

Classes that implement `TabbedPanbleBehavior` are able to directly modify their panel's children and layout.

```kotlin
class MyTabbedPanelBehavior: TabbedPanelBehavior<Any> {
    override fun install(view: TabbedPanel<Any>) {
        // accessible to TabbedPanelBehavior sub classes
        view += view {}
        view.layout = object: Layout {
            override fun layout(container: PositionableContainer) {}
        }
    }

    override fun uninstall(view: TabbedPanel<Any>) {
        view.children.clear()
        view.layout = null
    }

    // ...
}
```

```kotlin
val tabbedPanel = TabbedPanel(/*...*/).apply {
    behavior = MyTabbedPanelBehavior()
}
```

This provides great flexibility when defining the presentation and behavior for TabbedPanels. You can do similar things with
Views in your app.

?> You can automatically style Views using [**Themes**](themes.md)