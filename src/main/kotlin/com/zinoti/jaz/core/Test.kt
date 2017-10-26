
import com.zinoti.jaz.core.Gizmo
import com.zinoti.jaz.core.impl.DisplayImpl
import com.zinoti.jaz.dom.HtmlFactoryImpl
import com.zinoti.jaz.drawing.Canvas
import com.zinoti.jaz.drawing.Color
import com.zinoti.jaz.drawing.Color.Companion.blue
import com.zinoti.jaz.drawing.Color.Companion.green
import com.zinoti.jaz.drawing.Pen
import com.zinoti.jaz.drawing.SolidBrush
import com.zinoti.jaz.drawing.defaultCanvasFactory
import com.zinoti.jaz.drawing.impl.RealGraphicsDevice
import com.zinoti.jaz.drawing.impl.RealGraphicsSurfaceFactory
import com.zinoti.jaz.drawing.impl.RenderManagerImpl
import com.zinoti.jaz.geometry.Circle
import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.geometry.Size
import com.zinoti.jaz.scheduler.impl.SchedulerImpl
import com.zinoti.jaz.ui.UIManager
import com.zinoti.jaz.units.seconds
import org.w3c.dom.css.get
import kotlin.browser.document
import kotlin.math.min
import kotlin.properties.Delegates


class Box(color: Color = green): Gizmo() {
    var color by Delegates.observable(color) { _,_,_ ->
        rerender()
    }

    override fun render(canvas: Canvas) {
        canvas.rect(Rectangle(size = Size(width, height)), Pen(color.inverted, 2.0), SolidBrush(color))
    }
}

class Circle(color: Color = blue): Gizmo() {
    var color by Delegates.observable(color) { _,_,_ ->
        rerender()
    }

    override fun render(canvas: Canvas) {
        canvas.circle(Circle(Point(width/2, height/2), min(width/2, height/2)), Pen(color.inverted, 2.0), SolidBrush(color))
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

    RenderManagerImpl(
            display,
            DummyUIManager,
            scheduler,
            RealGraphicsDevice(RealGraphicsSurfaceFactory(htmlFactory, ::defaultCanvasFactory)))

    val box    = Box()
    val circle = Circle()

    box.bounds    = Rectangle(100.0, 100.0, 100.0, 100.0)
    circle.bounds = Rectangle(100.0, 100.0, 100.0, 100.0)

    display.children.addAll(arrayOf(box, circle))

//    display.children.add(box   )
//    display.children.add(circle)

    scheduler.after(3.seconds) {
        box.color = blue
    }

    scheduler.after(5.seconds) {
        box.size *= 5
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