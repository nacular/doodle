"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[921],{7034:function(e,t,n){n.r(t),n.d(t,{assets:function(){return h},contentTitle:function(){return c},default:function(){return k},frontMatter:function(){return p},metadata:function(){return m},toc:function(){return v}});var i=n(3117),a=n(102),o=(n(7294),n(3905)),r=n(9877),s=n(8215),l=n(3138),d=["components"],p={hide_title:!0},c="Pointer Input Overview",m={unversionedId:"pointer_input/overview",id:"pointer_input/overview",title:"Pointer Input Overview",description:"Pointer handling is easy with Doodle. The first thing you need to do is include the PointerModule",source:"@site/docs/pointer_input/overview.mdx",sourceDirName:"pointer_input",slug:"/pointer_input/overview",permalink:"/doodle/docs/pointer_input/overview",tags:[],version:"current",frontMatter:{hide_title:!0},sidebar:"tutorialSidebar",previous:{title:"Behaviors",permalink:"/doodle/docs/rendering/behaviors"},next:{title:"Pointer Motion Events",permalink:"/doodle/docs/pointer_input/pointermotion"}},h={},v=[{value:"Hit Detection",id:"hit-detection",level:2},{value:"Support For Transforms",id:"support-for-transforms",level:3},{value:"Pointer Listeners",id:"pointer-listeners",level:2},{value:"Pointer Event",id:"pointer-event",level:2},{value:"Event Bubbling",id:"event-bubbling",level:2},{value:"Event Filtering",id:"event-filtering",level:2}],u={toc:v};function k(e){var t=e.components,n=(0,a.Z)(e,d);return(0,o.kt)("wrapper",(0,i.Z)({},u,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"pointer-input-overview"},"Pointer Input Overview"),(0,o.kt)("p",null,"Pointer handling is easy with Doodle. The first thing you need to do is include the ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/application/Modules.kt#L51"},(0,o.kt)("inlineCode",{parentName:"a"},"PointerModule")),"\nwhen launching your app. The underlying framework uses the ",(0,o.kt)("inlineCode",{parentName:"p"},"PointerModule")," to produce pointer events."),(0,o.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Doodle uses opt-in modules like this to improve bundle size."))),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"class MyApp(display: Display): Application {\n    // pointer events will fire for this app when launched with\n    // the PointerModule\n}\n")),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},'import io.nacular.doodle.application.Modules.Companion.PointerModule\n\nfun main () {\n    // "full screen" launch with pointer support\n    application(modules = listOf(PointerModule)) {\n        MyApp(display = instance())\n    }\n}\n')),(0,o.kt)("h2",{id:"hit-detection"},"Hit Detection"),(0,o.kt)("p",null,"The framework relies on the ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L450"},(0,o.kt)("inlineCode",{parentName:"a"},"View.contains(Point)")),"\nmethod to determine when the pointer is within a View's boundaries."),(0,o.kt)("p",null,"The default implementation just checks the point against ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L108"},(0,o.kt)("inlineCode",{parentName:"a"},"bounds")),".\nHowever, more complex hit detection can be used to customize pointer handling."),(0,o.kt)(r.Z,{mdxType:"Tabs"},(0,o.kt)(s.Z,{value:"demo",label:"Demo",mdxType:"TabItem"},(0,o.kt)(l.B,{functionName:"hitDetection",height:"250",mdxType:"DoodleCodeBlock"})),(0,o.kt)(s.Z,{value:"code",label:"Code",mdxType:"TabItem"},(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"import io.nacular.doodle.geometry.Circle\n\nclass CircularView(val radius: Double): View() {\n    private val circle = Circle(radius)\n\n    override fun intersects(point: Point) = point - position in circle\n\n    override fun render(canvas: Canvas) {\n        canvas.circle(circle, Red.paint)\n    }\n}\n")),(0,o.kt)("p",null,"This view renders a circle and provides precise hit detection for it."))),(0,o.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"The contains check passes a point within the View's parent coordinate system (or the ",(0,o.kt)("a",{parentName:"p",href:"/doodle/docs/display#the-display-is-an-apps-root-container"},(0,o.kt)("strong",{parentName:"a"},"Display")),"'s for\ntop-level Views)."))),(0,o.kt)("h3",{id:"support-for-transforms"},"Support For Transforms"),(0,o.kt)("p",null,"Doodle also accounts for transformations applied to the View's ancestors when delivering pointer events. This means the View will receive the right notification\nwhenever the pointer intersects its parent despite transformations. Hit detection logic in the View is then triggered as usual. The View still needs to take\nits own transformation into account though, since the given point used in hit detection is within the parent coordinate space. This is automatically handled if the\nView implements ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L824"},(0,o.kt)("inlineCode",{parentName:"a"},"intersects(Point)"))," or if\n",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L856"},(0,o.kt)("inlineCode",{parentName:"a"},"toLocal"))," is used when overriding ",(0,o.kt)("inlineCode",{parentName:"p"},"contains"),"."),(0,o.kt)(r.Z,{mdxType:"Tabs"},(0,o.kt)(s.Z,{value:"demo",label:"Demo",mdxType:"TabItem"},(0,o.kt)(l.B,{functionName:"hitDetectionTransform",height:"500",mdxType:"DoodleCodeBlock"})),(0,o.kt)(s.Z,{value:"code",label:"Code",mdxType:"TabItem"},(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"class TriangleView: View() {\n    private val outerPoly get() = ConvexPolygon(Point(width / 2, 0), Point(width, height), Point(0, height))\n    private val innerPoly get() = outerPoly.reversed().let {\n        Identity.scale(around = Point(width / 2, height * 2 / 3), x = 0.5, y = 0.5).invoke(it)\n    }\n\n    private var pointerOver by renderProperty(false)\n\n    init {\n        pointerChanged += entered { pointerOver = true  }\n        pointerChanged += exited  { pointerOver = false }\n    }\n\n    override fun intersects(point: Point) = (point - position).let {\n        it in outerPoly && it !in innerPoly\n    }\n\n    override fun render(canvas: Canvas) {\n        canvas.path(outerPoly.toPath() + innerPoly.toPath(), fill = if (pointerOver) Black.paint else White.paint)\n    }\n}\n")),(0,o.kt)("p",null,"This view renders a circle and provides precise hit detection for it."))),(0,o.kt)("h2",{id:"pointer-listeners"},"Pointer Listeners"),(0,o.kt)("p",null,"Views are able to receive pointer events once the ",(0,o.kt)("inlineCode",{parentName:"p"},"PointerModule")," is loaded, they are ",(0,o.kt)("inlineCode",{parentName:"p"},"visible")," and ",(0,o.kt)("inlineCode",{parentName:"p"},"enabled"),". You can\nthen attach a ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/PointerListener.kt#L3"},(0,o.kt)("inlineCode",{parentName:"a"},"PointerListener")),"\nto any View and get notified."),(0,o.kt)("p",null,"Pointer listeners are notified whenever a pointer:"),(0,o.kt)("ul",null,(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("strong",{parentName:"li"},"Enters")," a View"),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("strong",{parentName:"li"},"Pressed")," within a View"),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("strong",{parentName:"li"},"Released")," within a View"),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("strong",{parentName:"li"},"Clicked")," (Pressed then Released) within a View"),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("strong",{parentName:"li"},"Exits")," a View")),(0,o.kt)("p",null,"You get these notifications by registering with a View's ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L294"},(0,o.kt)("inlineCode",{parentName:"a"},"pointerChanged")),"\nproperty."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"view.pointerChanged += object: PointerListener {\n    override fun pressed(event: PointerEvent) {\n        // ..\n    }\n}\n")),(0,o.kt)("p",null,"There are also short-hand functions for cases where you only consume one of the events."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"view.pointerChanged += pressed { event ->\n    // ..\n}\n")),(0,o.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/PointerListener.kt#L3"},(0,o.kt)("inlineCode",{parentName:"a"},"PointerListener")),"\nhas no-op defaults for each event, so you only need to implement the ones you need."))),(0,o.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Notice that ",(0,o.kt)("inlineCode",{parentName:"p"},"pointerChanged"),"--like other observable properties--supports many observers and enables you to add/remove an observer any time."))),(0,o.kt)("h2",{id:"pointer-event"},"Pointer Event"),(0,o.kt)("p",null,"The event provided to ",(0,o.kt)("inlineCode",{parentName:"p"},"PointerListener"),"s carries information about the View it originated from (",(0,o.kt)("inlineCode",{parentName:"p"},"target"),"), the View it is sent to (",(0,o.kt)("inlineCode",{parentName:"p"},"source"),"),\nvarious attributes about the state of the pointers--like buttons pressed--and their locations relative to the target View."),(0,o.kt)("p",null,"Pointer events are ",(0,o.kt)("strong",{parentName:"p"},"consumable"),". This means any observer can call ",(0,o.kt)("inlineCode",{parentName:"p"},"consume()")," on an event and prevent subsequent\nlisteners from receiving it."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"// ...\noverride fun pressed(event: PointerEvent) {\n    // ... take action based on event\n\n    event.consume() // indicate that no other listeners should be notified\n}\n// ..\n")),(0,o.kt)("h2",{id:"event-bubbling"},"Event Bubbling"),(0,o.kt)("p",null,'Pointer events "bubble" up to ancestors of a View. Events sent to a View will also be sent up to its parent and so on.\nThis means you can listen to all events that happen to the descendants of a View.'),(0,o.kt)("p",null,"The event sent to a parent is slightly different from the one sent to the View. These events continue to have the same ",(0,o.kt)("inlineCode",{parentName:"p"},"target"),"\n(descendant View where the event fired), but their ",(0,o.kt)("inlineCode",{parentName:"p"},"source")," changes to the recipient ancestor as they bubble."),(0,o.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Bubbling is canceled if any listener calls ",(0,o.kt)("inlineCode",{parentName:"p"},"consume")))),(0,o.kt)("h2",{id:"event-filtering"},"Event Filtering"),(0,o.kt)("p",null,'Pointer events also "sink" from ancestors down to their target. The first phase of pointer event handling is the "sink" phase. It runs ',(0,o.kt)("strong",{parentName:"p"},"before"),' the "bubbling" phase.\nThe root ancestor and all descendants toward the ',(0,o.kt)("inlineCode",{parentName:"p"},"target")," View are notified of the event before the target is."),(0,o.kt)("p",null,'The filter phase is like the "bubbling" phase in reverse. Like with bubbling, ',(0,o.kt)("inlineCode",{parentName:"p"},"PointerListener"),' is used to handle the event. Unlike\n"bubbling", registration happens via the ',(0,o.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L289"},(0,o.kt)("inlineCode",{parentName:"a"},"pointerFilter")),'\nproperty. This phase lets ancestors "veto" an event before it reaches the intended target.'),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-kotlin"},"view.pointerFilter += object: PointerListener {\n    // called whenever a pointer is pressed on this\n    // View or its children, before the target child\n    // is notified\n    override fun pressed(event: PointerEvent) {\n        // ..\n    }\n}\n")),(0,o.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Calling ",(0,o.kt)("inlineCode",{parentName:"p"},"consume")," during filter will prevent descendants (and the target) from receiving the event"))))}k.isMDXComponent=!0}}]);