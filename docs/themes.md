# Themes
--------

Doodle apps can use [`Themes`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/theme/Theme.kt#L16) 
to create a consistent look and behavior across their Views. Doodle has built-in support for a Native and Basic theme
(which has a light and dark variant). The [`NativeTheme`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/theme/native/NativeTheme.kt#L47) 
styles controls like buttons, text fields, and scroll panels using the browser
default styles and behaviors. The [`BasicTheme`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/BasicTheme.kt#L76) 
provides a customizable foundation to further build on.

```doodle
{
    "border": false,
    "height": "400px",
    "run"   : "DocApps.themes"
}
```


## How Themes work

Themes implement a simple interface that allows them to process the entire View graph and apply style and behavior changes.
The API is as follows:

```kotlin
interface Theme {
    fun install(display: Display, all: Sequence<View>)
}
```

Doodle calls the `install` method when applying a Theme for the first time, and when Views are added to the Display after
installation. It provides the `Display` and a sequence of displayed Views. The Theme is free to customize both.

---
## ThemeManager

Themes are handles by the [`ThemeManager`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/theme/Theme.kt#L31). 
It provides an API for selecting the active Theme. Inject it into your app to work with Themes.

```kotlin
class MyApp(display: Display,
            manager: ThemeManager,
            theme  : NativeTheme): Application {

    init {
        manager.selected = theme
    }
}

fun main() {
    application(modules = listOf(NativeTheme)) {
        MyApp(instance(), instance(), instance())
    }
}
``` 

This app installs the NativeTheme, which is available in a bundle of the same name. That bundle also includes the
common ThemeModule, which provides access to the ThemeManager.

---
## Bundle Size

Themes can lead to larger bundle sizes than expected depending on how they are implemented. Take the following
for example.

```kotlin
class CustomTheme: Theme {
    fun install(display: Display, all: Sequence<View>) = all.forEach {
        when (it) {
            is Slider      -> { it.behavior = /*...*/ }
            is PushButton  -> { it.behavior = /*...*/ }
            is ProgressBar -> { it.behavior = /*...*/ }
            // ...
        }
    }
}
```
!> Themes defined this way are not very portable due to their heavy bundle cost

```kotlin
class MyApp(display: Display,
            manager: ThemeManager,
            theme  : NativeTheme): Application {
            
    init {
        manager.selected = theme

        display.children += PushButton()
    }
}
```

`CustomTheme`'s static implementation leads to lots of dependencies that are not used in `MyApp`. This includes the View
and Behavior classes it uses.

## Dynamic Themes

Doodle addresses this concern with the [`DynamicTheme`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/adhoc/DynamicTheme.kt#L9). 
This Theme uses dependency injection to discover the set of
Behaviors that have been installed via Kodein Modules. It can filter that list down to those Behaviors associated
with it. This avoids hard dependencies on Views or Behaviors as a result.

DynamicThemes require explicit Behavior registration to work. The built-in Themes define a Module per Behavior to allow
arbitrary groupings within apps.

```kotlin
class MyApp(display: Display,
            manager: ThemeManager,
            theme  : DynamicTheme): Application {

    init {
        manager.selected = theme
        // use buttons
    }
}

fun main() {
    // DynamicThemes require a list of Behavior modules since the
    // Theme itself is essentially a Behavior filter.
    application(modules = listOf(NativeButtonBehavior)) {
        MyApp(instance(), instance(), instance())
    }
}
``` 
?> Include behavior modules at launch, so they can be used with a registered DynamicTheme.

This app no longer has extraneous dependencies on things like ProgressBar and its Behavior.