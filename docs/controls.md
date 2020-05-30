# UI Components
---------------

Doodle has several common UI components in the Controls library. Access these by adding a dependency to
the Controls library in your build file.

### build.gradle

```groovy
//...

dependencies {
     implementation "com.nectar.doodle:controls:$doodle_version"
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

val label = Label("hello")
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
---
### Button

A component that is "pressed" to trigger an action; usually with the pointer or keyboard.

```kotlin
val button = PushButton("Press Here").apply {
    fired += {
        println("Hey! That Hurt!")
    }
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
---
### ProgressBar

Represents a value within a specified range. It provides notifications when its value or range changes.

```kotlin
val progressBar = ProgressBar() // creates a bar that ranges form 0 - 100
```

?> ProgressBar is a specialization of ProgressIndicator, which should be used for more generalized
progress display (i.e. circular)

---
### Slider
---
### Spinner
---
### List
---
### Tree
---
### Table
---
### SplitPanel
---
### TabbedPanel