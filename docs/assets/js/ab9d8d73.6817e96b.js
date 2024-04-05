"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[3568],{9288:(o,n,e)=>{e.d(n,{cp:()=>r});var i=e(7624),a=e(2172),t=(e(1268),e(5388),e(5720));function l(o){const n={admonition:"admonition",p:"p",...(0,a.M)(),...o.components};return(0,i.jsxs)(n.admonition,{title:"Module Required",type:"info",children:[(0,i.jsxs)("p",{children:["You must include the ",o.link," in your application in order to use these features."]}),(0,i.jsx)(t.A,{children:o.module}),(0,i.jsx)(n.p,{children:"Doodle uses opt-in modules like this to improve bundle size."})]})}function r(o={}){const{wrapper:n}={...(0,a.M)(),...o.components};return n?(0,i.jsx)(n,{...o,children:(0,i.jsx)(l,{...o})}):l(o)}},8372:(o,n,e)=>{e.r(n),e.d(n,{assets:()=>k,contentTitle:()=>y,default:()=>j,frontMatter:()=>v,metadata:()=>x,toc:()=>b});var i=e(7624),a=e(2172),t=e(1268),l=e(5388),r=e(9288),p=e(7996),d=e(5720),s=e(3148);const c="package popups\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.PopupModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.controls.PopupManager\nimport io.nacular.doodle.core.Display\nimport org.kodein.di.instance\n\nclass PopupApp(display: Display, popupManager: PopupManager): Application {\n    init {\n        // ..\n    }\n\n    override fun shutdown() {}\n}\n\n//sampleStart\nfun main() {\n    application(modules = listOf(PopupModule)) {\n        PopupApp(display = instance(), popupManager = instance())\n    }\n}\n//sampleEnd",u="package popups\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.ModalModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.controls.modal.ModalManager\nimport io.nacular.doodle.core.Display\nimport org.kodein.di.instance\n\nclass ModalApp(display: Display, modalManager: ModalManager): Application {\n    init {\n        // ..\n    }\n\n    override fun shutdown() {}\n}\n\nfun example() {\n//sampleStart\n    fun main() {\n        application(modules = listOf(ModalModule)) {\n            ModalApp(display = instance(), modalManager = instance())\n        }\n    }\n//sampleEnd\n}\n",m='package io.nacular.doodle.docs.apps\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.controls.PopupManager\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.center\nimport io.nacular.doodle.docs.utils.DEFAULT_FONT_FAMILIES\nimport io.nacular.doodle.docs.utils.DEFAULT_FONT_SIZE\nimport io.nacular.doodle.docs.utils.blueClick\nimport io.nacular.doodle.docs.utils.clickView\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.FontLoader\nimport io.nacular.doodle.drawing.TextMetrics\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.event.PointerListener.Companion.clicked\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.doodle.geometry.Size\nimport io.nacular.doodle.image.ImageLoader\nimport io.nacular.doodle.utils.Resizer\nimport kotlinx.coroutines.CoroutineDispatcher\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.SupervisorJob\nimport kotlinx.coroutines.launch\n\nclass SimplePopup(\n    display     : Display,\n    popups      : PopupManager,\n    fonts       : FontLoader,\n    images      : ImageLoader,\n    textMetrics : TextMetrics,\n    uiDispatcher: CoroutineDispatcher,\n): Application {\n    init {\n        val appScope = CoroutineScope(SupervisorJob() + uiDispatcher)\n\n        appScope.launch {\n            val font = fonts {\n                families = DEFAULT_FONT_FAMILIES\n                size     = DEFAULT_FONT_SIZE * 2\n            }!!\n\n            val image = images.load("/doodle/images/touch.svg")!!\n\n            val popup = clickView(textMetrics).apply {\n                this.font       = font\n                pointerChanged += clicked { popups.hide(this) }\n            }\n\n//sampleStart\n            // Hide popup when it is clicked\n            popup.pointerChanged += clicked { popups.hide(popup) }\n\n            display += blueClick(image).apply {\n                size       = Size(400, 200)\n                position   = display.center - Point(width / 2, height / 2)\n\n                // show popup when blue view clicked\n                pointerChanged += clicked {\n                    popups.show(popup) {\n                        // size / position popup\n                        it.height eq parent.height / 2\n                        it.width  eq it.height * 1.5\n                        it.center eq parent.center\n                    }\n                }\n\n                Resizer(this)\n            }\n//sampleEnd\n\n            display.fill(White.paint)\n        }\n    }\n\n    override fun shutdown() {}\n}',h='package io.nacular.doodle.docs.apps\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.controls.PopupManager\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.center\nimport io.nacular.doodle.docs.utils.DEFAULT_FONT_FAMILIES\nimport io.nacular.doodle.docs.utils.DEFAULT_FONT_SIZE\nimport io.nacular.doodle.docs.utils.blueClick\nimport io.nacular.doodle.docs.utils.clickView\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.FontLoader\nimport io.nacular.doodle.drawing.TextMetrics\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.event.PointerListener.Companion.clicked\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.doodle.geometry.Size\nimport io.nacular.doodle.image.ImageLoader\nimport io.nacular.doodle.layout.constraints.Strength.Companion.Strong\nimport io.nacular.doodle.utils.Resizer\nimport kotlinx.coroutines.CoroutineDispatcher\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.SupervisorJob\nimport kotlinx.coroutines.launch\n\nclass RelativePopup(\n    display     : Display,\n    popups      : PopupManager,\n    fonts       : FontLoader,\n    images      : ImageLoader,\n    textMetrics : TextMetrics,\n    uiDispatcher: CoroutineDispatcher,\n): Application {\n    init {\n        val appScope = CoroutineScope(SupervisorJob() + uiDispatcher)\n\n        appScope.launch {\n            val font = fonts {\n                families = DEFAULT_FONT_FAMILIES\n                size     = DEFAULT_FONT_SIZE * 2\n            }!!\n\n            val image = images.load("/doodle/images/touch.svg")!!\n\n            val popup = clickView(textMetrics).apply {\n                this.font       = font\n                pointerChanged += clicked { popups.hide(this) }\n            }\n\n//sampleStart\n            // Hide popup when it is clicked\n            popup.pointerChanged += clicked { popups.hide(popup) }\n\n            display += blueClick(image).apply {\n                size     = Size(300, 200)\n                position = display.center - Point(width / 2, height / 2)\n\n                // show popup when blue view clicked\n                pointerChanged += clicked {\n                    popups.show(popup, relativeTo = this) { popup, blueView ->\n                        // size / position popup\n                        (popup.top    eq       blueView.y         ) .. Strong\n                        (popup.left   eq       blueView.right + 10) .. Strong\n                        (popup.bottom lessEq   parent.bottom  -  5) .. Strong\n\n                        popup.top    greaterEq 5\n                        popup.left   greaterEq 5\n                        popup.right  lessEq    parent.right - 5\n                        popup.height eq        parent.height / 2\n                        popup.width  eq        popup.height * 1.5\n                    }\n                }\n\n                Resizer(this)\n            }\n//sampleEnd\n\n            display.fill(White.paint)\n        }\n    }\n\n    override fun shutdown() {}\n}',g="package popups\n\nimport io.nacular.doodle.controls.modal.ModalManager\nimport io.nacular.doodle.controls.modal.ModalManager.Modal\nimport io.nacular.doodle.core.view\n\nsuspend fun <T> modalCreation(modal: ModalManager, result: T) {\n//sampleStart\n    // launch modal and await result (suspending)\n    val value: T = modal {\n        Modal(\n            // View used as modal\n            view {\n                // ...\n\n                // call completed with user input when done\n                completed(result)\n            }\n        ) {\n            // optionally provide a layout block\n            // or the view will default to being\n            // displayed in the center\n        }\n    }\n//sampleEnd\n}\n",w="package popups\n\nimport io.nacular.doodle.controls.modal.ModalManager\nimport io.nacular.doodle.controls.modal.ModalManager.RelativeModal\nimport io.nacular.doodle.core.view\n\nsuspend fun <T> relativeModal(modal: ModalManager, result: T) {\n//sampleStart\n    val someView = view {}\n\n    // launch modal and await result (suspending)\n    val value: T = modal {\n        RelativeModal(\n            // View used as modal\n            view {\n                // ...\n\n                // call completed when the modal is done\n                completed(result)\n            },\n            relativeTo = someView\n        ) { modal, someViewBounds ->\n\n            // position relative to parent and someView\n        }\n    }\n//sampleEnd\n}\n",M='package io.nacular.doodle.docs.apps\n\nimport io.nacular.doodle.animation.Animation\nimport io.nacular.doodle.animation.Animator\nimport io.nacular.doodle.animation.invoke\nimport io.nacular.doodle.animation.transition.easeOutBack\nimport io.nacular.doodle.animation.transition.easeOutBounce\nimport io.nacular.doodle.animation.transition.linear\nimport io.nacular.doodle.animation.tweenFloat\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.controls.buttons.PushButton\nimport io.nacular.doodle.controls.modal.ModalManager\nimport io.nacular.doodle.controls.modal.ModalManager.Modal\nimport io.nacular.doodle.controls.text.Label\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.View\nimport io.nacular.doodle.core.center\nimport io.nacular.doodle.core.then\nimport io.nacular.doodle.docs.utils.ClickMe\nimport io.nacular.doodle.docs.utils.DEFAULT_FONT_FAMILIES\nimport io.nacular.doodle.docs.utils.DEFAULT_FONT_SIZE\nimport io.nacular.doodle.drawing.AffineTransform.Companion.Identity\nimport io.nacular.doodle.drawing.Canvas\nimport io.nacular.doodle.drawing.Color.Companion.Black\nimport io.nacular.doodle.drawing.Color.Companion.Lightgray\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.FontLoader\nimport io.nacular.doodle.drawing.TextMetrics\nimport io.nacular.doodle.drawing.lerp\nimport io.nacular.doodle.drawing.opacity\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.event.PointerListener.Companion.clicked\nimport io.nacular.doodle.layout.constraints.constrain\nimport io.nacular.doodle.layout.constraints.fill\nimport io.nacular.doodle.theme.Theme\nimport io.nacular.doodle.theme.ThemeManager\nimport io.nacular.doodle.utils.Dimension.Height\nimport io.nacular.doodle.utils.TextAlignment.Start\nimport io.nacular.doodle.utils.autoCanceling\nimport io.nacular.doodle.utils.lerp\nimport io.nacular.measured.units.Angle.Companion.degrees\nimport io.nacular.measured.units.Time.Companion.milliseconds\nimport io.nacular.measured.units.times\nimport kotlinx.coroutines.CoroutineDispatcher\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.SupervisorJob\nimport kotlinx.coroutines.launch\n\nclass ModalApp(\n    display     : Display,\n    modal       : ModalManager,\n    animate     : Animator,\n    themeManager: ThemeManager,\n    theme       : Theme,\n    fonts       : FontLoader,\n    textMetrics : TextMetrics,\n    uiDispatcher: CoroutineDispatcher,\n): Application {\n    init {\n        val appScope = CoroutineScope(SupervisorJob() + uiDispatcher)\n\n        appScope.launch {\n            val font = fonts {\n                families = DEFAULT_FONT_FAMILIES\n                size     = DEFAULT_FONT_SIZE\n            }!!\n\n            // for basic Button Behavior\n            themeManager.selected = theme\n\n            val clickMe = ClickMe(textMetrics).also { it.font = font }\n\n//sampleStart\n            clickMe.pointerChanged += clicked {\n                appScope.launch {\n                    modal {\n                        val popup    = createPopup(this::completed).also { it.font = font }\n                        val duration = 250 * milliseconds\n\n                        var jiggleAnimation: Animation<*>? by autoCanceling()\n\n                        // Shake the popup when the pointer clicked outside of it\n                        pointerOutsideModalChanged += clicked {\n                            jiggleAnimation = animate(1f to 0f, tweenFloat(easeOutBounce, duration)) {\n                                popup.transform = Identity.rotate(around = popup.center, by = 2 * degrees * it)\n                            }\n                        }\n\n                        var layoutProgress = 0f\n\n                        animate {\n                            // Animate background\n                            0f to 1f using (tweenFloat(linear, duration)) {\n                                background = lerp(Lightgray opacity 0f, Lightgray opacity 0.5f, it).paint\n                            }\n\n                            // Animate layout\n                            0f to 1f using (tweenFloat(easeOutBack, duration)) {\n                                layoutProgress = it // modify values used in layout\n                                reLayout()          // ask modal to update its layout\n                            }\n                        }\n\n                        // Show modal with popup using the given layout constraints\n                        Modal(popup) {\n                            it.centerX eq parent.centerX\n                            it.centerY eq lerp(-it.height.readOnly / 2, parent.centerY.readOnly, layoutProgress)\n                        }\n                    }\n                }\n            }\n//sampleEnd\n\n            display += clickMe\n            display.fill(White.paint)\n            display.layout = constrain(display.first(), fill)\n        }\n    }\n\n    private fun createPopup(completed: (Unit) -> Unit) = object: View() {\n        init {\n            width              = 300.0\n            clipCanvasToBounds = false\n\n            children += Label("Thanks for clicking. Now please press Ok to acknowledge.").apply {\n                fitText       = setOf(Height)\n                wrapsWords    = true\n                textAlignment = Start\n            }\n\n            children += PushButton("Ok").apply { fired += { completed(Unit) } }\n\n            layout = constrain(children[0], children[1]) { label, button ->\n                label.top      eq 20\n                label.left     eq 20\n                label.right    eq parent.right - 20\n                label.height.preserve\n\n                button.top     eq label.bottom + 20\n                button.width   eq parent.width / 4\n                button.height  eq 30\n                button.centerX eq parent.centerX\n            }.then {\n                height = children.last().bounds.bottom + 20\n            }\n        }\n\n        override fun render(canvas: Canvas) {\n            canvas.outerShadow(blurRadius = 10.0, color = Black opacity 0.05f) {\n                canvas.rect(bounds.atOrigin, radius = 10.0, fill = White.paint)\n            }\n        }\n    }\n\n    override fun shutdown() {}\n}',f='package io.nacular.doodle.docs.apps\n\nimport io.nacular.doodle.animation.Animation\nimport io.nacular.doodle.animation.Animator\nimport io.nacular.doodle.animation.invoke\nimport io.nacular.doodle.animation.transition.easeOutBounce\nimport io.nacular.doodle.animation.transition.linear\nimport io.nacular.doodle.animation.tweenFloat\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.controls.buttons.PushButton\nimport io.nacular.doodle.controls.modal.ModalManager\nimport io.nacular.doodle.controls.modal.ModalManager.Modal\nimport io.nacular.doodle.controls.text.Label\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.View\nimport io.nacular.doodle.core.center\nimport io.nacular.doodle.core.then\nimport io.nacular.doodle.docs.utils.ClickMe\nimport io.nacular.doodle.docs.utils.DEFAULT_FONT_FAMILIES\nimport io.nacular.doodle.docs.utils.DEFAULT_FONT_SIZE\nimport io.nacular.doodle.drawing.AffineTransform.Companion.Identity\nimport io.nacular.doodle.drawing.Canvas\nimport io.nacular.doodle.drawing.Color\nimport io.nacular.doodle.drawing.Color.Companion.Black\nimport io.nacular.doodle.drawing.Color.Companion.Blue\nimport io.nacular.doodle.drawing.Color.Companion.Lightgray\nimport io.nacular.doodle.drawing.Color.Companion.Pink\nimport io.nacular.doodle.drawing.Color.Companion.Red\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.FontLoader\nimport io.nacular.doodle.drawing.TextMetrics\nimport io.nacular.doodle.drawing.lerp\nimport io.nacular.doodle.drawing.opacity\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.drawing.stripedPaint\nimport io.nacular.doodle.event.PointerListener.Companion.clicked\nimport io.nacular.doodle.layout.constraints.constrain\nimport io.nacular.doodle.layout.constraints.fill\nimport io.nacular.doodle.theme.Theme\nimport io.nacular.doodle.theme.ThemeManager\nimport io.nacular.doodle.utils.Dimension.Height\nimport io.nacular.doodle.utils.TextAlignment\nimport io.nacular.doodle.utils.autoCanceling\nimport io.nacular.measured.units.Angle.Companion.degrees\nimport io.nacular.measured.units.Time.Companion.milliseconds\nimport io.nacular.measured.units.times\nimport kotlinx.coroutines.CoroutineDispatcher\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.SupervisorJob\nimport kotlinx.coroutines.launch\n\nclass RedOrBlueApp(\n    display     : Display,\n    modal       : ModalManager,\n    animate     : Animator,\n    themeManager: ThemeManager,\n    theme       : Theme,\n    fonts       : FontLoader,\n    textMetrics : TextMetrics,\n    uiDispatcher: CoroutineDispatcher,\n): Application {\n    init {\n        val appScope = CoroutineScope(SupervisorJob() + uiDispatcher)\n\n        appScope.launch {\n            val font = fonts {\n                families = DEFAULT_FONT_FAMILIES\n                size     = DEFAULT_FONT_SIZE\n            }!!\n\n            // for basic Button Behavior\n            themeManager.selected = theme\n\n            val clickMe = ClickMe(textMetrics).also { it.font = font }\n\n//sampleStart\n            clickMe.pointerChanged += clicked {\n                appScope.launch {\n                    val color = modal {\n                        val popup    = createPopup(this::completed).also { it.font = font; it.opacity = 0f }\n                        val duration = 250 * milliseconds\n\n                        var bounceAnimation: Animation<*>? by autoCanceling()\n\n                        // Bounce the popup when the pointer clicked outside of it\n                        pointerOutsideModalChanged += clicked {\n                            bounceAnimation = animate(1f to 0f, tweenFloat(easeOutBounce, duration)) {\n                                popup.transform = Identity.scale(around = popup.center, 1.0 - (.05 * it), 1.0 - (.05 * it))\n                            }\n                        }\n\n                        animate {\n                            // Animate background\n                            0f to 1f using (tweenFloat(linear, duration)) {\n                                popup.opacity = it\n                                background    = stripedPaint(\n                                    stripeWidth  = 50.0,\n                                    evenRowColor = lerp(Lightgray opacity 0f, Pink opacity 0.25f, it),\n                                    transform    = Identity.rotate(around = display.center, by = 45 * degrees * it)\n                                )\n                            }\n                        }\n\n                        // Show modal with popup (defaults to center layout)\n                        Modal(popup)\n                    }\n\n                    clickMe.bangColor = color\n                }\n            }\n//sampleEnd\n\n            display += clickMe\n            display.fill(White.paint)\n            display.layout = constrain(display.first(), fill)\n        }\n    }\n\n    private fun createPopup(completed: (Color) -> Unit) = object: View() {\n        init {\n            width              = 300.0\n            clipCanvasToBounds = false\n\n            children += Label("Which pill?").apply {\n                fitText       = setOf(Height)\n                wrapsWords    = true\n                TextAlignment.Center.also { textAlignment = it }\n            }\n\n            children += PushButton("Red" ).apply { fired += { completed(Red ) } }\n            children += PushButton("Blue").apply { fired += { completed(Blue) } }\n\n            layout = constrain(children[0], children[1], children[2]) { label, red, blue ->\n                label.top      eq 20\n                label.left     eq 20\n                label.right    eq parent.right - 20\n                label.height.preserve\n\n                red.top     eq label.bottom + 20\n                red.width   eq parent.width / 4\n                red.height  eq 30\n                red.right   eq parent.centerX - 5\n\n                blue.top     eq red.top\n                blue.width   eq red.width\n                blue.height  eq red.height\n                blue.left    eq red.right + 10\n            }.then {\n                height = children.last().bounds.bottom + 20\n            }\n        }\n\n        override fun render(canvas: Canvas) {\n            canvas.outerShadow(blurRadius = 10.0, color = Black opacity 0.05f) {\n                canvas.rect(bounds.atOrigin, radius = 10.0, fill = White.paint)\n            }\n        }\n    }\n\n    override fun shutdown() {}\n}',v={hide_title:!0,title:"Popups \u2022 Modals"},y=void 0,x={id:"modals",title:"Popups \u2022 Modals",description:"Popups and modals",source:"@site/docs/modals.mdx",sourceDirName:".",slug:"/modals",permalink:"/doodle/docs/modals",draft:!1,unlisted:!1,tags:[],version:"current",frontMatter:{hide_title:!0,title:"Popups \u2022 Modals"},sidebar:"tutorialSidebar",previous:{title:"Drag \u2022 Drop",permalink:"/doodle/docs/dragdrop"},next:{title:"Themes",permalink:"/doodle/docs/themes"}},k={},b=[{value:"Popups and modals",id:"popups-and-modals",level:2},{value:"Popups",id:"popups",level:2},{value:"Popup relative positioning",id:"popup-relative-positioning",level:2},{value:"Modals",id:"modals",level:2},{value:"Modal relative positioning",id:"modal-relative-positioning",level:2}];function C(o){const n={a:"a",admonition:"admonition",code:"code",h2:"h2",p:"p",...(0,a.M)(),...o.components};return s||T("api",!1),s.Color||T("api.Color",!0),s.Display||T("api.Display",!0),s.Layout||T("api.Layout",!0),s.Modal||T("api.Modal",!0),s.ModalManager||T("api.ModalManager",!0),s.ModalModule||T("api.ModalModule",!0),s.PopupManager||T("api.PopupManager",!0),s.PopupManagerShow||T("api.PopupManagerShow",!0),s.PopupModule||T("api.PopupModule",!0),s.RelativeModal||T("api.RelativeModal",!0),s.View||T("api.View",!0),(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(n.h2,{id:"popups-and-modals",children:"Popups and modals"}),"\n",(0,i.jsxs)(n.p,{children:["May apps need to show content above all other content periodically in the form of a popup, or overlay. You can do this manually by adding a ",(0,i.jsx)(n.a,{href:"display#adding-views-to-the-display",children:"top-level"})," ",(0,i.jsx)(s.View,{})," to the ",(0,i.jsx)(s.Display,{}),", but this has limitations. This approach would subject the View to the ",(0,i.jsx)(n.code,{children:"Display"}),"'s ",(0,i.jsx)(s.Layout,{}),", so controlling its position would be difficult. Moreover, new ",(0,i.jsx)(n.code,{children:"View"}),"s added to the ",(0,i.jsx)(n.code,{children:"Display"})," could easily be placed above that ",(0,i.jsx)(n.code,{children:"View"}),"."]}),"\n",(0,i.jsxs)(n.p,{children:["Doodle provides a solution to address this use case via the ",(0,i.jsx)(s.PopupManager,{}),". You can use this component to manage a set of ",(0,i.jsx)(n.code,{children:"Views"})," that overcome the limitations mentioned above. Moreover, popups shown using the manager can be positioned with flexible and powerful ",(0,i.jsx)(n.a,{href:"layouts/constraints",children:"constraints"})," to ensure they remain visible, or change size based on the items around them."]}),"\n",(0,i.jsx)(n.h2,{id:"popups",children:"Popups"}),"\n",(0,i.jsxs)(n.p,{children:["Any ",(0,i.jsx)(s.View,{})," can be shown as a popup. This will add the it to the ",(0,i.jsx)(s.Display,{})," above all existing ",(0,i.jsx)(n.code,{children:"View"}),"s, including other popups that are visible. This is done by invoking ",(0,i.jsx)(s.PopupManagerShow,{})," on the ",(0,i.jsx)(s.PopupManager,{}),"."]}),"\n",(0,i.jsx)(r.cp,{link:(0,i.jsx)(s.PopupModule,{}),module:c}),"\n",(0,i.jsx)(n.p,{children:"The following shows a simple popup that is shown whenever you click on the blue rectangle. Clicking the popup hides it again."}),"\n",(0,i.jsx)(p.u,{functionName:"simplePopup",height:"300"}),"\n",(0,i.jsx)(d.A,{children:m}),"\n",(0,i.jsx)(n.admonition,{type:"info",children:(0,i.jsxs)(n.p,{children:["Unlike ",(0,i.jsx)(n.a,{href:"#modals",children:"Modals"}),", regular popups do not prevent user interactions with the rest of the app."]})}),"\n",(0,i.jsx)(n.h2,{id:"popup-relative-positioning",children:"Popup relative positioning"}),"\n",(0,i.jsxs)(n.p,{children:["There are many use cases where a popup's position needs to be tied to another ",(0,i.jsx)(n.code,{children:"View"})," when it is shown. This is the case for drop down menus, hover callouts, tool tips, etc.. This is easy to achieve as well using the ",(0,i.jsx)(s.PopupManager,{}),". You simply use the variant of ",(0,i.jsx)(s.PopupManagerShow,{})," that takes a ",(0,i.jsx)(n.code,{children:"relativeTo"})," value. The manager will ensure the popup is positioned relative to the given ",(0,i.jsx)(n.code,{children:"View"})," based on the constraints specified."]}),"\n",(0,i.jsx)(n.p,{children:"This app shows a popup that is positioned relative to the blue rectangle. Move the rectangle around while the popup is visible to see how it works."}),"\n",(0,i.jsx)(p.u,{functionName:"relativePopup",height:"300"}),"\n",(0,i.jsx)(d.A,{children:h}),"\n",(0,i.jsx)(n.h2,{id:"modals",children:"Modals"}),"\n",(0,i.jsxs)(n.p,{children:["Modals are popups that can disable further interaction with the app (except for subsequent modals of course). They are useful when some user input is needed before proceeding. You can create a modal using any ",(0,i.jsx)(s.View,{})," (just like a popup) using the ",(0,i.jsx)(s.ModalManager,{}),". In fact, the ",(0,i.jsx)(n.code,{children:"ModalManager"})," uses the ",(0,i.jsx)(s.PopupManager,{})," internally to manage modals."]}),"\n",(0,i.jsx)(r.cp,{link:(0,i.jsx)(s.ModalModule,{}),module:u}),"\n",(0,i.jsxs)(n.p,{children:["Modals are asynchronous and return a value ",(0,i.jsx)(n.code,{children:"<T>"}),". This means you have to call them from a ",(0,i.jsx)(n.code,{children:"CoroutineContext"})," and you cannot get their result until they complete. This makes them ideal for user input."]}),"\n",(0,i.jsxs)(n.p,{children:["You launch a modal by invoking the ",(0,i.jsx)(s.ModalManager,{})," with a lambda that returns a ",(0,i.jsx)(s.Modal,{}),(0,i.jsx)(n.code,{children:"<T>"})," or ",(0,i.jsx)(s.RelativeModal,{}),(0,i.jsx)(n.code,{children:"<T>"}),". The type used in the return determines the value the modal will complete with. Completion is handled by calling the ",(0,i.jsx)(n.code,{children:"completed"})," method provided within the creation context."]}),"\n",(0,i.jsx)(d.A,{children:g}),"\n",(0,i.jsxs)(n.p,{children:["This app shows a simple modal that drops down from the top and requires the user to dismissing it. This modal returns ",(0,i.jsx)(n.code,{children:"Unit"})," as it's result when the user clicks 'Ok'."]}),"\n",(0,i.jsxs)(t.c,{children:[(0,i.jsx)(l.c,{value:"Demo",children:(0,i.jsx)(p.u,{functionName:"modals",height:"300"})}),(0,i.jsx)(l.c,{value:"Code",children:(0,i.jsx)(d.A,{children:M})})]}),"\n",(0,i.jsxs)(n.p,{children:["This shows how you might present a choice using a modal. Here we ask the user to select between two ",(0,i.jsx)(s.Color,{}),"s. This modal is centered (the default), it uses a different background treatment, and has a different animation when you click outside it."]}),"\n",(0,i.jsxs)(t.c,{children:[(0,i.jsx)(l.c,{value:"Demo",children:(0,i.jsx)(p.u,{functionName:"redOrBlue",height:"300"})}),(0,i.jsx)(l.c,{value:"Code",children:(0,i.jsx)(d.A,{children:f})})]}),"\n",(0,i.jsx)(n.h2,{id:"modal-relative-positioning",children:"Modal relative positioning"}),"\n",(0,i.jsxs)(n.p,{children:["Modals can also be positioned in relation to a ",(0,i.jsx)(s.View,{})," just like ",(0,i.jsx)(n.a,{href:"#popup-relative-positioning",children:"popups"}),". Simple return a ",(0,i.jsx)(s.RelativeModal,{})," instead when creating one."]}),"\n",(0,i.jsx)(d.A,{children:w})]})}function j(o={}){const{wrapper:n}={...(0,a.M)(),...o.components};return n?(0,i.jsx)(n,{...o,children:(0,i.jsx)(C,{...o})}):C(o)}function T(o,n){throw new Error("Expected "+(n?"component":"object")+" `"+o+"` to be defined: you likely forgot to import, pass, or provide it.")}}}]);