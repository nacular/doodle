"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[1196],{6648:(e,n,o)=>{o.r(n),o.d(n,{assets:()=>g,contentTitle:()=>u,default:()=>b,frontMatter:()=>h,metadata:()=>m,toc:()=>v});var i=o(7624),t=o(2172),a=(o(1268),o(5388),o(964),o(7996)),r=o(5720),l=o(3148);const s="package simplecircle\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.core.center\nimport io.nacular.doodle.core.height\nimport io.nacular.doodle.core.view\nimport io.nacular.doodle.core.width\nimport io.nacular.doodle.drawing.Color.Companion.Red\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.geometry.Circle\nimport org.kodein.di.instance\nimport kotlin.math.min\n\n//sampleStart\nclass SimpleCircle(display: Display): Application {\n    init {\n        display += view {\n            size   = display.size\n            render = {\n                circle(\n                    Circle(\n                    center = display.center,\n                    radius = min(display.width, display.height) / 2 - 10\n                ), fill = Red.paint)\n            }\n        }\n\n        display.fill(White.paint)\n    }\n\n    override fun shutdown() {}\n}\n\nfun main() {\n    application {\n        SimpleCircle(display = instance())\n    }\n}\n//sampleEnd",d="package invisiblebutton\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.controls.buttons.PushButton\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.geometry.Size\nimport org.kodein.di.instance\n\n//sampleStart\nclass InvisibleButton(display: Display): Application {\n    init {\n        // NOTE: This does not render because the button has no\n        // Behavior installed\n        display += PushButton().apply { size = Size(40, 20) }\n        display.fill(White.paint)\n    }\n\n    override fun shutdown() {}\n}\n\nfun main() {\n    application {\n        InvisibleButton(display = instance())\n    }\n}\n//sampleEnd",p='package buttonwiththeme\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.PointerModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.controls.buttons.PushButton\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.docs.apps.ButtonWithTheme\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.geometry.Size\nimport io.nacular.doodle.layout.constraints.center\nimport io.nacular.doodle.layout.constraints.constrain\nimport io.nacular.doodle.theme.Theme\nimport io.nacular.doodle.theme.ThemeManager\nimport io.nacular.doodle.theme.native.NativeTheme.Companion.nativeButtonBehavior\nimport org.kodein.di.instance\n\n//sampleStart\nclass ButtonWithTheme(display: Display, themeManager: ThemeManager, theme: Theme): Application {\n    init {\n        // install theme that provides a button behavior\n        themeManager.selected = theme\n\n        display += PushButton("Hi").apply { size = Size(80, 40) }\n        display.layout = constrain(display.first(), center)\n        display.fill(White.paint)\n    }\n\n    override fun shutdown() {}\n}\n\nfun main() {\n    application(modules = listOf(PointerModule, nativeButtonBehavior())) {\n        ButtonWithTheme(display = instance(), themeManager = instance(), theme = instance())\n    }\n}\n//sampleEnd',c='package buttonwithbehavior\n\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.application.Modules.Companion.PointerModule\nimport io.nacular.doodle.application.application\nimport io.nacular.doodle.controls.buttons.PushButton\nimport io.nacular.doodle.controls.theme.simpleButtonRenderer\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.drawing.Color.Companion.Lightgray\nimport io.nacular.doodle.drawing.Color.Companion.White\nimport io.nacular.doodle.drawing.darker\nimport io.nacular.doodle.drawing.paint\nimport io.nacular.doodle.geometry.Size\nimport io.nacular.doodle.layout.constraints.center\nimport io.nacular.doodle.layout.constraints.constrain\nimport org.kodein.di.instance\n\n//sampleStart\nclass ButtonWithBehavior(display: Display): Application {\n    init {\n        display += PushButton("Hi").apply {\n            size = Size(80, 40)\n\n            // Assign a Behavior to the button to handle rendering\n            behavior = simpleButtonRenderer { button, canvas ->\n                var color = Lightgray\n\n                if (button.model.pressed    ) color = color.darker(0.25f)\n                if (button.model.pointerOver) color = color.darker(0.25f)\n\n                canvas.rect(button.bounds.atOrigin, radius = 10.0, fill = color.paint)\n            }\n\n//            acceptsThemes = false // prevents any theme from overriding behavior\n        }\n        display.layout = constrain(display.first(), center)\n        display.fill(White.paint)\n    }\n\n    override fun shutdown() {}\n}\n\nfun main() {\n    // PointerModule required to support mouse/touch\n    application(modules = listOf(PointerModule)) {\n        ButtonWithBehavior(display = instance())\n    }\n}\n//sampleEnd',h={hide_title:!0,title:"Where's My View?"},u=void 0,m={id:"troubleshooting/gotchas",title:"Where's My View?",description:"Where's my view?",source:"@site/docs/troubleshooting/gotchas.mdx",sourceDirName:"troubleshooting",slug:"/troubleshooting/gotchas",permalink:"/doodle/docs/troubleshooting/gotchas",draft:!1,unlisted:!1,tags:[],version:"current",frontMatter:{hide_title:!0,title:"Where's My View?"},sidebar:"tutorialSidebar",previous:{title:"Accessibility",permalink:"/doodle/docs/accessibility"}},g={},v=[{value:"Where&#39;s my view?",id:"wheres-my-view",level:2},{value:"Where is my Button?",id:"where-is-my-button",level:2},{value:"Setting a Behavior fixes the button",id:"setting-a-behavior-fixes-the-button",level:2},{value:"A Theme could also be used",id:"a-theme-could-also-be-used",level:2}];function y(e){const n={a:"a",admonition:"admonition",code:"code",h2:"h2",p:"p",...(0,t.M)(),...e.components};return l||w("api",!1),l.Behavior||w("api.Behavior",!0),l.ButtonBehavior||w("api.ButtonBehavior",!0),l.NativeButtonBehavior||w("api.NativeButtonBehavior",!0),l.PushButton||w("api.PushButton",!0),l.Theme||w("api.Theme",!0),l.ThemeManager||w("api.ThemeManager",!0),(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(n.h2,{id:"wheres-my-view",children:"Where's my view?"}),"\n",(0,i.jsx)(n.p,{children:"The following app has a single top-level view that fills the Display and draws a centered circle."}),"\n",(0,i.jsx)(r.A,{children:s}),"\n",(0,i.jsx)(a.u,{functionName:"simpleCircle",height:"200"}),"\n",(0,i.jsx)(n.h2,{id:"where-is-my-button",children:"Where is my Button?"}),"\n",(0,i.jsxs)(n.p,{children:["Now we try the same thing with a ",(0,i.jsx)(l.PushButton,{}),". The following code feels like it should work; but it doesn't."]}),"\n",(0,i.jsx)(r.A,{children:d}),"\n",(0,i.jsx)(a.u,{functionName:"invisibleButton",height:"200"}),"\n",(0,i.jsx)(n.admonition,{type:"danger",children:(0,i.jsxs)(n.p,{children:["The above examples does not render anything because ",(0,i.jsx)(l.PushButton,{})," delegates all rendering to its ",(0,i.jsx)(l.ButtonBehavior,{})," and the button has none specified."]})}),"\n",(0,i.jsxs)(n.h2,{id:"setting-a-behavior-fixes-the-button",children:["Setting a ",(0,i.jsx)(n.a,{href:"/doodle/docs/rendering/behaviors",children:"Behavior"})," fixes the button"]}),"\n",(0,i.jsxs)(n.p,{children:["The above example does not render anything because the button has no ",(0,i.jsx)(l.Behavior,{})," installed. ",(0,i.jsx)(l.PushButton,{})," (like many Views in the ",(0,i.jsx)(n.code,{children:"controls"})," library) does not render directly itself, but relies on its ",(0,i.jsx)(l.ButtonBehavior,{})," for all drawing."]}),"\n",(0,i.jsxs)(n.p,{children:["So we can fix the above app by explicitly adding a ",(0,i.jsx)(l.Behavior,{})," to the button."]}),"\n",(0,i.jsx)(r.A,{children:c}),"\n",(0,i.jsx)(a.u,{functionName:"buttonWithBehavior",height:"200"}),"\n",(0,i.jsxs)(n.h2,{id:"a-theme-could-also-be-used",children:["A ",(0,i.jsx)(n.a,{href:"/doodle/docs/themes",children:"Theme"})," could also be used"]}),"\n",(0,i.jsxs)(n.p,{children:["Themes are a great way to assign behaviors to your entire app all at once. In this version we register the ",(0,i.jsx)(l.NativeButtonBehavior,{})," module which makes a ",(0,i.jsx)(l.Theme,{})," available that will assign a native button behavior to all buttons in the app by default. This theme is injected into the app along with the ",(0,i.jsx)(l.ThemeManager,{})," which is used to select the theme. The result is that our button now has a behavior and delegates all rendering to it."]}),"\n",(0,i.jsx)(r.A,{children:p}),"\n",(0,i.jsx)(a.u,{functionName:"buttonWithTheme",height:"200"})]})}function b(e={}){const{wrapper:n}={...(0,t.M)(),...e.components};return n?(0,i.jsx)(n,{...e,children:(0,i.jsx)(y,{...e})}):y(e)}function w(e,n){throw new Error("Expected "+(n?"component":"object")+" `"+e+"` to be defined: you likely forgot to import, pass, or provide it.")}}}]);