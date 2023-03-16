package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.invoke
import io.nacular.doodle.animation.transition.easeOutBack
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.spinner.MutableIntSpinnerModel
import io.nacular.doodle.controls.spinner.MutableSpinner
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.spinner.SpinnerModel
import io.nacular.doodle.controls.spinner.spinnerEditor
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.simpleButtonRenderer
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.center
import io.nacular.doodle.core.container
import io.nacular.doodle.core.height
import io.nacular.doodle.core.then
import io.nacular.doodle.datatransport.Files
import io.nacular.doodle.datatransport.dragdrop.DropEvent
import io.nacular.doodle.datatransport.dragdrop.DropReceiver
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.AffineTransform2D
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Darkgray
import io.nacular.doodle.drawing.Color.Companion.Gray
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.computeAngle
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.event.PointerListener.Companion.pressed
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.times
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.height
import io.nacular.doodle.image.width
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.ListLayout
import io.nacular.doodle.layout.WidthSource.Parent
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor.Companion.Text
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.range.BasicCircularSliderBehavior
import io.nacular.doodle.theme.basic.spinner.SpinnerTextEditOperation
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.Resizer
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.ToStringIntEncoder
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.atan2
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.normalize
import io.nacular.measured.units.times
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.properties.Delegates.observable
import kotlin.random.Random
import kotlin.reflect.KMutableProperty0
import io.nacular.doodle.datatransport.Image as ImageType

/**
 * Panel used to display editable properties of an image.
 */
private class PropertyPanel(private val focusManager: FocusManager): Container() {
    private val spinnerHeight = 24.0

    /**
     * Simple View with a [MutableSpinner] and [Label] that displays a numeric property.
     */
    private inner class Property(
        private val property  : KMutableProperty0<Double>,
        private val updateWhen: PropertyObservers<Any, Any>,
                    suffix    : String = ""
    ): View() {
        private fun <T, M: SpinnerModel<T>> spinnerVisualizer(suffix: String = "") = itemVisualizer { item: Int, previous: View?, context: Spinner<T, M> ->
            when (previous) {
                is Label -> previous.also { it.text = "$item$suffix" }
                else     -> Label("$item$suffix").apply {
                    font            = previous?.font
                    cursor          = Text
                    foregroundColor = previous?.foregroundColor
                    backgroundColor = previous?.backgroundColor ?: Transparent

                    (context as? MutableSpinner<*,*>)?.let { spinner ->
                        pointerFilter += pressed { event ->
                            spinner.startEditing()
                            event.consume()
                        }
                    }
                }
            }
        }

        private val spinner = MutableSpinner(
                MutableIntSpinnerModel(Int.MIN_VALUE..Int.MAX_VALUE, property.get().toInt()),
                spinnerVisualizer(suffix)
        ).apply {
            // Make the spinner editable
            editor = spinnerEditor { spinner, value, current ->
                object: SpinnerTextEditOperation<Int>(focusManager, ToStringIntEncoder, spinner, value, current) {
                    init {
                        textField.selectionBackgroundColor = Darkgray
                    }
                }
            }

            changed += {
                it.value.onSuccess {
                    if (property.get().toInt() != it) {
                        property.set(it.toDouble())
                    }
                }
            }
        }

        private val callBack: PropertyObserver<Any, Any> = { _,_,_ -> spinner.set(property.get().toInt()) }

        init {
            updateWhen += callBack

            children += listOf(spinner, Label(property.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }))

            // Label is centered below spinner, which is stretched to fit its parent's width
            layout = constrain(children[0], children[1]) { spinner, label ->
                spinner.top    eq 0
                spinner.left   eq 0
                spinner.right  eq parent.right
                spinner.height eq spinnerHeight
                label.top      eq spinner.bottom + 2
                label.centerX  eq parent.centerX
                label.height   eq label.height.readOnly
            }
        }

