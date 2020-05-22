# Creating Applications
-----------------------

All doodle code runs within an [`Application`](https://github.com/pusolito/doodle/blob/master/Browser/src/commonMain/kotlin/com/nectar/doodle/application/Application.kt#L6).
It is the entry-point for your business logic, and often the first class you write. Doodle will fully initialize and make your app ready
at constructor time. So there is no additional *run* or *start* method to implement. You can provide custom tear-down logic via the `shutdown`
method though.

```kotlin
class UsefulApp: Application {
    init {
        println("Hi!")
    }

    override fun shutdown() {}
}
```

## App Launch

Applications can either be launched as stand-alone, or within another app using an [`ApplicationViewFactory`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/ApplicationView.kt#L75).
Regardless of the launch mode, your app class does not change. In this way, apps have no knowledge of which mode they will be run in,
which improves separation of concerns and flexibility.

### Stand-alone Apps

Most apps will be full-screen, stand-alone experiences. The [`application`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/Application.kt#L94) function is used to launch stand-alone apps.
Closing the page cleans up stand-alone apps within it. Removing the element hosting an app or explicitly calling `shutdown`
has the same effect.

```kotlin
fun main() {
    // launch full-screen
    application {
        UsefulApp()
    }
}
```

### Nested Apps

Doodle apps can also be run within other apps. This is done by placing the nested app in a [**View**](views.md?id=creating-views)
that the host app can manage. The life-cycle of a nested app is slightly different from a stand-alone one.

```kotlin
InnerApp.kt

class InnerApp(display: Display): Application {
    init {
        // add stuff to display
    }
    
    // ...

    override fun shutdown() { /*...*/ }
}
```
```kotlin
OuterApp.kt

class OuterApp(display: Display, appView: ApplicationViewFactory): Application {
    init {
        display.children += appView {
            InnerApp(display = instance()) // inner app initialization 
        }
    }

    override fun shutdown() {}
}

fun main() {
    application(modules = setOf(appViewModule)) {
        OuterApp(display = instance(), appView = instance())
    }
}
```

Use an [`ApplicationViewFactory`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/ApplicationView.kt#L75)
to create nested apps. This class is available via the [`appViewModule`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/ApplicationView.kt#L82)
module.

?> Adding a nested app's View to the [**Display**](display.md?id=the-display-is-an-apps-root-container) triggers the app's initialization. Shutdown
the app by removing the host View from the Display.

## Dependencies

Doodle uses [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) when creating your app. The
lambda provided when launching an app is actually a [Kodein](https://github.com/Kodein-Framework/Kodein-DI) binding
context that lets you inject instances from doodle modules, or your own.