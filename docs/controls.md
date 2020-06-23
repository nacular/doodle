# Common UI Components
----------------------

Doodle has several UI components in the Controls library. Here are a few of the common ones. Access these by adding a dependency to
the Controls library in your build file.

### build.gradle

```groovy
//...

dependencies {
     implementation "io.nacular.doodle:controls:$doodle_version"
}

//...
```

Most of these components rely entirely on their [`Behavior`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Behavior.kt#L7) for rendering. Moreover, they do not have
defaults for them to minimize bundle size. So you need to specify them explicitly or use a [**Theme**](themes.md) that provides
them for the controls you use. 
---

### [Label](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/text/Label.kt#L41)

Holds and displays static text with support for basic styling. You can construct it using the [`LableFactory`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/text/Label.kt#L22).

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

?> Requires a `Behavior<Label>` like [`LabelBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/theme/LabelBehavior.kt#L20) 

---
### [TextField](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/text/TextField.kt#L15)

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

?> Requires a [`TextFieldBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/text/TextField.kt#L11). The module NativeTextFieldBehavior provides one. 

---
### [PushButton](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/buttons/PushButton.kt#L8)

A component that triggers an action when pressed; usually with the pointer or keyboard.

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

?> There are several types of buttons available, including ToggleButton, CheckBox, and RadioButton. Rendering requires
a `Behavior<Buton>`. [`NativeTheme`](https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/theme/native/NativeTheme.kt#L47) 
and [`BasicTheme`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/BasicTheme.kt#L64) 
provide versions.

---
### [Photo](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/Photo.kt#L8)

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
### [ProgressBar](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ProgressBar.kt#L7)

Represents a value within a specified range that usually indicates progress toward some goal. It provides notifications when
its value or range changes. Specify a range by passing a `ClosedRange` or [`ConfinedValueModel`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ConfinedRangeModel.kt#L22) 
in the constructor.

ProgressBar is a specialization of ProgressIndicator, which should be used for more generalized progress display (i.e. circular)

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

?> Rendering requires a `Behavior<ProgressBar>`. [`BasicTheme`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/BasicTheme.kt#L260) provides one.

---
### [Slider](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/range/Slider.kt#L16)

Sliders hold a value within a specified range and allow the user to change the value. It provides notifications when
its value or range changes. Specify a range by passing a `ClosedRange` or [`ConfinedValueModel`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ConfinedRangeModel.kt#L22) 
in the constructor.

You can also confine the values to a predefined set within the range by specifying the `ticks` count and setting
`snapToTicks` to `true`. This will pin the slider values to an evenly spaced set of points along its range.

```kotlin
val slider = Slider().apply {
    size = Size(200, 15)

//  ticks       = 10
//  snapToTicks = true
}
```

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.slider"
}
```

?> Rendering requires a `Behavior<Slider>`. [`BasicTheme`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/BasicTheme.kt#L197) provides one.

---
### [Spinner](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/spinner/Spinner.kt#L42)

Spinner is a list data structure analog that lets you represent a list of items where only one is visible (selected) at a time. 
They work well when the list of options is relatively small, or the input is an incremental value: like the number of items to purchase.

Spinner takes a [`Model`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/spinner/Spinner.kt#L11) that works like an `Iterator`. This allows them to represent an open-ended list of items that do not need
to be loaded up front. 

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

?> Rendering requires a `SpinnerBehavior`. [`BasicTheme`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/BasicTheme.kt#L210) provides one.

### [StarRater](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/StarRater.kt#L30)

A highly customizable control that displays a rating between [0, n] using stars. It also lets the user change the underlying value.

```kotlin
val stars = StarRater(displayRounded = 0f, max = 5).apply {
    bounds           = Rectangle(200, 50)
    innerRadiusRatio = 0.6f
    minSpacing       = 15.0
}
``` 

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.starRater"
}
```

---
### [List](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/list/List.kt#L51)

The `List` control is a visual analog to the list data structure. It is a **readonly**, ordered collection of items with random
access to its members.

You need 2 things to create a List: a [`ListModel`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ListModel.kt#L10), 
and [`IndexedItemVisualizer`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ItemVisualizer.kt#L41).

?> You also need to provide a Behavior or use a Theme with one since List delegates rendering.

```kotlin
val textVisualizer: TextItemVisualizer

val list = List(listOf(
    "Alabama",
    "Alaska",
    "Arizona",
    "Arkansas",
    "California"
    // ...
    ),
    selectionModel = MultiSelectionModel(),
    itemVisualizer = ignoreIndex(HighlightingTextVisualizer(textMetrics)))
```

This creates a List using a factory that takes a list collection and creates a ListModel from it. The demo also places
the List in a resizable ScrollPanel; but that code is excluded for simplicity.

Lists provide memory optimization by only rendering the contents within their viewport. It then recycles the items to display
new rows. The default setting caches 10 extra items; but this can be changed with the `scrollCache` property when creating 
the List.

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.list"
}
```

?> [`DynamicList`]() supports changes to its model, and [`MutableList`]() allows editing. 

---
### Tree

The Tree control is a visual analog to the tree data structure. It is a **readonly**, hierarchical collection of items that are accessible
via a numeric path. It is also readonly like the data structure.

You need 2 things to create a Tree: a [`TreeModel`](), and [`IndexedItemVisualizer`]().

?> You also need to provide a Behavior or use a Theme with one since Tree delegates rendering.

```kotlin
val root = rootNode("") {
    node("Applications")
    node("Desktop"     )
    node("Documents"   ) {
        node("Image.jpg")
        node("Todos.txt")
    }
    node("Downloads"    )
    node("Movies"       )
    node("Music"        ) {
        node("Track1.mp3")
        node("Track2.mp3")
        node("Track3.mp3")
        node("Track4.mp3")
    }
    node("Photos"        ) {
        node("Capture1.jpg")
        node("Capture2.jpg")
        node("Capture3.jpg")
        node("Capture4.jpg")
    }
}

val tree = Tree(
    SimpleTreeModel(root), 
    ignoreIndex(HighlightingTextVisualizer(textMetrics)), 
    MultiSelectionModel()
)
```

This creates a Tree from the nodes defined. This demo also places the Tree in a resizable ScrollPanel; but that code is excluded
for simplicity. Trees--like Lists--provide memory optimized rendering.

?> [`DynamicTree`]() supports changes to its model, and [`MutableTree`]() allows editing. 

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.tree"
}
```

---
### Table

Tables are very similar to Lists (**readonly** analog to the list data structure). They are like Lists that can display structured
data for each entry they hold.

Tables are strongly typed and homogeneous, like Lists. So each item is of some type `<T>`. The values of each column are therefore
derivable from each `<T>` in the table. This Table extracts a `name`, `age`, and gender for each item. Columns can also produce
arbitrary values. The first column here does that by rendering an index for each item.

Each column's [`CellVisualizer`]() ultimately controls what is displayed in it. The visualizer is given the value of each element in
that column to produce a View. So the Name column gets a `String`, while the Male colum gets a `Boolean`. The first column has values 
of type `Unit`. The RowNumberGenerator just renders the index of each one.

?> Tables require a [`TableBehavior`] for rendering. `BasicTheme` provides one.

```kotlin
val textVisualizer = HighlightingTextVisualizer(textMetrics)
val itemGenerator  = toString<Any>(textVisualizer)

val table = Table(listOf(female("Alice", 53), male("Bob", 35), male("Jack", 8), female("Jill", 5)), MultiSelectionModel()) {
    //     header          value      visualizer                                      config
    column(label("#"   ),             RowNumberGenerator(toString(textVisualizer))) { minWidth =  50.0; width =  50.0; maxWidth = 150.0; cellAlignment = center }
    column(label("Name"), { name   }, itemGenerator                               ) { minWidth = 100.0;                                                         }
    column(label("Age" ), { age    }, itemGenerator                               ) { minWidth = 100.0; width = 100.0; maxWidth = 150.0                         }
    column(label("Male"), { isMale }, ignoreSelection(BooleanItemVisualizer())    ) { minWidth = 100.0; width = 100.0; maxWidth = 150.0; cellAlignment = center }
}
```

```doodle
{
    "border": false,
    "height": "400px",
    "run"   : "DocApps.table"
}
```

?> [`DynamicTable`]() supports changes to its model, and [`MutableTable`]() allows editing. 

---
### SplitPanel

This control divides a region into two areas, each occupied by a View. It also allows the user to change the portion of its viewport
dedicated to either view.

```doodle
{
    "border": false,
    "height": "400px",
    "run"   : "DocApps.splitPanel"
}
```

?> Requires a [`SplitPanelBehavior`] for rendering. `BasicTheme` provides one.

---
### TabbedPanel

This control manages a generic list of items and displays them one at a time using an [`ItemVisualizer`](). Each item is generally
tracked with a visual "tab" that allows selection of particular items in the list.

?> This control requires a [`TabbedPanelBehavior`]() for rendering.

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