        override fun removedFromDisplay() {
            updateWhen -= callBack
        }
    }

    /**
     * Simple container created from a [Label] and two property views.
     */
    private fun propertyGroup(name: String, property1: View, property2: View) = container {
        this += listOf(Label(name).apply { fitText = setOf(Width) }, property1, property2)

        val spacing = 10.0

        layout = constrain(children[0], property1, property2) { label, first, second ->
            label.top     eq second.top
            label.left    eq spacing
            label.height  eq spinnerHeight
            first.centerY eq parent.centerY.writable
            first.right   eq second.left - spacing
            first.width   eq second.width
            first.height  eq 50
            second.height eq 50
            second.top    eq first.top.readOnly
            second.right  eq parent.right - spacing
            second.width  eq (parent.width - spacing * 3) / 3

            parent.height.writable eq max(first.bottom, second.bottom) + spacing
        }
    }

    // Used to cache listeners, so they can be cleaned up when photo changes
    private val photoBoundsChanged   : PropertyObservers<View, Rectangle>       = SetPool()
    private val photoTransformChanged: PropertyObservers<View, AffineTransform> = SetPool()

    var photo: View? by io.nacular.doodle.utils.observable(null) {old, new ->
        old?.let { photo ->
            (photoBoundsChanged    as SetPool).forEach { photo.boundsChanged    -= it }
            (photoTransformChanged as SetPool).forEach { photo.transformChanged -= it }
        }

        children.clear()

        val computeAngle: (View) -> Measure<Angle> = { (it.transform as? AffineTransform2D)?.computeAngle() ?: (0 * degrees) }

        new?.also { photo ->
            val photoAngle = object: Any() {
                var angle by observable(computeAngle(photo) `in` degrees) { _,_,new ->
                    if ((new * degrees).normalize() != computeAngle(photo).normalize()) {
                        photo.parent?.let {
                            val photoCenter = Point(photo.width/2, photo.height/2)
                            photo.position  = it.toLocal(photoCenter, photo) - photoCenter
                        }
                        photo.transform = Identity.rotate(around = photo.center, new * degrees)
                    }
                }
            }

            val rotationSlider = CircularSlider(0.0 .. 359.0).apply {
                size     = Size(50)
                value    = photoAngle.angle
                behavior = BasicCircularSliderBehavior(thickness = 18.0)
                changed += { _,_,new -> photoAngle.angle = new }
            }

            photo.transformChanged += { _,_,_ ->
                photoAngle.angle     = computeAngle(photo) `in` degrees
                rotationSlider.value = photoAngle.angle
            }

            children += propertyGroup("Size",     Property(photo::width, photoBoundsChanged), Property(photo::height,     photoBoundsChanged                 ))
            children += propertyGroup("Position", Property(photo::x,     photoBoundsChanged), Property(photo::y,          photoBoundsChanged                 ))
            children += propertyGroup("Rotation", rotationSlider,                             Property(photoAngle::angle, photoTransformChanged, suffix = "°"))

            (photoBoundsChanged    as SetPool).forEach { photo.boundsChanged    += it }
            (photoTransformChanged as SetPool).forEach { photo.transformChanged += it }
        }
    }

    init {
        insets = Insets(top = 48.0, left = 20.0, right = 20.0)
        layout = ListLayout(widthSource = Parent).then { height = (children.lastOrNull()?.bounds?.bottom ?: 280.0) + insets.left + 8 }
        width  = 300.0 + insets.left
        height = 280.0 + insets.left + 8
    }

    override fun render(canvas: Canvas) {
        canvas.outerShadow(blurRadius = 8.0, color = Black opacity 0.3f) {
            canvas.rect(bounds.atOrigin.inset(insets.left), radius = 10.0, color = White)
        }
    }
}

/**
 * Simple View that displays an image and ensures its aspect ratio.
 */
private class FixedAspectPhoto(private var image: Image): View() {
    private val aspectRation = image.width / image.height

    init {
        size = image.size
        boundsChanged += { _,old,_ ->
            when {
                old.width  != width  -> size = Size(width, width / aspectRation  )
                old.height != height -> size = Size(height * aspectRation, height)
            }
        }
    }

    override fun render(canvas: Canvas) {
        canvas.image(image, destination = bounds.atOrigin)
    }
}

/**
 * Simple app for displaying images.
 */
