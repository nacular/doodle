# Common UI Components
----------------------

Doodle has several UI components in the Controls library. Here are a few of the common ones. Access these by adding a dependency to
the `Controls` library in your build file.

<!-- tabs:start -->

#### ** build.gradle.kts **

```kotlin
//...

dependencies {
     implementation ("io.nacular.doodle:controls:$doodleVersion")
}

//...
```

#### ** build.gradle **

```groovy
//...

dependencies {
     implementation "io.nacular.doodle:controls:$doodle_version"
}

//...
```
<!-- tabs:end -->

Most of these components rely entirely on their [`Behavior`](https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Behavior.kt#L7) for rendering. Moreover, they do not have
defaults for them to minimize bundle size. So you need to specify them explicitly or use a [**Theme**](themes.md) that provides
them for the controls you use. 
---

### [Label](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/text/Label.kt#L41)

Holds and displays static text with support for basic styling. You can construct it using the [`LabelFactory`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/text/Label.kt#L22).

```kotlin
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

ProgressBar is a specialization of [`ProgressIndicator`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ProgressIndicator.kt#L11), 
which should be used for more generalized progress display (i.e. circular)

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

ProgressIndicators can also take different shapes. Here's an example that uses [`CircularProgressBehavior`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/CircularProgressBehavior.kt#L23).

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.circularProgress"
}
```

This one draws a path using [`PathProgressBehavior`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/PathProgressBarBehavior.kt#L20).

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.pathProgress"
}
```

```kotlin
val blueColor = Color(0x5AE0FCu) opacity 0.85f

object: ProgressIndicator() {
    init {
        size     = Size(200, 100)
        progress = 0.25
        behavior = PathProgressIndicatorBehavior(
            pathMetrics,          // injected
            path                = path("M10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80")!!,
            foreground          = LinearGradientPaint(Black, blueColor, Origin, Point(width, 0.0)),
            foregroundThickness = 5.0,
            background          = Lightgray.paint,
            backgroundThickness = 5.0
        )
    }
}
```

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

?> Rendering requires a [`SpinnerBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/spinner/Spinner.kt#L31). [`BasicTheme`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/BasicTheme.kt#L210) provides one.

---
### [Dropdown](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/dropdown/Dropdown.kt#L42)

Dropdown is a list data structure similar to Spinner. It also lets you represent a list of choices where only one is visible (selected) at a time.
But unlike a Spinner, the choices are shown in a list when the control is activated. They work well when the list of options is relatively small.

Dropdown takes a [`ListModel`](https://github.com/nacular/doodle/blob/master/Controls/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ListModel.kt#L12) that works like an `Iterator`. This allows them to represent an open-ended list of items that do not need
to be loaded up front. 

```kotlin
val dropdown1 = Dropdown(1..9 step 2)
val dropdown2 = Dropdown(listOf("Left", "Center", "Right"))
```

```doodle
{
    "border": false,
    "height": "200px",
    "run"   : "DocApps.dropdown"
}
```

?> Rendering requires a [`DropdownBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/dropdown/Dropdown.kt#L22). [`BasicTheme`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/BasicTheme.kt#L559) provides one.

---
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

The `List` control is a visual analog to the list data structure. It is a **readonly**, ordered, generic collection of items with random
access to its members.

You need 2 things to create a List: a [`ListModel`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ListModel.kt#L10), 
and [`ItemVisualizer`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ItemVisualizer.kt#L41).

?> You also need to provide a Behavior or use a Theme with one since List delegates rendering.

The model represents the data within the List, and the visualizer provides a way to translate each item to a `View` that will be rendered within
the List.

```kotlin
val model = SimpleMutableListModel<Country>()

launch {
    listOf(
        "United Kingdom" to "images/197374.svg",
        "United States"  to "images/197484.svg",
        "France"         to "images/197560.svg",
        "Germany"        to "images/197571.svg",
        "Spain"          to "images/197593.svg",
        // ...
        ).
    sortedBy { it.first }.map { (name, path) ->
        imageLoader.load(path)?.let { image ->
            model.add(Country(name, image))
        }
    }
}

val list = DynamicList(
    model,
    selectionModel = MultiSelectionModel(),
    itemVisualizer = CountryVisualizer(textVisualizer)
).apply {
    cellAlignment = fill
}
```
This List displays a set of countries, with each having a name and flag image. A `DynamicList` is used here because
the underlying `model` changes as each country is added asynchronously when its image loads. 

Lists provide memory optimization by only rendering the contents within their viewport, recycling items to display
new rows. The default setting caches 10 extra items; but this can be changed with the `scrollCache` property when creating 
the List.

The following shows a [`DynamicList`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/list/DynamicList.kt#L16)
of countries (a custom data class). These Lists are useful when the underlying model can change after creation.
This demo loads images asynchronously and adds new countries to the model as they load. The demo also illustrates a
custom visualizer that represents each country as a name label and flag image.

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.list"
}
```

<div style="font-size:10px;text-align:right;color:gray">Icons made by <a style="color:gray" href="https://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a style="color:gray" href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>

?> [`DynamicList`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/list/DynamicList.kt#L16)
is readonly (though its models may change), while [`MutableList`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/list/MutableList.kt#L25) is read/write.

---
### [Tree](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/tree/Tree.kt#L69)

The Tree control is a visual analog to the tree data structure. It is a **readonly**, hierarchical, generic collection of items that are accessible
via a numeric path.

You need 2 things to create a Tree: a [`TreeModel`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/TreeModel.kt#L10),
 and [`ItemVisualizer`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ItemVisualizer.kt#L41).

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
    highlightingTextVisualizer, 
    MultiSelectionModel()
)
```

This creates a Tree from the nodes defined. This demo also places the Tree in a resizable ScrollPanel; but that code is excluded
for simplicity. Trees--like Lists--provide memory optimized rendering.

?> [`DynamicTree`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/tree/DynamicTree.kt#L10) 
is readonly (though its models may change), while [`MutableTree`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/tree/MutableTree.kt#L18) is read/write. 

```doodle
{
    "border": false,
    "height": "300px",
    "run"   : "DocApps.tree"
}
```

---
### [Table](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/table/Table.kt#L24)

Tables are very similar to `Lists` (**readonly** analog to the list data structure). They are like Lists that can display structured
data for each entry they hold.

Tables are strongly typed and homogeneous, like Lists. So each item is of some type `<T>`. The values of each column are therefore
derivable from each `<T>` in the table. The Table below contains a list of `Person` and has columns for the `name`, `age`, and gender
for each item. Columns can also produce arbitrary values, which is done to show the index of each item.

```kotlin
data class Person(val name: String, val age: Int, val isMale: Boolean)

fun male  (name: String, age: Int) = Person(name, age, isMale = true )
fun female(name: String, age: Int) = Person(name, age, isMale = false)

val textVisualizer  = HighlightingTextVisualizer(textMetrics)
val indexVisualizer = object: CellVisualizer<Unit> {
    override fun invoke(item: Unit, previous: View?, context: CellInfo<Unit>) =
        textVisualizer("${context.index + 1}", previous, context)
}

val data = listOf(female("Alice", 53), male("Bob", 35), male("Jack", 8), female("Jill", 5))
            
val table = Table(data, MultiSelectionModel()) {
    column(label("#"   ),             indexVisualizer         ) { minWidth =  50.0; width =  50.0; maxWidth = 150.0; cellAlignment = center }
    column(label("Name"), { name   }, textVisualizer          ) { minWidth = 100.0;                                                         }
    column(label("Age" ), { age    }, toString(textVisualizer)) { minWidth = 100.0; width = 100.0; maxWidth = 150.0                         }
    column(label("Male"), { isMale }, BooleanVisualizer()     ) { minWidth = 100.0; width = 100.0; maxWidth = 150.0; cellAlignment = center }
}
```

Each column's [`CellVisualizer`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/table/ColumnFactory.kt#L36) 
ultimately controls what is displayed in it. The visualizer is given the value of each element in that column to produce a View. So the
Name column gets a `String`, while the Male column gets a `Boolean`. The first column has values of type `Unit`, and uses the `RowNumberGenerator` to
display the index of each item.

?> Tables require a [`TableBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/table/TableBehavior.kt#L17) 
for rendering. `BasicTheme` provides one.

```doodle
{
    "border": false,
    "height": "400px",
    "run"   : "DocApps.table"
}
```

?> [`DynamicTable`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/table/DynamicTable.kt#L63) 
supports changes to its model, and [`MutableTable`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/table/MutableTable.kt#L27) allows editing. 

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

?> Requires a [`SplitPanelBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/theme/SplitPanelBehavior.kt#L16) 
for rendering. `BasicTheme` provides one.

---
### TabbedPanel

This control manages a generic list of items and displays them one at a time using an [`ItemVisualizer`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/ItemVisualizer.kt#L41). Each item is generally
tracked with a visual "tab" that allows selection of particular items in the list.

?> This control requires a [`TabbedPanelBehavior`](https://github.com/nacular/doodle/blob/master/Controls/src/commonMain/kotlin/io/nacular/doodle/controls/panels/TabbedPanel.kt#L19) 
for rendering. This demo uses the
[`basicTabbedPanelBehavior`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/BasicTheme.kt#L427) 
module which installs [`BasicTabbedPanelBehavior`](https://github.com/nacular/doodle/blob/master/Themes/src/commonMain/kotlin/io/nacular/doodle/theme/basic/tabbedpanel/BasicTabbedPanelBehavior.kt#L508)

```kotlin
val object1: View
val object2: View
val object3: View
val object4: View

// Each tab preview shows hardcoded names
val tabVisualizer = object: ItemVisualizer<View, Any> {
    private val textVisualizer = TextVisualizer(textMetrics)
    private val mapping = mapOf(
            object1 to "Circle",
            object2 to "Second Tab",
            object3 to "Cool Photo",
            object4 to "Tab 4"
    )

    override fun invoke(item: View, previous: View?, context: Any) =
        textVisualizer(mapping[item] ?: "Unknown")
}

// Each object is displayed within a ScrollPanel
val panel = TabbedPanel(
    ScrollPanelVisualizer(), 
    tabVisualizer, 
    object1, 
    object2, 
    object3, 
    object4
).apply {
    size = Size(500, 300)

    Resizer(this).apply { movable = false }
}
```

```doodle
{
    "border": false,
    "height": "400px",
    "run"   : "DocApps.tabbedPanel"
}
```
