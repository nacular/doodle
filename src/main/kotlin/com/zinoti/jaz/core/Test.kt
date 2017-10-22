import com.zinoti.jaz.core.impl.DisplayImpl
import com.zinoti.jaz.drawing.Color
import com.zinoti.jaz.drawing.SolidBrush
import org.w3c.dom.css.get
import kotlin.browser.document

fun main(args: Array<String>) {
    document.styleSheets[0].asDynamic().addRule("body *", "position:absolute")
    document.styleSheets[0].asDynamic().addRule("b",      "overflow:hidden"  )
    document.styleSheets[0].asDynamic().addRule("pre",    "overflow:hidden")

    val display = DisplayImpl(document.body!!)

    display.fill(SolidBrush.create(Color.Red))
}