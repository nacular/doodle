//package com.zinoti.jaz.containers
//
//import com.zinoti.jaz.core.Container
//import com.zinoti.jaz.geometry.Dimension
//import com.zinoti.jaz.geometry.Rectangle
//import kotlin.math.max
//
//
//class ListLayout @JvmOverloads constructor(private val spacing: Int = 0): StatelessLayout() {
//
//    override fun layout(container: Container) {
//        // TODO: Can this be cleaned up to use idealSize?
//        val padding = container.padding ?: Padding.create()
//        var y       = padding.top
//        var width   = container.firstOrNull()?.run { idealSize?.width ?: width } ?: 0.0
//
//        container.asSequence().filter { it.visible }.forEach {
//            width = max(width, it.idealSize?.width ?: it.width)
//        }
//
//        var i = 0
//
//        container.asSequence().filter { it.visible }.forEach {
//            it.bounds = Rectangle.create(padding.left, y, width, it.height)
//
//            y += it.height + if (++i < container.children.size) spacing else 0
//        }
//
//        val size = Dimension.create(width + padding.left + padding.right, y + padding.bottom)
//
//        container.idealSize   = size // FIXME: Do we need this?
//        container.minimumSize = size
//
//        if (container.parent?.layout == null) {
//            container.size = size
//        }
//
//        container.idealSize = size
//    }
//
//    override fun idealSize(container: Container): Dimension? {
//        var width       = 0.0
//        val padding     = container.padding ?: Padding.create()
//        var y           = padding.top
//
//        if (container.children.size > 0) {
//            val aChild     = container.first()
//            val aIdealSize = aChild.idealSize
//
//            width = aIdealSize?.width ?: aChild.width
//        }
//
//        var i = 0
//
//        container.asSequence().filter { it.visible }.forEach {
//            width = max(width, it.idealSize?.width ?: it.width)
//
//            y += it.height + if (++i < container.children.size) spacing else 0
//        }
//
//        return Dimension.create(width + padding.left + padding.right, y + padding.bottom)
//    }
//}
