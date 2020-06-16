# Creating Applications
-----------------------

All Doodle apps run within an [`Application`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/application/Application.kt#L6).
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

You can either launch an app **top-level**, or **nested** within another app. The Application class does not change regardless of the
launch mode. That is because apps have no knowledge of the mode they will run in, making them independent of browser concepts by default.

?> Doodle does not expose any browser concepts to apps. The launch step is the only place where HTML elements are directly accepted, 
and this is only to support running Doodle apps within a non-Doodle page.

### Top-level Apps

Most apps will run independent of others and exist purely within the context of a page (or element within it). Use the [`application`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/application/Application.kt#L65)
function to launch apps this way. The result is a full-screen experience by default, with the app taking up the entire page and control all
aspects of it. You can also provide an HTML element when launching a top-level app. This allows you to host Doodle apps in non-Doodle contexts. 
The apps in this documentation are top-level within specific elements.

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
that the host app manages. An app launched this way has the same functionality as a top-level one. Its lifecycle however, is tied to
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

Use an [`ApplicationViewFactory`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/application/ApplicationView.kt#L20)
to create nested apps. This class is available via the [`AppViewModule`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/application/ApplicationView.kt#L27)
module.

?> Adding a nested app's View to the [**Display**](display.md?id=the-display-is-an-apps-root-container) triggers the app's initialization. Shutdown
the app by removing the host View from the Display.

---
## Dependencies

Doodle uses [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) when creating apps. The
lambda provided when launching an app is actually a [Kodein](https://github.com/Kodein-Framework/Kodein-DI) binding
context that lets you inject instances from Doodle modules, or your own.