class PhotosApp(display     : Display,
                focusManager: FocusManager,
                themeManager: ThemeManager,
                theme       : DynamicTheme,
                animate     : Animator,
    private val images      : ImageLoader): Application {

    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        themeManager.selected = theme

        val buttonInset  = 20.0
        var panelVisible = false

        // Contains controls to show/update photo properties
        val propertyPanel = PropertyPanel(focusManager).apply { opacity = 0f }

        // Helper for constraining property panel layout
        val panelConstraints: ConstraintDslContext.(Bounds) -> Unit = {
            when {
                panelVisible -> it.bottom eq parent.bottom - buttonInset / 2
                else         -> it.top    eq parent.bottom - buttonInset * 2
            }

            it.height  eq it.height.readOnly
            it.centerX eq parent.centerX
        }

        // Used to show/hide panel
        val panelToggle = PushButton().apply {
            y        = display.height - buttonInset / 2
            height   = 50.0
            behavior = simpleButtonRenderer { _, canvas ->
                val color = when {
                    model.pointerOver -> Gray
                    model.pressed     -> Darkgray
                    else              -> Lightgray
                }

                canvas.rect(Rectangle(width / 2 - 25, height - 35.0, 50.0, 10.0), radius = 5.0, color = color)
            }

            fired += {
                val start = if (panelVisible) 1f else 0f
                val end   = if (panelVisible) 0f else 1f

                // removing layout constraints from panel when it is animating
                display.layout = (display.layout as io.nacular.doodle.layout.constraints.ConstraintLayout).unconstrain(propertyPanel, panelConstraints)

                // Animate property panel show/hide
                animate(start to end, tweenFloat(easeOutBack, 250 * milliseconds)) {
                    propertyPanel.y       = display.height - buttonInset * 2 - ((propertyPanel.height - 30) * it.toDouble())
                    propertyPanel.opacity = it
                }.completed += {
                    panelVisible = !panelVisible
                    (display.layout as io.nacular.doodle.layout.constraints.ConstraintLayout).constrain(propertyPanel, panelConstraints)
                }
            }
        }

        // Holds images and serves as drop-target
        val mainContainer = container {
            val import = { photo: View, location: Point ->
                photo.width     = 400.0
                photo.position  = location - Point(photo.width / 2, photo.height / 2)
                photo.transform = Identity.rotate(location, (Random.nextFloat() * 30 - 15) * degrees)

                // Bring photo to foreground and update panel on pointer-down
                photo.pointerChanged += pressed {
                    children.move(photo, to = children.size - 1)
                    propertyPanel.photo = photo
                }

                // Register gesture recognizer to track multi-pointer interactions
                GestureRecognizer(photo).changed += object: GestureListener<GestureEvent> {
                    lateinit var originalSize    : Size
                    lateinit var originalCenter  : Point
                    lateinit var originalVector  : Point
                    lateinit var originalPosition: Point
                    lateinit var initialTransform: AffineTransform

                    override fun started(event: GestureEvent) {
                        // Capture initial state to apply deltas with in `changed`
                        originalSize     = photo.size
                        originalCenter   = this@container.toLocal(event.center, photo)
                        originalVector   = event.initial[1].inParent(photo) - event.initial[0].inParent(photo)
                        originalPosition = photo.position
                        initialTransform = photo.transform

                        event.consume() // ensure event is consumed from Resizer
                    }

                    override fun changed(event: GestureEvent) {
                        val currentVector = event.current[1].inParent(photo) - event.current[0].inParent(photo)

                        // Angle between initial set of points and their current locations
                        val transformAngle = atan2(
                            originalVector.x * currentVector.y - originalVector.y * currentVector.x,
                            originalVector.x * currentVector.x + originalVector.y * currentVector.y
                        )

                        // Use transform for rotation
                        photo.transform = initialTransform.rotate(around = originalCenter, by = transformAngle)

                        // Use bounds to keep panel updated
                        photo.bounds = Rectangle(
                                originalPosition - ((originalPosition - originalCenter) * (1 - event.scale)),
                                originalSize * event.scale)

                        event.consume() // ensure event is consumed from Resizer
                    }

                    override fun ended(event: GestureEvent) {
                        event.consume() // ensure event is consumed from Resizer
                    }
                }

                Resizer(photo) // Use to handle edge resizing and movement with single pointer

                children += photo
            }

            dropReceiver = object: DropReceiver {
                private  val allowedFileTypes                    = Files(ImageType("jpg"), ImageType("jpeg"), ImageType("png"))
                override val active                              = true
                private  fun allowed          (event: DropEvent) = allowedFileTypes in event.bundle
                override fun dropEnter        (event: DropEvent) = allowed(event)
                override fun dropOver         (event: DropEvent) = allowed(event)
                override fun dropActionChanged(event: DropEvent) = allowed(event)
                override fun drop             (event: DropEvent) = event.bundle[allowedFileTypes]?.let { files ->
                    // Load images as FixedAspectPhotos
                    val photos = files.map { appScope.async { images.load(it)?.let { FixedAspectPhoto(it) } } }

                    // Import images
                    appScope.launch {
                        photos.mapNotNull { it.await() }.forEach { import(it, event.location) }
                    }
                    true
                } ?: false
            }

            // Load default images
            appScope.launch {
                listOf("tetons.jpg", "earth.jpg").forEachIndexed { index, file ->
                    images.load(file)?.let { FixedAspectPhoto(it) }?.let {
                        import(it, display.center + Point(y = index * 50.0))
                    }
                }
            }
        }

        display += listOf(mainContainer, propertyPanel, panelToggle)

        display.layout = constrain(mainContainer, panelToggle, propertyPanel) { main, toggle, panel ->
            main.edges eq parent.edges

            toggle.width   eq panel.width - 24
            toggle.height  eq 50
            toggle.centerX eq panel.centerX
            toggle.bottom  eq panel.top + 22 + 40
        }.constrain(propertyPanel, panelConstraints)

        display.relayout()
    }

    override fun shutdown() {}
}