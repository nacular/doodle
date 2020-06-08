# UI Components
---------------

Doodle has several common UI components in the Controls library. Access these by adding a dependency to
the Controls library in your build file.

### build.gradle

```groovy
//...

dependencies {
     implementation "io.nacular.doodle:controls:$doodle_version"
}

//...
```

Most of these components rely entirely on their `Behavior` for rendering. Moreover, they do not have
defaults for them to minimize bundle size. So you need to specify them explicitly or use a [**Theme**](themes.md) that provides
them for the controls you use. 
---
### Label

Holds and displays static text with support for basic styling. You can construct it using the `LableFactory`. 

```kotlin
val Label: LabelFactory // injectable to app and Views

val label = Label("Some Text")
``` 

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.label"
}
```

---
### TextField

Provides simple (un-styled) text input.

```kotlin
val textField = TextField().apply {
    mask = '*'
    fitText = true
    borderVisible = false
}
```
```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.textField"
}
```

---
### Button

A component that is "pressed" to trigger an action; usually with the pointer or keyboard.

```kotlin
val button = PushButton("Button").apply {
    fired += {
        println("Hey! That Hurt!")
    }
}
```

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.button"
}
```

?> There are several types of buttons available, including ToggleButton, CheckBox, and RadioButton.

---
### Photo

Images in Doodle are not Views, they are more like text, in that you render them directly to a Canvas.
The Photo component provides a simple wrapper around an Image.

```kotlin
val photo = Photo(image).apply {
    size = Size(100, 200)
}
```

```doodle
{
    "height": "400px",
    "run"   : "DocApps.photo"
}
```
 
---
### ProgressBar

Represents a value within a specified range. It provides notifications when its value or range changes.

```kotlin
val progressBar = ProgressBar() // creates a bar that ranges form 0 - 100
```

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.progress"
}
```

?> ProgressBar is a specialization of ProgressIndicator, which should be used for more generalized
progress display (i.e. circular)

---
### Slider

Sliders hold a value within a specified range and allow the user to change the value.

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.slider"
}
```

---
### Spinner

Spinners let you represent a list of items where only one is visible (selected) at a time. They work well when the list of options
is relatively small, or the input is an incremental value: like the number of items to purchase.

```kotlin
val spinner1 = Spinner(1..9 step 2)
val spinner2 = Spinner(listOf("Monday", "Tuesday", "Wednesday"))
```

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.spinner"
}
```

---
### List

The List control is a visual analog to the list data structure. It is an ordered collection of items with random
access to its members. It is also readonly like the data structure.

You need 2 things to create a List: a [`ListModel`](), and [`IndexedItemVisualizer`]().

?> You also need to provide a Behavior or use a Theme with one since List delegates rendering.

```kotlin
val textVisualizer: TextItemVisualizer

val list = List(listOf(
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
    "Sunday"),
    itemVisualizer = ignoreIndex(textVisualizer))
```

This creates a list using a factory that takes a list collection and creates a ListModel from it. 

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.list"
}
```

---
### Tree

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.tree"
}
```

---
### Table

```doodle
{
    "border": false,
    "height": "400px",
    "run"   : "DocApps.table"
}
```

---
### SplitPanel

```doodle
{
    "border": false,
    "height": "400px",
    "run"   : "DocApps.splitPanel"
}
```

---
### TabbedPanel

```kotlin
val object1: View
val object2: View
val object3: View
val object4: View

// Each Tab just shows the View
val visualizer = object: ItemVisualizer<View> {
    override fun invoke(item: View, previous: View?) = item
}

val panel = TabbedPanel(visualizer, object1, object2, object3, object4).apply {
    // disable Themes since specifying Behavior directly
    acceptsThemes = false

    // BasicTabbedPanelBehavior uses text for each tab 
    behavior = BasicTabbedPanelBehavior(
        BasicTabProducer(
            textMetrics,
            namer = { _,item,_ ->
                when (item) {
                    object1 -> "Circle"
                    object2 -> "Second Tab"
                    object3 -> "Cool Photo"
                    object4 -> "Tab 4"
                    else    -> "Unknown"
                }
            })) { panel, producer ->
                // override the default tab container with one that animates
                AnimatingTabContainer(animator, panel, producer)
            }
}
```

```doodle
{
    "border": false,
    "height": "400px",
    "run"   : "DocApps.tabbedPanel"
}
```