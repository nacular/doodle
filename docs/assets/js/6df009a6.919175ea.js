"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[294],{9288:(e,n,i)=>{i.d(n,{cp:()=>l});var o=i(7624),r=i(2172),a=(i(1268),i(5388),i(5720));function t(e){const n={admonition:"admonition",p:"p",...(0,r.M)(),...e.components};return(0,o.jsxs)(n.admonition,{title:"Module Required",type:"info",children:[(0,o.jsxs)("p",{children:["You must include the ",e.link," in your application in order to use these features."]}),(0,o.jsx)(a.A,{children:e.module}),(0,o.jsx)(n.p,{children:"Doodle uses opt-in modules like this to improve bundle size."})]})}function l(e={}){const{wrapper:n}={...(0,r.M)(),...e.components};return n?(0,o.jsx)(n,{...e,children:(0,o.jsx)(t,{...e})}):t(e)}},2056:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>D,contentTitle:()=>G,default:()=>z,frontMatter:()=>E,metadata:()=>M,toc:()=>O});var o=i(7624),r=i(2172),a=i(1268),t=i(5388),l=i(7996),s=i(5720),d=i(3148),c=i(9288);const p='package rendering\n\nimport io.nacular.doodle.core.View\nimport io.nacular.doodle.drawing.Canvas\nimport io.nacular.doodle.drawing.Color.Companion.Black\nimport io.nacular.doodle.drawing.text\n\n//sampleStart\nclass MyView: View() {\n    override fun render(canvas: Canvas) {\n        canvas.flipHorizontally(around = width / 2) {\n            text("hello", color = Black)\n        }\n    }\n}\n//sampleEnd',h="import io.nacular.doodle.core.View\nimport io.nacular.doodle.drawing.Canvas\nimport io.nacular.doodle.drawing.Color.Companion.Blue\nimport io.nacular.doodle.drawing.paint\n\n//sampleStart\nclass RectView: View() {\n    override fun render(canvas: Canvas) {\n        canvas.rect(bounds.atOrigin, Blue.paint)\n    }\n}\n//sampleEnd",u='package timerdsl\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Color.Companion.Black\nimport io.nacular.doodle.drawing.Color.Companion.Green\nimport io.nacular.doodle.drawing.Color.Companion.Red\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.drawing.rect\nimport io.nacular.doodle.drawing.text\nimport io.nacular.doodle.geometry.Size\nimport io.nacular.doodle.scheduler.Scheduler\nimport io.nacular.doodle.time.Clock\nimport io.nacular.measured.units.Time.Companion.milliseconds\nimport io.nacular.measured.units.times\n\n//sampleStart\nclass Timer(display: Display, clock: Clock, scheduler: Scheduler): Application {\n    init {\n        display += view {\n            size = Size(200)\n\n            scheduler.every(1 * milliseconds) {\n                rerender()\n            }\n\n            render = {\n                rect(bounds.atOrigin, Stroke(Red))\n                text("${clock.epoch}", color = Black)\n                rect(bounds.at(y = 20.0), color = Green)\n            }\n        }\n    }\n\n    override fun shutdown() {}\n}\n//sampleEnd',m='package timerrenderprop\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.View\nimport io.nacular.doodle.core.renderProperty\nimport io.nacular.doodle.drawing.Canvas\nimport io.nacular.doodle.drawing.Color.Companion.Black\nimport io.nacular.doodle.drawing.Color.Companion.Green\nimport io.nacular.doodle.drawing.Color.Companion.Red\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.drawing.rect\nimport io.nacular.doodle.drawing.text\nimport io.nacular.doodle.geometry.Size\nimport io.nacular.doodle.scheduler.Scheduler\nimport io.nacular.doodle.time.Clock\nimport io.nacular.measured.units.Time.Companion.milliseconds\nimport io.nacular.measured.units.times\n\n//sampleStart\nclass Timer(display: Display, clock: Clock, scheduler: Scheduler): Application {\n    init {\n        display += object: View() {\n            var time by renderProperty(clock.epoch)\n\n            init {\n                size = Size(200)\n\n                scheduler.every(1 * milliseconds) {\n                    time = clock.epoch\n                }\n            }\n\n            override fun render(canvas: Canvas) {\n                canvas.rect(bounds.atOrigin, Stroke(Red))\n                canvas.text("$time", color = Black)\n                canvas.rect(bounds.at(y = 20.0), color = Green)\n            }\n        }\n    }\n\n    override fun shutdown() {}\n}\n//sampleEnd',g="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Color\nimport io.nacular.doodle.drawing.paint\n\nfun colorPaint(color: Color) {\n//sampleStart\n    view {\n        render = {\n            rect(bounds.atOrigin, color.paint)\n        }\n    }\n//sampleEnd\n}",w="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Color\nimport io.nacular.doodle.drawing.GradientPaint.Stop\nimport io.nacular.doodle.drawing.LinearGradientPaint\nimport io.nacular.doodle.geometry.Point\n\n/**\n * Example showing how to use [LinearGradientPaint]s.\n */\nfun linearPaint(color1: Color, color2: Color, point1: Point, point2: Point) {\n//sampleStart\n    view {\n        render = {\n            // Simple version with 2 colors\n            rect(bounds.atOrigin, LinearGradientPaint(\n                color1,\n                color2,\n                point1,\n                point2\n            ))\n        }\n    }\n\n    view {\n        render = {\n            // Also able to use a list of color stops\n            rect(bounds.atOrigin, LinearGradientPaint(\n                listOf(\n                    Stop(color1, 0f  ),\n                    Stop(color1, 1f/3),\n                    // ...\n                ),\n                point1,\n                point2\n            ))\n        }\n    }\n//sampleEnd\n}",x="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Color\nimport io.nacular.doodle.drawing.GradientPaint.Stop\nimport io.nacular.doodle.drawing.RadialGradientPaint\nimport io.nacular.doodle.geometry.Circle\n\n/**\n * Example showing how to use [RadialGradientPaint]s.\n */\nfun radialPaint(color1: Color, color2: Color, circle1: Circle, circle2: Circle) {\n//sampleStart\n    view {\n        render = {\n            // Simple version with 2 colors\n            rect(bounds.atOrigin, RadialGradientPaint(\n                color1,\n                color2,\n                circle1,\n                circle2\n            ))\n        }\n    }\n\n    view {\n        render = {\n            // Also able to use a list of color stops\n            rect(bounds.atOrigin, RadialGradientPaint(\n                listOf(\n                    Stop(color1, 0f  ),\n                    Stop(color1, 1f/3),\n                    // ...\n                ),\n                circle1,\n                circle2\n            ))\n        }\n    }\n//sampleEnd\n}";var f=i(6068);const v="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.AffineTransform2D\nimport io.nacular.doodle.drawing.PatternPaint\nimport io.nacular.doodle.geometry.Size\n\nfun patternPaint(size: Size, transform: AffineTransform2D) {\n//sampleStart\n    view {\n        render = {\n            rect(bounds.atOrigin, PatternPaint(size, transform) {\n                // render onto canvas\n                // rect(..)\n            })\n        }\n    }\n//sampleEnd\n}",j="package rendering\n\nimport io.nacular.doodle.core.View\nimport io.nacular.doodle.core.renderProperty\nimport io.nacular.doodle.drawing.AffineTransform.Companion.Identity\nimport io.nacular.doodle.drawing.Canvas\nimport io.nacular.doodle.drawing.Color.Companion.Red\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.stripedPaint\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.measured.units.Angle.Companion.degrees\nimport io.nacular.measured.units.times\n\n// Doodle's implementation of striped-paint looks something like this\n/*\nfun stripedPaint(\n    stripeWidth : Double,\n    evenRowColor: Color? = null,\n    oddRowColor : Color? = null,\n    transform   : AffineTransform2D = Identity\n): PatternPaint = PatternPaint(Size(if (evenRowColor.visible || oddRowColor.visible) stripeWidth else 0.0, 2 * stripeWidth), transform) {\n    evenRowColor?.let { rect(Rectangle(                  stripeWidth, stripeWidth), ColorPaint(it)) }\n    oddRowColor?.let  { rect(Rectangle(0.0, stripeWidth, stripeWidth, stripeWidth), ColorPaint(it)) }\n}\n*/\n\n//sampleStart\nclass SomeView: View() {\n    private val stripeWidth = 20.0\n    private var paintAngle by renderProperty(0 * degrees)\n\n    override fun render(canvas: Canvas) {\n        val paintCenter = Point(canvas.size.width / 2, canvas.size.height / 2)\n\n        canvas.rect(bounds.atOrigin, stripedPaint(\n            stripeWidth  = stripeWidth,\n            evenRowColor = Red,\n            oddRowColor  = White,\n            transform    = Identity.rotate(around = paintCenter, by = paintAngle)\n        ))\n    }\n}\n//sampleEnd",P="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Paint\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.drawing.Stroke.LineCap.Round\nimport io.nacular.doodle.drawing.Stroke.LineJoint.Miter\n\nfun stroke(paint: Paint) {\n//sampleStart\n    view {\n        render = {\n            rect(bounds.atOrigin, stroke = Stroke(\n                fill      = paint,\n                dashes    = doubleArrayOf(10.0, 20.0),\n                thickness = 2.5,\n                lineCap   = Round,\n                lineJoint = Miter\n            ))\n        }\n    }\n//sampleEnd\n}",y="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Paint\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.geometry.Rectangle\n\nfun rect(rectangle: Rectangle, paint: Paint, stroke: Stroke) {\n//sampleStart\n    view {\n        render = {\n            rect(rectangle, paint, stroke = stroke)\n        }\n    }\n//sampleEnd\n}",C="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Paint\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.geometry.Circle\nimport io.nacular.doodle.geometry.Polygon\nimport io.nacular.doodle.geometry.inscribed\nimport io.nacular.measured.units.Angle.Companion.degrees\nimport io.nacular.measured.units.times\n\nfun poly(polygon: Polygon, circle: Circle, paint: Paint, stroke: Stroke) {\n    // You can also create equilateral polygons by inscribing them\n    // within circles.\n    circle.inscribed(8, rotation = 45 * degrees)\n\n//sampleStart\n    view {\n        render = {\n            poly(polygon, fill = paint, stroke = stroke)\n        }\n    }\n//sampleEnd\n}",k="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Paint\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.geometry.Circle\nimport io.nacular.doodle.geometry.Rectangle\nimport io.nacular.doodle.geometry.inscribedCircle\n\nfun circle(circle: Circle, rectangle: Rectangle, paint: Paint, stroke: Stroke) {\n    // You can also create circles based on Rectangles\n    rectangle.inscribedCircle()\n\n//sampleStart\n    view {\n        render = {\n            circle(circle, fill = paint, stroke = stroke)\n        }\n    }\n//sampleEnd\n}",b="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Paint\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.geometry.Ellipse\nimport io.nacular.doodle.geometry.Rectangle\nimport io.nacular.doodle.geometry.inscribedEllipse\n\nfun ellipse(ellipse: Ellipse, rectangle: Rectangle, paint: Paint, stroke: Stroke) {\n    // You can also create ellipses based on Rectangles\n    rectangle.inscribedEllipse()\n\n//sampleStart\n    view {\n        render = {\n            ellipse(ellipse, fill = paint, stroke = stroke)\n        }\n    }\n//sampleEnd\n}",S="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Paint\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.measured.units.Angle.Companion.degrees\nimport io.nacular.measured.units.Angle.Companion.radians\nimport io.nacular.measured.units.times\nimport kotlin.math.PI\n\nfun arc(center: Point,  paint: Paint, stroke: Stroke) {\n//sampleStart\n    view {\n        render = {\n            arc(\n                center   = center,\n                radius   = 100.0,\n                fill     = paint,\n                stroke   = stroke,\n                sweep    = 270 * degrees,\n                rotation = PI * radians\n            )\n        }\n    }\n//sampleEnd\n}",A="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Paint\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.measured.units.Angle.Companion.degrees\nimport io.nacular.measured.units.Angle.Companion.radians\nimport io.nacular.measured.units.times\nimport kotlin.math.PI\n\nfun wedge(center: Point,  paint: Paint, stroke: Stroke) {\n//sampleStart\n    view {\n        render = {\n            wedge(\n                center   = center,\n                radius   = 100.0,\n                fill     = paint,\n                stroke   = stroke,\n                sweep    = 270 * degrees,\n                rotation = PI / 2 * radians\n            )\n        }\n    }\n//sampleEnd\n}",R='package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Paint\nimport io.nacular.doodle.drawing.Stroke\nimport io.nacular.doodle.geometry.Circle\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.doodle.geometry.inscribed\nimport io.nacular.doodle.geometry.path\nimport io.nacular.doodle.geometry.rounded\n\nfun path(paint: Paint, stroke: Stroke) {\n    // Path from SVG data\n    val heart = path("M50,25 C35,0,-14,25,20,60 L50,90 L80,60 C114,20,65,0,50,25")!!\n\n    // Start with rounded corners\n    val star = Circle(\n        radius = 100.0,\n        center = Point(100, 100)\n    ).inscribed(5)!!.rounded(radius = 10.0)\n\n//sampleStart\n    view {\n        render = {\n            // transform the Canvas to change path bounds, etc.\n            path(star, fill = paint, stroke = stroke)\n        }\n    }\n//sampleEnd\n}',V='package rendering\n\nimport io.nacular.doodle.geometry.Circle\nimport io.nacular.doodle.geometry.Path\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.doodle.geometry.Rectangle\nimport io.nacular.doodle.geometry.map\nimport io.nacular.doodle.geometry.path\nimport io.nacular.doodle.geometry.toPath\n\nfun pathCreation(start: Point, end: Point, width: Double, height: Double) {\n//sampleStart\n    // Just convert most geometric shapes into them:\n\n    Circle().toPath()\n    Rectangle().toPath()\n    Rectangle().map { it * 2 }.toPath()\n\n    // Use the path builder function:\n\n    path(start)\n        .cubicTo(\n            Point(width / 2, height / 2),\n            Point(width / 3,   0),\n            Point(width / 2, -10)\n        )\n        .cubicTo(\n            end,\n            Point(width / 2, height + 10),\n            Point(width * 2/3, height)\n        ).finish() // or close\n\n    // Or use a raw String:\n\n    val path: Path? = path("...") // returns nullable since given string might be invalid\n    val imSure      = path("...")!!\n//sampleEnd\n}',T="package rendering\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.PathModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.geometry.PathMetrics\nimport org.kodein.di.instance\n\nclass PathApp(display: Display, pathMetrics: PathMetrics): Application {\n    override fun shutdown() {}\n}\n\nfun pathModule() {\n//sampleStart\n    application(modules = listOf(PathModule)) {\n        PathApp(display = instance(), pathMetrics = instance())\n    }\n//sampleEnd\n}",E={hide_title:!0,title:"Overview"},G=void 0,M={id:"rendering/overview",title:"Overview",description:"Rendering",source:"@site/docs/rendering/overview.mdx",sourceDirName:"rendering",slug:"/rendering/overview",permalink:"/doodle/docs/rendering/overview",draft:!1,unlisted:!1,tags:[],version:"current",frontMatter:{hide_title:!0,title:"Overview"},sidebar:"tutorialSidebar",previous:{title:"Displays",permalink:"/doodle/docs/display"},next:{title:"Text",permalink:"/doodle/docs/rendering/text"}},D={},O=[{value:"Rendering",id:"rendering",level:2},{value:"Efficient rendering",id:"efficient-rendering",level:2},{value:"The Canvas",id:"the-canvas",level:2},{value:"Filling regions with Paints",id:"filling-regions-with-paints",level:3},{value:"Colors",id:"colors",level:3},{value:"Gradients",id:"gradients",level:3},{value:"Patterns",id:"patterns",level:3},{value:"Outlining with Strokes",id:"outlining-with-strokes",level:3},{value:"Basic shapes",id:"basic-shapes",level:3},{value:"Paths",id:"paths",level:3},{value:"Transformations",id:"transformations",level:3}];function I(e){const n={a:"a",admonition:"admonition",code:"code",h2:"h2",h3:"h3",p:"p",pre:"pre",...(0,r.M)(),...e.components};return d||N("api",!1),d.AffineTransform||N("api.AffineTransform",!0),d.AffineTransform2D||N("api.AffineTransform2D",!0),d.Blue||N("api.Blue",!0),d.Canvas||N("api.Canvas",!0),d.Circle||N("api.Circle",!0),d.Color||N("api.Color",!0),d.ColorPaint||N("api.ColorPaint",!0),d.Color_Paint||N("api.Color_Paint",!0),d.EllipseInscribed||N("api.EllipseInscribed",!0),d.ImagePaint||N("api.ImagePaint",!0),d.InscribedCircle||N("api.InscribedCircle",!0),d.InscribedEllipse||N("api.InscribedEllipse",!0),d.LinearGradientPaint||N("api.LinearGradientPaint",!0),d.Paint||N("api.Paint",!0),d.PathMetrics||N("api.PathMetrics",!0),d.PathModule||N("api.PathModule",!0),d.PatternPaint||N("api.PatternPaint",!0),d.RadialGradientPaint||N("api.RadialGradientPaint",!0),d.Rectangle||N("api.Rectangle",!0),d.StripedPaint||N("api.StripedPaint",!0),d.Stroke||N("api.Stroke",!0),d.SweepGradientPaint||N("api.SweepGradientPaint",!0),d.View||N("api.View",!0),d.ViewClipCanvasToBounds||N("api.ViewClipCanvasToBounds",!0),d.ViewReRender||N("api.ViewReRender",!0),d.ViewRender||N("api.ViewRender",!0),d.ViewSize||N("api.ViewSize",!0),d.ViewVisible||N("api.ViewVisible",!0),(0,o.jsxs)(o.Fragment,{children:[(0,o.jsx)(n.h2,{id:"rendering",children:"Rendering"}),"\n",(0,o.jsxs)(n.p,{children:["Doodle automatically manages rendering of ",(0,o.jsx)(n.code,{children:"Views"}),", and this covers almost all use-cases. Each ",(0,o.jsx)(d.View,{})," draws its content to a ",(0,o.jsx)(n.a,{href:"#the-canvas",children:"Canvas"})," provided during calls to the ",(0,o.jsx)(d.ViewRender,{})," method. This either presents the View's contents on the screen for the first time or updates them on subsequent calls."]}),"\n",(0,o.jsxs)(n.p,{children:["Doodle calls ",(0,o.jsx)(n.code,{children:"render"})," whenever a ",(0,o.jsx)(n.code,{children:"View"})," needs a visual update, which includes changes to its ",(0,o.jsx)(d.ViewSize,{})," or it becoming ",(0,o.jsx)(d.ViewVisible,{}),". This means you do not need to call ",(0,o.jsx)(n.code,{children:"render"})," yourself. But there might be cases when you want to request that a View re-renders. You can do this by calling ",(0,o.jsx)(d.ViewReRender,{}),"."]}),"\n",(0,o.jsxs)(n.p,{children:["This is an example of a simple ",(0,o.jsx)(n.code,{children:"View"})," that draws a rectangle filled with ",(0,o.jsx)(d.Blue,{})," ",(0,o.jsx)(d.ColorPaint,{})," that covers its bounds."]}),"\n",(0,o.jsx)(s.A,{children:h}),"\n",(0,o.jsx)(n.admonition,{type:"tip",children:(0,o.jsxs)(n.p,{children:[(0,o.jsx)(d.ViewRender,{})," is automatically called on ",(0,o.jsx)(d.ViewSize,{})," changes and ",(0,o.jsx)(d.ViewVisible,{})," changing to ",(0,o.jsx)(n.code,{children:"true"})]})}),"\n",(0,o.jsx)(n.h2,{id:"efficient-rendering",children:"Efficient rendering"}),"\n",(0,o.jsxs)(n.p,{children:["Doodle optimizes rendering to avoid re-applying operations when drawing the same content repeatedly. For example, the ",(0,o.jsx)(n.code,{children:"Timer"})," app below renders the epoch time every millisecond. However, Doodle only updates the changing regions (i.e. the DOM for Web apps), which is the text in this case. This reduces the amount of thrash in the DOM for Web apps."]}),"\n",(0,o.jsx)(n.admonition,{type:"info",children:(0,o.jsxs)(n.p,{children:["Doodle uses ",(0,o.jsx)(n.a,{href:"https://nacular.github.io/measured/",children:"Measured"})," for time, angles etc."]})}),"\n",(0,o.jsxs)(a.c,{children:[(0,o.jsx)(t.c,{value:"Demo",children:(0,o.jsx)(l.u,{functionName:"timer",height:"200"})}),(0,o.jsx)(t.c,{value:"View DSL",children:(0,o.jsx)(s.A,{children:u})}),(0,o.jsx)(t.c,{value:"Render Property",children:(0,o.jsx)(s.A,{children:m})})]}),"\n",(0,o.jsx)(n.h2,{id:"the-canvas",children:"The Canvas"}),"\n",(0,o.jsxs)(n.p,{children:["All drawing is done via the ",(0,o.jsx)(d.Canvas,{})," API, which offers a rich set of operations for geometric shapes, paths, images, and text. It also supports different ",(0,o.jsx)(d.Paint,{})," types (i.e. ",(0,o.jsx)(d.ColorPaint,{}),", ",(0,o.jsx)(d.LinearGradientPaint,{}),", ",(0,o.jsx)(n.a,{href:"/doodle/docs/rendering/overview#patterns",children:"PatternPaint"}),", etc.) for filling regions."]}),"\n",(0,o.jsxs)(n.p,{children:["The ",(0,o.jsx)(n.code,{children:"Canvas"})," provided to a ",(0,o.jsx)(n.code,{children:"View"}),"'s ",(0,o.jsx)(d.ViewRender,{})," method has a coordinate system that anchors ",(0,o.jsx)(n.code,{children:"0,0"})," on the ",(0,o.jsx)(n.code,{children:"Canvas"})," to the ",(0,o.jsx)(n.code,{children:"View"}),"'s origin. The Canvas itself extends in all directions beyond the bounds of the View; but the contents drawn to it will be clipped to the view's bounds by default."]}),"\n",(0,o.jsx)(n.admonition,{type:"tip",children:(0,o.jsxs)(n.p,{children:["Sub-classes can disable clipping by setting ",(0,o.jsx)(d.ViewClipCanvasToBounds,{})," = ",(0,o.jsx)(n.code,{children:"false"}),"."]})}),"\n",(0,o.jsx)(n.h3,{id:"filling-regions-with-paints",children:"Filling regions with Paints"}),"\n",(0,o.jsxs)(n.p,{children:["Most Views require geometric regions on their Canvas to be filled with some color, gradient, texture, etc.. Therefore, many Canvas APIs take a ",(0,o.jsx)(d.Paint,{})," parameter that determines how the inner region such shapes are filled. There are several built-in ",(0,o.jsx)(n.code,{children:"Paint"})," types to choose from."]}),"\n",(0,o.jsx)(n.h3,{id:"colors",children:"Colors"}),"\n",(0,o.jsxs)(n.p,{children:[(0,o.jsx)(d.ColorPaint,{})," is the simplest and most common way to fill a region. You can create one using the constructor, or via the ",(0,o.jsx)(d.Color_Paint,{})," extension on any ",(0,o.jsx)(d.Color,{}),"."]}),"\n",(0,o.jsx)(l.u,{functionName:"colorPaint",height:"300"}),"\n",(0,o.jsx)(s.A,{children:g}),"\n",(0,o.jsx)(n.h3,{id:"gradients",children:"Gradients"}),"\n",(0,o.jsxs)(n.p,{children:["You can fill regions with ",(0,o.jsx)(d.LinearGradientPaint,{}),"s, ",(0,o.jsx)(d.RadialGradientPaint,{}),"s and ",(0,o.jsx)(d.SweepGradientPaint,{}),"s paints as well. These take a series of ",(0,o.jsx)(d.Color,{}),"s and stop locations that are turned into a smoothly transitioning gradient across a shape."]}),"\n",(0,o.jsxs)(a.c,{children:[(0,o.jsxs)(t.c,{value:"Linear",children:[(0,o.jsx)(l.u,{functionName:"linearGradientPaint",height:"300"}),(0,o.jsx)(s.A,{children:w})]}),(0,o.jsxs)(t.c,{value:"Radial",children:[(0,o.jsx)(l.u,{functionName:"radialGradientPaint",height:"300"}),(0,o.jsx)(s.A,{children:x})]}),(0,o.jsxs)(t.c,{value:"Sweep",children:[(0,o.jsx)(l.u,{functionName:"sweepGradientPaint",height:"300"}),(0,o.jsx)(s.A,{children:f.c})]})]}),"\n",(0,o.jsx)(n.h3,{id:"patterns",children:"Patterns"}),"\n",(0,o.jsxs)(n.p,{children:["Sometimes you need to fill a region with a repeating pattern, like an image or some geometric shapes. This is easy to achieve with ",(0,o.jsx)(d.ImagePaint,{})," (when an image is all you need) or ",(0,o.jsx)(d.PatternPaint,{}),", when more sophisticated patterns are needed."]}),"\n",(0,o.jsxs)(n.p,{children:[(0,o.jsx)(n.code,{children:"PatternPaint"}),' has a "render" body that provides a powerful and familiar way of creating repeating patterns. You create one by specifying a ',(0,o.jsx)(n.code,{children:"size"})," and a ",(0,o.jsx)(n.code,{children:"paint"})," lambda, which has access to the full ",(0,o.jsx)(d.Canvas,{})," APIs."]}),"\n",(0,o.jsxs)(n.p,{children:["This app uses ",(0,o.jsx)(d.StripedPaint,{})," to show how a ",(0,o.jsx)(d.PatternPaint,{})," can be transformed using ",(0,o.jsx)(d.AffineTransform2D,{}),", like rotated around its center for example."]}),"\n",(0,o.jsxs)(a.c,{children:[(0,o.jsx)(t.c,{value:"Demo",children:(0,o.jsx)(l.u,{functionName:"patternPaint",height:"300"})}),(0,o.jsx)(t.c,{value:"Code",children:(0,o.jsx)(s.A,{children:j})})]}),"\n",(0,o.jsx)(s.A,{children:v}),"\n",(0,o.jsx)(n.h3,{id:"outlining-with-strokes",children:"Outlining with Strokes"}),"\n",(0,o.jsxs)(n.p,{children:["Regions and curves can be stroked to give them an outline of some sort. The Canvas APIs also take ",(0,o.jsx)(d.Stroke,{})," parameters that determine how to outline the shape being rendered."]}),"\n",(0,o.jsxs)(n.p,{children:["A ",(0,o.jsx)(n.code,{children:"Stroke"})," combines the behavior of a ",(0,o.jsx)(n.a,{href:"overview#filling-regions-with-paints",children:"Paint"})," (but for the outline only) and adds several properties that govern how the outline is created."]}),"\n",(0,o.jsx)(n.admonition,{type:"info",children:(0,o.jsxs)(n.p,{children:["Strokes are centered along the edge of the shape the outline. This means adding a stroke will expand the area needed to render the shape by ",(0,o.jsx)(n.code,{children:"stroke.thickness / 2"})," in the x and y direction. Keep this in mind, and shrink shapes accordingly to ensure outlines are not clipped and they appear where you want them to."]})}),"\n",(0,o.jsx)(l.u,{functionName:"strokes",height:"300"}),"\n",(0,o.jsx)(s.A,{children:P}),"\n",(0,o.jsx)(n.h3,{id:"basic-shapes",children:"Basic shapes"}),"\n",(0,o.jsxs)(n.p,{children:["The ",(0,o.jsx)(d.Canvas,{})," has APIs for drawing many basic shapes that foundation of most Views. Each shape can be filled with ",(0,o.jsx)(d.Paint,{})," and outlined with ",(0,o.jsx)(d.Stroke,{}),"s as well."]}),"\n",(0,o.jsxs)(a.c,{children:[(0,o.jsxs)(t.c,{value:"Rect",children:[(0,o.jsx)(l.u,{functionName:"shapes",args:'["rect"]',height:"300"}),(0,o.jsx)(s.A,{children:y}),(0,o.jsx)(n.admonition,{type:"tip",children:(0,o.jsxs)(n.p,{children:["A View's bounds is relative to its parent, so you need to do bring it into the View's coordinate space before drawing it to the Canvas. This is easy with ",(0,o.jsx)(n.code,{children:"bounds.atOrigin"}),"."]})})]}),(0,o.jsxs)(t.c,{value:"Poly",children:[(0,o.jsx)(l.u,{functionName:"shapes",args:'["poly"]',height:"300"}),(0,o.jsx)(s.A,{children:C}),(0,o.jsxs)(n.admonition,{type:"tip",children:[(0,o.jsxs)(n.p,{children:["You can also create equilateral polygons using the ",(0,o.jsx)(d.EllipseInscribed,{})," function on a ",(0,o.jsx)(d.Circle,{}),":"]}),(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"circle.inscribed(8, rotation = 45 * degrees)\n"})})]})]}),(0,o.jsxs)(t.c,{value:"Circle",children:[(0,o.jsx)(l.u,{functionName:"shapes",args:'["circle"]',height:"300"}),(0,o.jsx)(s.A,{children:k}),(0,o.jsxs)(n.admonition,{type:"tip",children:[(0,o.jsxs)(n.p,{children:["Circles can be created from ",(0,o.jsx)(d.Rectangle,{}),"s, using the ",(0,o.jsx)(d.InscribedCircle,{})," method:"]}),(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"rectangle.inscribedCircle()\n"})})]})]}),(0,o.jsxs)(t.c,{value:"Ellipse",children:[(0,o.jsx)(l.u,{functionName:"shapes",args:'["ellipse"]',height:"300"}),(0,o.jsx)(s.A,{children:b}),(0,o.jsxs)(n.admonition,{type:"tip",children:[(0,o.jsxs)(n.p,{children:["Ellipses can be created from ",(0,o.jsx)(d.Rectangle,{}),"s, using the ",(0,o.jsx)(d.InscribedEllipse,{})," method:"]}),(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"rectangle.inscribedEllipse()\n"})})]})]}),(0,o.jsxs)(t.c,{value:"Arc",children:[(0,o.jsx)(l.u,{functionName:"shapes",args:'["arc"]',height:"300"}),(0,o.jsx)(s.A,{children:S})]}),(0,o.jsxs)(t.c,{value:"Wedge",children:[(0,o.jsx)(l.u,{functionName:"shapes",args:'["wedge"]',height:"300"}),(0,o.jsx)(s.A,{children:A})]})]}),"\n",(0,o.jsx)(n.h3,{id:"paths",children:"Paths"}),"\n",(0,o.jsx)(n.p,{children:"Paths a really powerful primitives that let you create arbitrarily complex shapes that can be filled and stroked like those above. This flexibility comes from the fact that paths are fairly opaque components that behave almost like images."}),"\n",(0,o.jsxs)(n.p,{children:["A major difference with Paths is that they do not track their intrinsic size (though this can be obtained using a ",(0,o.jsx)(d.PathMetrics,{}),"). This is a tradeoff that allows them to be created in a very light weight way. Paths can be transformed when rendered to a ",(0,o.jsx)(d.Canvas,{})," to adjust their size, position, etc."]}),"\n",(0,o.jsx)(l.u,{functionName:"shapes",args:'["path"]',height:"300"}),"\n",(0,o.jsx)(s.A,{children:R}),"\n",(0,o.jsx)(c.cp,{link:(0,o.jsx)(d.PathModule,{}),module:T}),"\n",(0,o.jsxs)(n.admonition,{type:"tip",children:[(0,o.jsx)(n.p,{children:"There are may ways to create Paths."}),(0,o.jsx)(s.A,{children:V})]}),"\n",(0,o.jsx)(n.h3,{id:"transformations",children:"Transformations"}),"\n",(0,o.jsxs)(n.p,{children:["The ",(0,o.jsx)(n.code,{children:"Canvas"})," can also be transformed using any ",(0,o.jsx)(d.AffineTransform,{}),". These are linear transformation which encapsulate translation, scaling, and rotation in matrices. You can combine these transforms into more complex ones directly, or apply them to a ",(0,o.jsx)(n.code,{children:"Canvas"})," in a nested fashion to get similar results"]}),"\n",(0,o.jsxs)(n.p,{children:["This ",(0,o.jsx)(n.code,{children:"View"})," flips the Canvas horizontally around its mid-point and draws some text."]}),"\n",(0,o.jsx)(s.A,{children:p})]})}function z(e={}){const{wrapper:n}={...(0,r.M)(),...e.components};return n?(0,o.jsx)(n,{...e,children:(0,o.jsx)(I,{...e})}):I(e)}function N(e,n){throw new Error("Expected "+(n?"component":"object")+" `"+e+"` to be defined: you likely forgot to import, pass, or provide it.")}},6068:(e,n,i)=>{i.d(n,{c:()=>o});const o="package rendering\n\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.drawing.Color\nimport io.nacular.doodle.drawing.GradientPaint.Stop\nimport io.nacular.doodle.drawing.SweepGradientPaint\nimport io.nacular.doodle.geometry.Point\nimport io.nacular.measured.units.Angle\nimport io.nacular.measured.units.Measure\n\n/**\n * Example showing how to use [SweepGradientPaint]s.\n */\nfun sweepGradientPaint(color1: Color, color2: Color, center: Point, rotation: Measure<Angle>) {\n//sampleStart\n    view {\n        render = {\n            // Simple version with 2 colors\n            rect(bounds.atOrigin, SweepGradientPaint(\n                color1,\n                color2,\n                center,\n                rotation\n            ))\n        }\n    }\n\n    view {\n        render = {\n            // Also able to use a list of color stops\n            rect(\n                bounds.atOrigin, SweepGradientPaint(\n                    listOf(\n                        Stop(color1, 0f),\n                        Stop(color1, 1f / 3),\n                        // ...\n                    ),\n                    center,\n                    rotation\n                )\n            )\n        }\n    }\n//sampleEnd\n}"}}]);