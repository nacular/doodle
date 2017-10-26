
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.impl.DisplayImpl
import com.nectar.doodle.deviceinput.MouseInputManager
import com.nectar.doodle.dom.HtmlFactoryImpl
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.blue
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.SolidBrush
import com.nectar.doodle.drawing.defaultCanvasFactory
import com.nectar.doodle.drawing.impl.RealGraphicsDevice
import com.nectar.doodle.drawing.impl.RealGraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.RenderManagerImpl
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.scheduler.impl.SchedulerImpl
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.impl.MouseInputServiceImpl
import com.nectar.doodle.system.impl.MouseInputServiceStrategyWebkit
import com.nectar.doodle.ui.UIManager
import com.nectar.doodle.units.seconds
import org.w3c.dom.css.get
import kotlin.browser.document
import kotlin.math.min
import kotlin.properties.Delegates

open abstract class Shape<out T: com.nectar.doodle.geometry.Shape>(color: Color = green): Gizmo() {
    var color by Delegates.observable(color) { _,_,_ ->
        rerender()
    }

    abstract val shape: T

    override fun handleMouseEvent(event: MouseEvent) {
        when (event.type) {
            Enter, Exit -> color = color.inverted
            else        -> {}
        }

        println(event)
    }

    override fun handleMouseMotionEvent(event: MouseEvent) {
        println(event)
    }
}

class Box(color: Color = green): Shape<Rectangle>(color) {
    override val shape get() = Rectangle(size = Size(width, height))

    override fun render(canvas: Canvas) {
        canvas.rect(shape, SolidBrush(color))
    }
}

class Circle(color: Color = blue): Shape<Circle>(color) {
    override val shape get() = Circle(position + Point(width/2, height/2), min(width/2, height/2))

    override fun contains(point: Point) = shape.contains(point)

    override fun render(canvas: Canvas) {
        canvas.circle(shape.at(Point(width/2, height/2)), SolidBrush(color))
    }
}

class Ellipse(color: Color = red): Shape<Ellipse>(color) {
    override val shape get() = Ellipse(Point(width/2, height/2), width/2, height/2)

    override fun render(canvas: Canvas) {
        canvas.ellipse(shape, SolidBrush(color))
    }
}

fun main(args: Array<String>) {
    document.styleSheets[0].asDynamic().addRule("body *", "position:absolute")
    document.styleSheets[0].asDynamic().addRule("b",      "overflow:hidden"  )
    document.styleSheets[0].asDynamic().addRule("pre",    "overflow:hidden"  )

    document.styleSheets[0].asDynamic().addRule("pre",   "margin:0")
    document.styleSheets[0].asDynamic().addRule("svg",   "display:inline-block;width:100%;height:100%")
    document.styleSheets[0].asDynamic().addRule("svg *", "position:absolute")

    val htmlFactory = HtmlFactoryImpl()

    val display   = DisplayImpl(htmlFactory, document.body!!)
    val scheduler = SchedulerImpl()

//    display.fill(SolidBrush(Color.red))

    MouseInputManager(display, MouseInputServiceImpl(MouseInputServiceStrategyWebkit(htmlFactory)))

    RenderManagerImpl(
            display,
            DummyUIManager,
            scheduler,
            RealGraphicsDevice(RealGraphicsSurfaceFactory(htmlFactory, ::defaultCanvasFactory)))

    val box     = Box    ()
    val circle  = Circle ()
    val ellipse = Ellipse()

    box.bounds     = Rectangle(100.0, 100.0, 100.0, 200.0)
    circle.bounds  = box.bounds
    ellipse.bounds = box.bounds

    display.children.addAll(arrayOf(box, ellipse, circle))

//    display.children.add(box   )
//    display.children.add(circle)

//    scheduler.after(3.seconds) {
//        box.color = blue
//    }

    scheduler.after(3.seconds) {
        box.size *= 2
    }
}

object DummyUIManager: UIManager {
    override val installedTheme get() = TODO("not implemented")
    override val availableThemes get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun installUI(gizmo: Gizmo, aResponse: UIManager.UIResponse) = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    override fun revalidateUI(gizmo: Gizmo) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun installTheme(theme: UIManager.ThemeInfo) = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    override fun <T> getDefaultValue(aName: String) = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}