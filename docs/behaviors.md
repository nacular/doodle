# Delegating Rendering
----------------------

It is common to make a View's behavior and presentation configurable. In many cases this happens through properties like colors, fonts, etc.

```kotlin
val textField = TextField().apply {
    backgroundColor = Darkgray
    foregroundColor = White
    borderVisible   = false
}
```

### Sometimes a View requires more complex customization.

Take a TabbedPanel for example. The number of configurations is fairly open-ended; and the API would be needlessly complicated if it
tried to encompass everything.

This is where [`Behavior`s]() come in. Views can accept a Behavior and delegate things like rendering, hit-detection and anything else
to provide deep customization. TabbedPanel--along with Textfield and many other controls--actually does this.