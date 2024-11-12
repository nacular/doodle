"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[8492],{9712:(e,o,n)=>{n.d(o,{SI:()=>t,cp:()=>d});var i=n(7624),a=n(4552),r=(n(7793),n(6236),n(7492));const t=[];function l(e){const o={admonition:"admonition",p:"p",...(0,a.M)(),...e.components};return(0,i.jsxs)(o.admonition,{title:"Module Required",type:"info",children:[(0,i.jsxs)("p",{children:["You must include the ",e.link," in your application in order to use these features."]}),(0,i.jsx)(r.A,{children:e.module}),(0,i.jsx)(o.p,{children:"Doodle uses opt-in modules like this to improve bundle size."})]})}function d(e={}){const{wrapper:o}={...(0,a.M)(),...e.components};return o?(0,i.jsx)(o,{...e,children:(0,i.jsx)(l,{...e})}):l(e)}},1004:(e,o,n)=>{n.r(o),n.d(o,{assets:()=>w,contentTitle:()=>f,default:()=>j,frontMatter:()=>y,metadata:()=>x,toc:()=>v});var i=n(7624),a=n(4552),r=n(7793),t=n(6236),l=n(5272),d=n(7492),s=n(3220),p=n(9712);const c='package rendering\n\nimport io.nacular.doodle.image.Image\nimport io.nacular.doodle.image.ImageLoader\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.launch\n\nfun loadImage(imageLoader: ImageLoader, scope: CoroutineScope) {\n//sampleStart\n    scope.launch {\n        val image: Image? = imageLoader.load("some_image_path")\n\n        // won\'t get here until load resolves\n        image?.let {\n            // ...\n        }\n    }\n//sampleEnd\n}',m="package rendering\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.ImageModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.image.ImageLoader\nimport org.kodein.di.instance\n\nclass ImageLoaderApp(display: Display, images: ImageLoader): Application {\n    override fun shutdown() {}\n}\n\nfun imageLoader() {\n//sampleStart\n    application(modules = listOf(ImageModule)) {\n        ImageLoaderApp(display = instance(), images = instance())\n    }\n//sampleEnd\n}",u='package io.nacular.doodle.docs.apps\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.ImageModule\nimport io.nacular.doodle.application.Modules.Companion.PointerModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.center\nimport io.nacular.doodle.core.height\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.core.width\nimport io.nacular.doodle.docs.utils.controlBackgroundColor\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.geometry.Rectangle\nimport io.nacular.doodle.geometry.Size\nimport io.nacular.doodle.geometry.centered\nimport io.nacular.doodle.image.ImageLoader\nimport io.nacular.doodle.utils.Resizer\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.SupervisorJob\nimport kotlinx.coroutines.launch\nimport org.kodein.di.instance\nimport org.w3c.dom.HTMLElement\nimport kotlin.math.min\n\nclass ImageApp(\n    display    : Display,\n    imageLoader: ImageLoader,\n    appScope   : CoroutineScope\n): Application {\n    init {\n        appScope.launch {\n//sampleStart\n            val image = imageLoader.load("/doodle/images/photo.jpg") ?: return@launch\n\n            display.children += view {\n                render = {\n                    image(image, destination = bounds.atOrigin)\n                }\n\n                Resizer(this)\n            }\n//sampleEnd\n\n            display.fill(controlBackgroundColor.paint)\n\n            when {\n                display.size.empty -> display.sizeChanged += { _,_,_ -> setInitialBounds(display, image.size) }\n                else               -> setInitialBounds(display, image.size)\n            }\n        }\n    }\n\n    private fun setInitialBounds(display: Display, imageSize: Size) {\n        with(display.first()) {\n            if (size.empty && !display.size.empty) {\n                var w = min(display.width / 2, display.height - 40)\n                var h = w * imageSize.height / imageSize.width\n\n                if (h > display.height - 40) {\n                    h = display.height - 40\n                    w = h * imageSize.width / imageSize.height\n                }\n\n                display.first().bounds = Rectangle(w, h).centered(display.center)\n            }\n        }\n    }\n\n    override fun shutdown() {}\n\n    companion object {\n        private val appModules = listOf(PointerModule, ImageModule)\n\n        operator fun invoke(root: HTMLElement) = application(root, modules = appModules) {\n            ImageApp(instance(), instance(), CoroutineScope(SupervisorJob() + Dispatchers.Default))\n        }\n    }\n}',g='package io.nacular.doodle.docs.apps\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.ImageModule\nimport io.nacular.doodle.application.Modules.Companion.PointerModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.center\nimport io.nacular.doodle.core.height\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.core.width\nimport io.nacular.doodle.docs.utils.controlBackgroundColor\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.geometry.Rectangle\nimport io.nacular.doodle.geometry.centered\nimport io.nacular.doodle.geometry.div\nimport io.nacular.doodle.image.ImageLoader\nimport io.nacular.doodle.utils.Resizer\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.SupervisorJob\nimport kotlinx.coroutines.launch\nimport org.kodein.di.instance\nimport org.w3c.dom.HTMLElement\nimport kotlin.math.min\n\nclass CroppedImageApp(\n    display    : Display,\n    imageLoader: ImageLoader,\n    appScope   : CoroutineScope\n): Application {\n    init {\n        appScope.launch {\n//sampleStart\n            imageLoader.load("/doodle/images/photo.jpg")?.let { image ->\n                display.children += view {\n                    render = {\n                        image(image, source = Rectangle(image.size / 2), destination = bounds.atOrigin)\n                    }\n\n                    Resizer(this)\n                }\n            }\n//sampleEnd\n\n            display.fill(controlBackgroundColor.paint)\n\n            when {\n                display.size.empty -> display.sizeChanged += { _,_,_ -> setInitialBounds(display) }\n                else               -> setInitialBounds(display)\n            }\n        }\n    }\n\n    private fun setInitialBounds(display: Display) {\n        with(display.first()) {\n            if (size.empty && !display.size.empty) {\n                display.first().bounds = Rectangle(min(display.width / 2, display.height - 80)).centered(display.center)\n            }\n        }\n    }\n\n    override fun shutdown() {}\n\n    companion object {\n        private val appModules = listOf(PointerModule, ImageModule)\n\n        operator fun invoke(root: HTMLElement) = application(root, modules = appModules) {\n            CroppedImageApp(instance(), instance(), CoroutineScope(SupervisorJob() + Dispatchers.Default))\n        }\n    }\n}',h='package io.nacular.doodle.docs.apps\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.ImageModule\nimport io.nacular.doodle.application.Modules.Companion.PointerModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.center\nimport io.nacular.doodle.core.height\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.core.width\nimport io.nacular.doodle.docs.utils.controlBackgroundColor\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.opacity\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.doodle.geometry.Rectangle\nimport io.nacular.doodle.geometry.centered\nimport io.nacular.doodle.geometry.div\nimport io.nacular.doodle.image.ImageLoader\nimport io.nacular.doodle.image.height\nimport io.nacular.doodle.image.width\nimport io.nacular.doodle.utils.Resizer\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.SupervisorJob\nimport kotlinx.coroutines.launch\nimport org.kodein.di.instance\nimport org.w3c.dom.HTMLElement\nimport kotlin.math.min\n\nclass AspectCroppedImageApp(\n    display    : Display,\n    imageLoader: ImageLoader,\n    appScope   : CoroutineScope\n): Application {\n    init {\n        appScope.launch {\n//sampleStart\n            imageLoader.load("/doodle/images/photo.jpg")?.let { image ->\n                display.children += view {\n                    val aspect = image.width / image.height\n\n                    render = {\n                        var h = height\n                        var w = h * aspect\n\n                        if (w > width) {\n                            w = width\n                            h = w / aspect\n                        }\n\n                        rect(bounds.atOrigin, fill = (White opacity 0.75f).paint)\n\n                        image(\n                            image       = image,\n                            source      = Rectangle(image.size / 2),\n                            destination = Rectangle(w, h).centered(Point(width / 2, height / 2))\n                        )\n                    }\n\n                    Resizer(this)\n                }\n            }\n//sampleEnd\n\n            display.fill(controlBackgroundColor.paint)\n\n            when {\n                display.size.empty -> display.sizeChanged += { _,_,_ -> setInitialBounds(display) }\n                else               -> setInitialBounds(display)\n            }\n        }\n    }\n\n    private fun setInitialBounds(display: Display) {\n        with(display.first()) {\n            if (size.empty && !display.size.empty) {\n                display.first().bounds = Rectangle(min(display.width / 2, display.height - 40)).centered(display.center)\n            }\n        }\n    }\n\n    override fun shutdown() {}\n\n    companion object {\n        private val appModules = listOf(PointerModule, ImageModule)\n\n        operator fun invoke(root: HTMLElement) = application(root, modules = appModules) {\n            AspectCroppedImageApp(instance(), instance(), CoroutineScope(SupervisorJob() + Dispatchers.Default))\n        }\n    }\n}',y={title:"Images",hide_title:!0},f=void 0,x={id:"rendering/images",title:"Images",description:"Images",source:"@site/docs/rendering/images.mdx",sourceDirName:"rendering",slug:"/rendering/images",permalink:"/doodle/docs/rendering/images",draft:!1,unlisted:!1,tags:[],version:"current",frontMatter:{title:"Images",hide_title:!0},sidebar:"tutorialSidebar",previous:{title:"Text",permalink:"/doodle/docs/rendering/text"},next:{title:"Colors",permalink:"/doodle/docs/rendering/colors"}},w={},v=[{value:"Images",id:"images",level:2},...p.SI,{value:"Loading from resource",id:"loading-from-resource",level:2},{value:"Rendering",id:"rendering",level:2}];function I(e){const o={a:"a",admonition:"admonition",code:"code",h2:"h2",p:"p",...(0,a.M)(),...e.components};return s.m||k("api",!1),s.m.Canvas||k("api.Canvas",!0),s.m.Image||k("api.Image",!0),s.m.ImageLoader||k("api.ImageLoader",!0),s.m.ImageModule||k("api.ImageModule",!0),(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(o.h2,{id:"images",children:"Images"}),"\n",(0,i.jsxs)(o.p,{children:["Images are rendered directly to the ",(0,i.jsx)(o.code,{children:"Canvas"})," as primitives just like text and shapes. This means transformations and other rendering capabilities apply to images as well. You load ",(0,i.jsx)(s.m.Image,{}),"s into your app using the ",(0,i.jsx)(s.m.ImageLoader,{}),". This interface provides an async API for fetching images from different sources."]}),"\n",(0,i.jsx)(p.cp,{link:(0,i.jsx)(s.m.ImageModule,{}),module:m}),"\n",(0,i.jsx)(o.h2,{id:"loading-from-resource",children:"Loading from resource"}),"\n",(0,i.jsxs)(o.p,{children:[(0,i.jsx)(s.m.ImageLoader,{})," provides APIs for loading images from various sources, like urls, file-paths, and ",(0,i.jsx)(o.code,{children:"LocalFiles"})," that are obtained during ",(0,i.jsx)(o.a,{href:"/docs/dragdrop",children:"drag-drop"})," or via a ",(0,i.jsx)(o.a,{href:"/docs/ui_components/overview#fileselector",children:"FileSelector"}),"."]}),"\n",(0,i.jsxs)(o.p,{children:["The following examples shows how loading works. Notice that ",(0,i.jsx)(o.code,{children:"ImageLoader.load"})," returns ",(0,i.jsx)(o.code,{children:"Image?"}),", which is ",(0,i.jsx)(o.code,{children:"null"})," when the image fails to load for some reason. Fetching is also async, so it must be done from a ",(0,i.jsx)(o.code,{children:"suspend"})," method or a ",(0,i.jsx)(o.code,{children:"CoroutineScope"}),"."]}),"\n",(0,i.jsx)(d.A,{children:c}),"\n",(0,i.jsx)(o.admonition,{type:"tip",children:(0,i.jsxs)(o.p,{children:["See here for an example of how you might handle ",(0,i.jsx)(o.a,{href:"text#handling-timeouts",children:"time-outs"}),"."]})}),"\n",(0,i.jsx)(o.h2,{id:"rendering",children:"Rendering"}),"\n",(0,i.jsxs)(o.p,{children:[(0,i.jsx)(s.m.Image,{}),"s are treated like primitive elements of the rendering pipeline. They are rendered directly to a ",(0,i.jsx)(s.m.Canvas,{})," like other shapes and text."]}),"\n",(0,i.jsxs)(o.p,{children:["You are able to define the rectangular region within an image that will be put onto the ",(0,i.jsx)(o.code,{children:"Canvas"}),", as well as where on the ",(0,i.jsx)(o.code,{children:"Canvas"})," that region will be placed. These two values allow you to zoom and scale images as you draw them."]}),"\n",(0,i.jsxs)(r.c,{children:[(0,i.jsxs)(t.c,{value:"Image",children:[(0,i.jsx)(l.u,{functionName:"images",height:"300"}),(0,i.jsx)(d.A,{children:u})]}),(0,i.jsxs)(t.c,{value:"Cropped",children:[(0,i.jsx)(l.u,{functionName:"croppedImage",height:"300"}),(0,i.jsx)(d.A,{children:g})]}),(0,i.jsxs)(t.c,{value:"Aspect Cropped",children:[(0,i.jsx)(l.u,{functionName:"aspectCroppedImage",height:"300"}),(0,i.jsx)(d.A,{children:h})]})]}),"\n",(0,i.jsx)(o.admonition,{type:"tip",children:(0,i.jsx)(o.p,{children:"You can also control the corner radius, and opacity of the image being drawn."})})]})}function j(e={}){const{wrapper:o}={...(0,a.M)(),...e.components};return o?(0,i.jsx)(o,{...e,children:(0,i.jsx)(I,{...e})}):I(e)}function k(e,o){throw new Error("Expected "+(o?"component":"object")+" `"+e+"` to be defined: you likely forgot to import, pass, or provide it.")}}}]);