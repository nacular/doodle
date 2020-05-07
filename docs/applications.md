# Applications
--------------

All doodle code runs within an [`Application`](). It is the entry-point for your business logic, and often the first
class you write.  Doodle will fully initialize and make your app ready at constructor time. So there is no additional
*run* or *start* method to implement. You can provide custom tear-down logic via the `shutdown` method though.

```kotlin
class UsefulApp: Application {
    init {
        println("Hi!")
    }

    override fun shutdown() {}
}
```

## App Creation

Applications can either be launched as stand-alone, or within another app using an [`ApplicationView`](). Regardless of the launch
mode, your app class does not change.  In this way, apps are written without knowledge of which mode they will be run in.  This
separation of concerns provides flexibility.

### Stand-alone

Most apps will be full-screen, stand-alone experiences. The [`application`]() function is used to launch stand-alone apps.
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

### Nested

Doodle apps can also be run within other apps. This is done by placing the nested app in a [`View`]() that the host app can
manage.  The life-cycle of a nested app is slightly different from a stand-alone one.

?> [`ApplicationView`]() initializes its
nested app when added to the [`Display`](), and shuts the app down when removed.

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
            InnerApp(display = instance()) // inner app initialized 
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

Nested apps are created using [`ApplicationViewFactory`]().  This class is available via the [`appViewModule`]() module.

## Dependencies

Doodle uses [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) when creating your app. The
lambda provided when launching an app is actually a [Kodein](https://github.com/Kodein-Framework/Kodein-DI) binding
context that lets you pull instances from custom modules, or those built into doodle.