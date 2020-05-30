# Creating Applications
-----------------------

All Doodle code runs within an [`Application`](https://github.com/pusolito/doodle/blob/master/Browser/src/commonMain/kotlin/com/nectar/doodle/application/Application.kt#L6).
It is the entry-point for your business logic, and often the first class you write. Doodle fully initializes your app at constructor time,
so there is no additional *run* or *start* method to implement. You can provide custom tear-down logic via the `shutdown` method though.

```kotlin
class UsefulApp: Application {
    init {
        println("Hi!")
    }

    override fun shutdown() {}
}
```
---
## App Launch

Applications are launched stand-alone, or within another app using [`ApplicationViewFactory()`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/ApplicationView.kt#L21).
Your app class does not change regardless of the launch mode. That is because apps have no knowledge of which mode they will run in,
further improving separation of concerns and flexibility.

### Top-level Apps

Most apps will run independent of other apps and exist purely within the context of a page (or element within it). Use the [`application`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/Application.kt#L94)
function to launch apps this way. This launches apps in full-screen by default, but you can override this by passing an HTML element
as the root. This is useful when hosting Doodle apps in non-Doodle contexts.

Closing the page cleans up any apps within it. Removing the element hosting an app or explicitly calling `shutdown` has the same effect.

```kotlin
fun main() {
    // launch full-screen
    application {
        UsefulApp()
    }
}
```

### Nested Apps

Doodle apps can also be run within other Doodle apps. This is done by placing the nested app in a [**View**](views.md?id=creating-views)
that the host app can manage. An app launched this way have the same functionality as a top-level one. Its lifecycle however, is tied to
the host View.

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
    application(modules = listOf(AppViewModule)) {
        OuterApp(display = instance(), appView = instance())
    }
}
```

Use an [`ApplicationViewFactory`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/ApplicationView.kt#L75)
to create nested apps. This class is available via the [`AppViewModule`](https://github.com/pusolito/doodle/blob/master/Browser/src/jsMain/kotlin/com/nectar/doodle/application/ApplicationView.kt#L82)
module.

?> Adding a nested app's View to the [**Display**](display.md?id=the-display-is-an-apps-root-container) triggers the app's initialization. Shutdown
the app by removing the host View from the Display.

---
## Dependencies

Doodle uses [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) when creating apps. The
lambda provided when launching an app is actually a [Kodein](https://github.com/Kodein-Framework/Kodein-DI) binding
context that lets you inject instances from Doodle modules, or your own.