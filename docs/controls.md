# UI Components
---------------

Doodle has several common UI components in the Controls library. Access these by adding a dependency to
`com.nectar.doodle:controls:$doodle_version` in your build.gradle file.

Most of these components rely entirely on their `Behavior` for rendering. Moreover, they do not have
defaults for them to minimize bundle size. So you need to specify them explicitly or use a [**Theme**](themes.md) that provides
them for the controls you use. 

## Label

A simple static text component. You can construct it using the `LableFactory`. 

```kotlin
val Label: LabelFactory // injectable to app and Views

val label = Label("hello")
``` 

## TextField

A component for simple text input.

```kotlin
val textField = TextField().apply {
    mask = '*'
    fitText = true
    borderVisible = false
}
```

## Button

A component that is "pressed" to trigger an action; usually with the pointer or keyboard.

```kotlin
val button = PushButton("Press Here").apply {
    fired += {
        println("Hey! That Hurt!")
    }
}
```

There are several types of buttons available, including ToggleButton, CheckBox, and RadioButton.

## Photo

Images in Doodle are not Views, they are more like text, in that you render them directly to a Canvas.
The Photo component provides a simple wrapper around an Image.

```kotlin
val photo = Photo(image).apply {
    size = Size(100, 200)
}
``` 

## ProgressBar

## Slider

## Spinner

## List

## Tree

## Table

## SplitPanel

## TabbedPanel

