import com.zinoti.jaz.core.Gizmo
import com.zinoti.jaz.core.impl.DisplayImpl
import com.zinoti.jaz.drawing.Color
import com.zinoti.jaz.drawing.SolidBrush
import com.zinoti.jaz.drawing.impl.RealGraphicsDevice
import com.zinoti.jaz.drawing.impl.RealGraphicsSurfaceFactory
import com.zinoti.jaz.drawing.impl.RenderManagerImpl
import com.zinoti.jaz.scheduler.impl.SchedulerImpl
import com.zinoti.jaz.ui.UIManager
import org.w3c.dom.css.get
import kotlin.browser.document


fun main(args: Array<String>) {
    document.styleSheets[0].asDynamic().addRule("body *", "position:absolute")
    document.styleSheets[0].asDynamic().addRule("b",      "overflow:hidden"  )
    document.styleSheets[0].asDynamic().addRule("pre",    "overflow:hidden"  )

    val display = DisplayImpl(document.body!!)

    display.fill(SolidBrush.create(Color.Red))

    val renderManager = RenderManagerImpl(display, DummyUIManager, SchedulerImpl(), RealGraphicsDevice(RealGraphicsSurfaceFactory()))
}

object DummyUIManager: UIManager {
    override val installedTheme: UIManager.ThemeInfo
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val availableThemes: List<UIManager.ThemeInfo>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun installUI(gizmo: Gizmo, aResponse: UIManager.UIResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun revalidateUI(gizmo: Gizmo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun installTheme(theme: UIManager.ThemeInfo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> getDefaultValue(aName: String): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}