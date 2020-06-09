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

This is where [`Behavior`s](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Behavior.kt#L6)
come in. Views can offer deep customization by delegating rendering, hit detection and anything else to Behaviors. TabbedPanel--along
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
    override fun uninstall(view: Button) {}
}
``` 

?> The methods on `Behavior` are all optional

Behaviors support installation into a View. This gives the Behavior a chance to configure the View upon first assignment.

## Specialized Behaviors

As mentioned before, TabbedPanel delegates a lot to its Behavior. It actually exposes the fact that it is a container to it. This is done
using the [`TabbedPanelBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/panels/TabbedPanel.kt#L15)
sub interface.

Classes that implement `TabbedPanbleBehavior` are able to directly modify their panel's children and layout.

```kotlin
class MyTabbedPanelBehavior: TabbedPanelBehavior<Any> {
    override fun install(view: TabbedPanel<Any>) {
        // accessible to TabbedPanelBehavior sub classes
        view.children += object: View() {}
        view.layout    = object: Layout {
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