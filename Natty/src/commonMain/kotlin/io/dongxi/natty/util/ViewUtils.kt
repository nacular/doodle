package io.dongxi.natty.util

import io.dongxi.natty.tabbedpanel.CenterView
import io.dongxi.natty.tabbedpanel.FooterView
import io.dongxi.natty.tabbedpanel.LeftView
import io.dongxi.natty.tabbedpanel.RightView
import io.dongxi.natty.util.ClassUtils.simpleClassName
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width

object ViewUtils {

    data class NattyViewBounds(
        val top: Double,
        val left: Double,
        val width: Double,
        val bottom: Double
    )


    fun getContentViewBoundaries(display: Display): Map<String, NattyViewBounds> {
        /*
        left.top eq 5
        left.left eq 5
        left.width eq display.width / 4
        left.bottom eq display.height - 200
         */
        val leftView = NattyViewBounds(
            5.00,
            5.00,
            display.width / 4,
            display.height - 200
        )

        /*
        center.top eq left.top
        center.left eq left.right + 5
        center.width eq display.width / 2
        center.bottom eq left.bottom
         */
        val centerView = NattyViewBounds(
            leftView.top,
            (display.width / 4) + 10,
            display.width / 2,
            leftView.bottom
        )

        /*
        right.top eq left.top
        right.left eq center.right + 5
        right.right eq display.width - 5
        right.bottom eq left.bottom
        */
        // TODO
        val rightView = NattyViewBounds(
            leftView.top,
            centerView.left + centerView.width + 5,
            display.width - 10,
            leftView.bottom
        )

        // TODO Buggy right side
        val footerView = NattyViewBounds(
            5.00,
            5.00,
            display.width / 4,
            display.height - 200
        )

        return mapOf(
            LeftView::class.simpleName.toString() to leftView,
            CenterView::class.simpleName.toString() to centerView,
            RightView::class.simpleName.toString() to rightView,
            FooterView::class.simpleName.toString() to footerView
        )
    }


    private fun isLeftView(contentView: View): Boolean {
        return simpleClassName(contentView) == LeftView::class.simpleName.toString()
    }

    private fun isCenterView(contentView: View): Boolean {
        return simpleClassName(contentView) == CenterView::class.simpleName.toString()
    }

    private fun isRightView(contentView: View): Boolean {
        return simpleClassName(contentView) == RightView::class.simpleName.toString()
    }

    private fun isFooterView(contentView: View): Boolean {
        return simpleClassName(contentView) == FooterView::class.simpleName.toString()
    }
}
