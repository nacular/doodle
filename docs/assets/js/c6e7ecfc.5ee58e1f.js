"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[696],{7074:function(e,n,t){t.r(n),t.d(n,{assets:function(){return h},contentTitle:function(){return c},default:function(){return g},frontMatter:function(){return p},metadata:function(){return m},toc:function(){return u}});var a=t(3117),i=t(102),o=(t(7294),t(3905)),s=t(9877),r=t(8215),l=t(3138),d=["components"],p={hide_title:!0},c="Layout",m={unversionedId:"positioning",id:"positioning",title:"Layout",description:"Every View has an x, y position (in pixels) relative to its parent. This is exactly where the View will be rendered--unless it (or an ancestor) also has",source:"@site/docs/positioning.mdx",sourceDirName:".",slug:"/positioning",permalink:"/doodle/docs/positioning",tags:[],version:"current",frontMatter:{hide_title:!0},sidebar:"tutorialSidebar",previous:{title:"Displaying Views",permalink:"/doodle/docs/display"},next:{title:"Transformations",permalink:"/doodle/docs/transforms"}},h={},u=[{value:"Manual positioning",id:"manual-positioning",level:2},{value:"Transforms",id:"transforms",level:2},{value:"Using Layouts",id:"using-layouts",level:2},{value:"Constraint-based Layout",id:"constraint-based-layout",level:2},{value:"Custom Layouts",id:"custom-layouts",level:2},{value:"Deciding When Layout Happens",id:"deciding-when-layout-happens",level:2}],k={toc:u};function g(e){var n=e.components,t=(0,i.Z)(e,d);return(0,o.kt)("wrapper",(0,a.Z)({},k,t,{components:n,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"layout"},"Layout"),(0,o.kt)("p",null,"Every View has an ",(0,o.kt)("inlineCode",{parentName:"p"},"x"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"y")," position (in pixels) relative to its parent. This is exactly where the View will be rendered--unless it (or an ancestor) also has\na ",(0,o.kt)("inlineCode",{parentName:"p"},"transform"),". Doodle ensures that there is never a disconnect between a View's position, transform and render coordinates."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"val panel = view { size = Size(100.0) }\n\ndisplay += view // view's position is 0,0\n")),(0,o.kt)("h2",{id:"manual-positioning"},"Manual positioning"),(0,o.kt)("p",null,"You can set the View's ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L165"},(0,o.kt)("inlineCode",{parentName:"a"},"x")),",\n",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L170"},(0,o.kt)("inlineCode",{parentName:"a"},"y")),", or\n",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L175"},(0,o.kt)("inlineCode",{parentName:"a"},"position"))," properties directly\nto move it around. These are proxies to the View's ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L198"},(0,o.kt)("inlineCode",{parentName:"a"},"bounds")),"\nproperty, which represents its rectangular boundary relative to its parent."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"view.x = 10.0                 // move to 10,0\nview.position = Point(13, -2) // reposition to 13,-2\n")),(0,o.kt)(l.B,{functionName:"positioning",height:"400",mdxType:"DoodleCodeBlock"}),(0,o.kt)("p",null,"This demo shows how the pointer can be used to position Views easily. In this case, we use the ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/utils/Resizer.kt#L31"},"Resizer"),"\nutility to provide simple resize/move operations. The Resizer simply monitors the View for Pointer events and updates its ",(0,o.kt)("inlineCode",{parentName:"p"},"bounds")," accordingly."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"import io.nacular.doodle.utils.Resizer\n\n// ...\n\nMyView().apply {\n    bounds = Rectangle(100, 100)\n    Resizer(this) // monitors the View and manages resize/move\n}\n")),(0,o.kt)("h2",{id:"transforms"},"Transforms"),(0,o.kt)("p",null,"Views can also have\n",(0,o.kt)("a",{parentName:"p",href:"transforms"},"transformations")," to change how they are displayed. A transformed View still\nretains the same ",(0,o.kt)("inlineCode",{parentName:"p"},"bounds"),", but its ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L241"},(0,o.kt)("inlineCode",{parentName:"a"},"boundingBox"))," property changes, since it reflects the smallest rectangle that encloses the View's\n",(0,o.kt)("strong",{parentName:"p"},"transformed")," bounds."),(0,o.kt)(l.B,{functionName:"transforms",height:"400",mdxType:"DoodleCodeBlock"}),(0,o.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},(0,o.kt)("inlineCode",{parentName:"p"},"boundingBox")," == ",(0,o.kt)("inlineCode",{parentName:"p"},"bounds")," when\n",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L231"},(0,o.kt)("inlineCode",{parentName:"a"},"transform"))," ==\n",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/drawing/AffineTransform.kt#L272"},(0,o.kt)("inlineCode",{parentName:"a"},"Identity")),"."))),(0,o.kt)("h2",{id:"using-layouts"},"Using Layouts"),(0,o.kt)("p",null,"A ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Layout.kt#L80"},(0,o.kt)("inlineCode",{parentName:"a"},"Layout"))," keeps track\nof a View and its children and automatically arranges the children as sizes change. This happens (by default) whenever View's ",(0,o.kt)("inlineCode",{parentName:"p"},"size")," changes, or one of its children has its ",(0,o.kt)("inlineCode",{parentName:"p"},"bounds")," change."),(0,o.kt)("p",null,"The View class also ",(0,o.kt)("inlineCode",{parentName:"p"},"protects")," its ",(0,o.kt)("inlineCode",{parentName:"p"},"layout")," property from callers, but sub-classes are free to expose\nit."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"val container = container {}\n\ncontainer.layout = HorizontalFlowLayout() // Container exposes its layout\n")),(0,o.kt)(l.B,{functionName:"flowLayout",height:"400",mdxType:"DoodleCodeBlock"}),(0,o.kt)("p",null,(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/layout/HorizontalFlowLayout.kt#L16"},(0,o.kt)("inlineCode",{parentName:"a"},"HorizontalFlowLayout")),"\nwraps a View's children from left to right within its bounds."),(0,o.kt)("div",{className:"admonition admonition-caution alert alert--warning"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"}))),"caution")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Changes to a View's ",(0,o.kt)("inlineCode",{parentName:"p"},"transform")," will not trigger layout."))),(0,o.kt)("h2",{id:"constraint-based-layout"},"Constraint-based Layout"),(0,o.kt)("p",null,"This Layout uses anchor points to pin the ",(0,o.kt)("inlineCode",{parentName:"p"},"top"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"left"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"bottom"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"right"),", ",(0,o.kt)("inlineCode",{parentName:"p"},"centerX"),", and ",(0,o.kt)("inlineCode",{parentName:"p"},"cetnerY")," points of Views. It also allows you to\nspecify values for ",(0,o.kt)("inlineCode",{parentName:"p"},"width")," and ",(0,o.kt)("inlineCode",{parentName:"p"},"height"),". This covers many of the common layout use cases and is easy to use."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"val container = container {}\nval panel1    = view {}\nval panel2    = view {}\n\ncontainer += listOf(panel1, panel2)\n\n// use Layout that follows constraints to position items\ncontainer.layout = constrain(panel1, panel2) { panel1, panel2 ->\n    panel1.top    = parent.top\n    panel1.left   = parent.left\n    panel1.right  = parent.right\n    panel1.height = constant(100.0)\n\n    panel2.top    = panel1.bottom\n    panel2.left   = panel1.left\n    panel2.right  = panel1.right\n    panel2.bottom = parent.bottom\n}\n")),(0,o.kt)("div",{className:"admonition admonition-caution alert alert--warning"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"}))),"caution")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},(0,o.kt)("inlineCode",{parentName:"p"},"constrain")," only supports positioning for siblings within the same parent."))),(0,o.kt)("h2",{id:"custom-layouts"},"Custom Layouts"),(0,o.kt)("p",null,"Doodle comes with several useful layouts, including one based on constraints. You can also create custom Layouts very easily.\nJust implement the ",(0,o.kt)("inlineCode",{parentName:"p"},"Layout")," interface:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"class CustomLayout: Layout {\n    override fun layout(container: PositionableContainer) {\n        container.children.filter { it.visible }.forEach { child ->\n            child.bounds = Rectangle(/*...*/)\n        }\n    }\n}\n")),(0,o.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Layouts do not work with View directly because it does not expose its children. ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/Layout.kt#L33"},(0,o.kt)("inlineCode",{parentName:"a"},"PositionableContainer"))," proxies the\nmanaged View instead."))),(0,o.kt)("h2",{id:"deciding-when-layout-happens"},"Deciding When Layout Happens"),(0,o.kt)("p",null,"Layouts are generally triggered whenever their container's size changes or a child of their container has a bounds change. But there are cases\nwhen this default behavior does not work as well. A good example is a Layout that uses a child's ",(0,o.kt)("inlineCode",{parentName:"p"},"idealSize")," in positioning. Such a Layout\nwon't be invoked when the ",(0,o.kt)("inlineCode",{parentName:"p"},"idealSize"),"s change, and will be out of date in some cases. The following demo shows this."),(0,o.kt)(s.Z,{mdxType:"Tabs"},(0,o.kt)(r.Z,{value:"demo",label:"Demo",mdxType:"TabItem"},(0,o.kt)(l.B,{functionName:"layoutIdealIssue",height:"400",mdxType:"DoodleCodeBlock"})),(0,o.kt)(r.Z,{value:"usage",label:"Usage",mdxType:"TabItem"},(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"container {\n    repeat(2) {\n        this += BlueView().apply { size = Size(50) }\n    }\n\n    // This Layout does not override\n    // requiresLayout(child: Positionable,\n    //       of: PositionableContainer,\n    //       old: SizePreferences,\n    //       new: SizePreferences\n    // ): Boolean\n    // Which means it defaults to ignoring changes to child SizePreferences\n    layout = object: Layout {\n        override fun layout(container: PositionableContainer) {\n            var x = 0.0\n            container.children.forEach {\n                it.x = x\n                x += (it.idealSize?.width ?: it.width) + 1\n            }\n        }\n    }\n\n    size   = Size(200)\n    render = {\n        rect(bounds.atOrigin, Lightgray.lighter())\n    }\n\n    Resizer(this)\n}\n")))),(0,o.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Moving the slider changes the ideal width of the blue boxes. But the container isn't udpated because the Layout used does not indicate\nit needs an updated when ",(0,o.kt)("inlineCode",{parentName:"p"},"sizePreferences")," change via ",(0,o.kt)("inlineCode",{parentName:"p"},"requiresLayout"),"."),(0,o.kt)("p",{parentName:"div"},"You can see that it is out of date by resizing the container after moving the slider."))),(0,o.kt)("p",null,"This is why Doodle offers Layouts a chance to customize when they are invoked. In fact, Layouts are asked whether they want to respond to\nseveral potential triggers. These include size changes in the container, bounds and size preference changes for children. The latter happens\nwhenever ",(0,o.kt)("inlineCode",{parentName:"p"},"minimumSize")," or ",(0,o.kt)("inlineCode",{parentName:"p"},"idealSize")," are updated for a child. This way, a Layout can fine tune what triggers it."),(0,o.kt)("p",null,"The following shows how updating the Layout so it replies to ",(0,o.kt)("inlineCode",{parentName:"p"},"requiresLayout")," for this scenario fixes the issue."),(0,o.kt)(s.Z,{mdxType:"Tabs"},(0,o.kt)(r.Z,{value:"demo",label:"Demo",mdxType:"TabItem"},(0,o.kt)(l.B,{functionName:"layoutIdealIssue",args:"[true]",height:"400",mdxType:"DoodleCodeBlock"})),(0,o.kt)(r.Z,{value:"usage",label:"Usage",mdxType:"TabItem"},(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"container {\n    repeat(2) {\n        this += BlueView().apply { size = Size(50) }\n    }\n\n    layout = object: Layout {\n        // Request layout whenever a child's idealSize changes\n        // (and the updateOnIdealChange switch is tuned on)\n        override fun requiresLayout(\n            child: Positionable,\n            of  : PositionableContainer,\n            old : View.SizePreferences,\n            new : View.SizePreferences\n        ) = updateOnIdealChange && old.idealSize != new.idealSize\n\n        // This Layout is very unusual (b/c it is contrived) in that it does not depend\n        // on the container's size. So it ignores these changes.\n        override fun requiresLayout(container: PositionableContainer, old: Size, new: Size) = false\n\n        override fun layout(container: PositionableContainer) {\n            var x = 0.0\n            container.children.forEach {\n                it.x = x\n                x += (it.idealSize?.width ?: it.width) + 1\n            }\n        }\n    }\n\n    size   = Size(200)\n    render = {\n        rect(bounds.atOrigin, Lightgray.lighter())\n    }\n\n    Resizer(this)\n}\n")))),(0,o.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Notice that this Layout will actually ignore changes to the container's ",(0,o.kt)("inlineCode",{parentName:"p"},"size"),"! Layouts are free to do that if the\ncontainer's ",(0,o.kt)("inlineCode",{parentName:"p"},"size")," is irrelevant to the positioning of its children. This is very unlikely, but there might be\ncases where one dimension of ",(0,o.kt)("inlineCode",{parentName:"p"},"size"),", maybe ",(0,o.kt)("inlineCode",{parentName:"p"},"width")," or ",(0,o.kt)("inlineCode",{parentName:"p"},"height")," is irrelevant. In which case the Layout can\nignore updates if only that component changes."))))}g.isMDXComponent=!0}}]);