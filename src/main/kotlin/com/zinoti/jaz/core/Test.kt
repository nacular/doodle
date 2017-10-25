import com.zinoti.jaz.core.Gizmo
import com.zinoti.jaz.core.impl.DisplayImpl
import com.zinoti.jaz.dom.HtmlFactoryImpl
import com.zinoti.jaz.drawing.Canvas
import com.zinoti.jaz.drawing.Color
import com.zinoti.jaz.drawing.SolidBrush
import com.zinoti.jaz.drawing.defaultCanvasFactory
import com.zinoti.jaz.drawing.impl.RealGraphicsDevice
import com.zinoti.jaz.drawing.impl.RealGraphicsSurfaceFactory
import com.zinoti.jaz.drawing.impl.RenderManagerImpl
import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.geometry.Size
import com.zinoti.jaz.scheduler.impl.SchedulerImpl
import com.zinoti.jaz.ui.UIManager
import org.w3c.dom.css.get
import kotlin.browser.document


class Box: Gizmo() {
    override fun render(canvas: Canvas) {
        canvas.rect(Rectangle(size = Size(width, height)), SolidBrush(Color.Green))
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

    val display = DisplayImpl(htmlFactory, document.body!!)

    display.fill(SolidBrush(Color.Red))

    val renderManager = RenderManagerImpl(display,
            DummyUIManager,
            SchedulerImpl(),
            RealGraphicsDevice(RealGraphicsSurfaceFactory(htmlFactory, ::defaultCanvasFactory)))

    val box = Box()

    box.bounds = Rectangle(100.0, 100.0, 100.0, 100.0)

    display.children.add(box)
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