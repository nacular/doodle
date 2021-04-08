# Accessibility
---------------

Making truly accessible apps is complex and requires familiarity with a wide range of concepts. The
[Web Content Accessibility Guidelines](https://www.w3.org/WAI/intro/wcag) provide recommendations for web apps.

Doodle supports accessibility through a number of features. Simply include the
[`AccessibilityModule`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/application/Modules.kt#L104)
in your app to fully enable them.

```kotlin
class MyApp(/*...*/) {
    // use accessibility features
}

fun main () {
    // Include the AccessibilityModule to enable features
    application(root, modules = listOf(AccessibilityModule)) {
        MyApp(/*...*/)
    }
}
```

## Descriptive Text

### [`accessibilityLabel`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L89)

Authors can provide short, descriptive text that is used by assistive technologies to announce a View when it is selected.
This property helps in cases where a View contains no meaningful text or has no [`toolTipText`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L347).

```kotlin
val button = PushButton("x").apply {
    accessibilityLabel = "Close the window"
}
```

### [`accessibilityLabelProvider`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L97)

In many cases the app presents descriptive text to the user directly using other Views, like labels for a text fields. 
The `accessibilityLabelProvider` points to another View that should be used as a "label" for the current one.

```kotlin
val label     = Label("Enter your name")
val textField = TextField().apply {
    accessibilityLabelProvider = label
}
```

!> Views can be linked this way at any time, even if they are not both currently displayed. Doodle will track the relationship,
and surface it to assistive technologies if the Views are simultaneously displayed.

### [`accessibilityDescriptionProvider`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L105)

Labels should be short descriptive names for a View. But it is possible to provide more detailed descriptions as well via the
`accessibilityDescriptionProvider`. This property behaves like `accessibilityLabelProvider`, but is intended for longer, more detailed
text that describes the View. 


## Widget Roles

Authors can indicate that a View plays a well-defined role as a widget by tagging it with an accessibility role. This enables
assistive technologies to change the presentation of the View to the user as she navigates a scene. This is done by setting the View's 
[`accessibilityRole`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L80)

Here is an example of creating a View that will serve as a [`button`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/accessibility/AccessibilityManager.kt#L59).

```kotlin
class CustomButton: View(accessibilityRole = button()) {
    // handle key events, etc.
}
```

This View will now be treated as a button by accessibility technologies (i.e. screen readers). The `button` role itself does not have
additional properties, so simply adopting it is sufficient.

Other roles have state and must be synchronized with the View to ensure proper assistive support.

```kotlin
class CustomToggle(private val role: togglebutton = togglebutton()): View(accessibilityRole = role) {
    var selected by observable(false) { old, new ->
        role.pressed = new
    }
}
```

Many of the widgets in the [`Controls`](https://github.com/nacular/doodle/tree/master/Controls) library ship with accessibility 
support (though this continues to improve). The library also provides bindings for some roles and models, which makes it easier to synchronize
roles with their widgets. [`ValueSlider`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/range/ValueSlider.kt#L13), 
for example, binds its role to the [`ConfinedValueModel`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ConfinedRangeModel.kt#L24)
that underlies it. This way the role and View are always in sync.

```kotlin
public abstract class ValueSlider private constructor(
                 model: ConfinedValueModel<Double>,
    protected val role: slider = slider()
): View(role) {
    // ...

    // delegate ensures old linkage is broken when new binding established
    private var roleBinding by binding(role.bind(model))

    public var model: ConfinedValueModel<Double> = model
        set(new) {
            // ..

            field = new.also {
                // ..
                roleBinding = role.bind(it) // link role to new model
            }
        }

    // ...
}
``` 