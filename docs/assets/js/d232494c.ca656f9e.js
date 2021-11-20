"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[637],{4175:function(e,n,t){t.r(n),t.d(n,{frontMatter:function(){return s},contentTitle:function(){return l},metadata:function(){return d},toc:function(){return p},default:function(){return m}});var a=t(7462),o=t(3366),i=(t(7294),t(3905)),r=["components"],s={hide_title:!0},l="Keyboard Input",d={unversionedId:"keyboard",id:"keyboard",isDocsHomePage:!1,title:"Keyboard Input",description:"Key handling is simple with Doodle. The first thing you need to do is include the KeyboardModule",source:"@site/docs/keyboard.mdx",sourceDirName:".",slug:"/keyboard",permalink:"/docs/keyboard",tags:[],version:"current",frontMatter:{hide_title:!0},sidebar:"tutorialSidebar",previous:{title:"Multi-touch Support",permalink:"/docs/pointer_input/multitouch"},next:{title:"Drag & Drop",permalink:"/docs/dragdrop"}},p=[{value:"Only focused Views receive key events",id:"only-focused-views-receive-key-events",children:[],level:2},{value:"Key Listeners",id:"key-listeners",children:[],level:2},{value:"Key Event",id:"key-event",children:[],level:2},{value:"Identifying Keys",id:"identifying-keys",children:[],level:2}],c={toc:p};function m(e){var n=e.components,t=(0,o.Z)(e,r);return(0,i.kt)("wrapper",(0,a.Z)({},c,t,{components:n,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"keyboard-input"},"Keyboard Input"),(0,i.kt)("p",null,"Key handling is simple with Doodle. The first thing you need to do is include the ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Browser/src/jsMain/kotlin/io/nacular/doodle/application/Modules.kt#L59"},(0,i.kt)("inlineCode",{parentName:"a"},"KeyboardModule")),"\nwhen launching your app. The underlying framework uses the ",(0,i.kt)("inlineCode",{parentName:"p"},"KeyboardModule")," to produce key events."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},"class MyApp(display: Display): Application {\n    // key events will fire for this app when launched with\n    // the KeyboardModule\n}\n")),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},'import io.nacular.doodle.application.Modules.Companion.KeyboardModule\n\nfun main () {\n    // "full screen" launch with keyboard support\n    application(modules = listOf(KeyboardModule)) {\n        MyApp(display = instance())\n    }\n}\n')),(0,i.kt)("h2",{id:"only-focused-views-receive-key-events"},"Only focused Views receive key events"),(0,i.kt)("p",null,"A View must gain ",(0,i.kt)("inlineCode",{parentName:"p"},"focus")," in order to begin receiving key events. This ensures that only a single View\ncan receive key events at any time within the app."),(0,i.kt)("p",null,"Use the ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/focus/FocusManager.kt#L9"},(0,i.kt)("inlineCode",{parentName:"a"},"FocusManager")),"\nto control focus. It is included in the ",(0,i.kt)("inlineCode",{parentName:"p"},"KeyboardModule"),". Just inject it into your app to begin managing the focus."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},"class MyApp(display: Display, focusManager: FocusManager): Application {\n    init {\n        // ...\n        focusManager.requestFocus(view)\n        // ...\n    }\n}\n\nfun main () {\n    application(modules = listOf(KeyboardModule)) {\n        MyApp(display = instance(), focusManager = instance())\n    }\n}\n")),(0,i.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,i.kt)("div",{parentName:"div",className:"admonition-heading"},(0,i.kt)("h5",{parentName:"div"},(0,i.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,i.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,i.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,i.kt)("div",{parentName:"div",className:"admonition-content"},(0,i.kt)("p",{parentName:"div"},"Some controls (i.e. text fields) also manage their focus when styled in the native theme"))),(0,i.kt)("h2",{id:"key-listeners"},"Key Listeners"),(0,i.kt)("p",null,"Views are able to receive key events once the ",(0,i.kt)("inlineCode",{parentName:"p"},"KeyboardModule")," is loaded and they have ",(0,i.kt)("inlineCode",{parentName:"p"},"focus"),". You can\nthen attach a ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/KeyListener.kt#L4"},(0,i.kt)("inlineCode",{parentName:"a"},"KeyListener")),"\nto any View and get notified."),(0,i.kt)("p",null,"Key listeners are notified whenever a key is:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("strong",{parentName:"li"},"Pressed")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("strong",{parentName:"li"},"Released"))),(0,i.kt)("p",null,"You get these notifications by registering with a View's ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/core/View.kt#L299"},(0,i.kt)("inlineCode",{parentName:"a"},"keyChanged")),"\nproperty."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},"view.keyChanged += object: KeyListener {\n    override fun pressed(event: MouseEvent) {\n        // ..\n    }\n}\n")),(0,i.kt)("p",null,"There are also short-hand functions for cases where you only consume one of the events."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},"view.keyChanged += pressed { event ->\n    // ..\n}\n")),(0,i.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,i.kt)("div",{parentName:"div",className:"admonition-heading"},(0,i.kt)("h5",{parentName:"div"},(0,i.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,i.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,i.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,i.kt)("div",{parentName:"div",className:"admonition-content"},(0,i.kt)("p",{parentName:"div"},(0,i.kt)("inlineCode",{parentName:"p"},"KeyListener")," has no-op defaults for the 2 events, so you only need to implement the ones you need."))),(0,i.kt)("div",{className:"admonition admonition-info alert alert--info"},(0,i.kt)("div",{parentName:"div",className:"admonition-heading"},(0,i.kt)("h5",{parentName:"div"},(0,i.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,i.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"},(0,i.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}))),"info")),(0,i.kt)("div",{parentName:"div",className:"admonition-content"},(0,i.kt)("p",{parentName:"div"},"Notice that ",(0,i.kt)("inlineCode",{parentName:"p"},"keyChanged"),"--like other observable properties--supports many observers and enables you to add/remove\nan observer any time."))),(0,i.kt)("h2",{id:"key-event"},"Key Event"),(0,i.kt)("p",null,"The event provided to key listeners carries information about the View it originated from (",(0,i.kt)("inlineCode",{parentName:"p"},"source"),"), and\nvarious attributes about the key that was pressed or released."),(0,i.kt)("p",null,"Key events are ",(0,i.kt)("strong",{parentName:"p"},"consumable"),". This means any observer can call ",(0,i.kt)("inlineCode",{parentName:"p"},"consume()")," on the event and prevent subsequent\nlisteners from receiving it."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},"// ...\noverride fun pressed(event: KeyEvent) {\n    // ... take action based on event\n\n    event.consume() // indicate that no other listeners should be notified\n}\n// ..\n")),(0,i.kt)("h2",{id:"identifying-keys"},"Identifying Keys"),(0,i.kt)("p",null,(0,i.kt)("strong",{parentName:"p"},"Virtual keys and text")),(0,i.kt)("p",null,(0,i.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/KeyEvent.kt#L211"},(0,i.kt)("inlineCode",{parentName:"a"},"KeyEvent.key")),'\nis a layout independent identifier that tells you which "virtual key" was pressed or which text the key can be translated into.\nMost key handling use-cases should use this property to compare keys.'),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},"import io.nacular.doodle.event.KeyText.Companion.Backspace\nimport io.nacular.doodle.event.KeyText.Companion.Enter\n\noverride fun pressed(event: KeyEvent) {\n    when (event.key) {\n        Enter     -> { /* ... */ }\n        Backspace -> { /* ... */ }\n        // ...\n    }\n}\n")),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},'override fun pressed(event: KeyEvent) {\n    // this will be user-appropriate text when the key pressed is not\n    // one of the "named" keys (i.e. Tab, Shift, Enter, ...)\n    inputText += event.key.text\n}\n')),(0,i.kt)("p",null,(0,i.kt)("strong",{parentName:"p"},"Physical keys")),(0,i.kt)("p",null,'Some applications will require the use of "physical" keys instead of virtual ones. This makes sense for games or other apps\nwhere the key position on a physical keyboard matters.'),(0,i.kt)("p",null,"This information comes from ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/nacular/doodle/blob/master/Core/src/commonMain/kotlin/io/nacular/doodle/event/KeyEvent.kt#L211"},(0,i.kt)("inlineCode",{parentName:"a"},"KeyEvent.code")),"."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin"},"import io.nacular.doodle.event.KeyCode.Companion.AltLeft\nimport io.nacular.doodle.event.KeyCode.Companion.AltRight\nimport io.nacular.doodle.event.KeyCode.Companion.Backspace\n\noverride fun pressed(event: KeyEvent) {\n    when (event.code) {\n        AltLeft   -> { /* ... */ }\n        AltRight  -> { /* ... */ }\n        Backspace -> { /* ... */ }\n        // ...\n    }\n}\n")),(0,i.kt)("div",{className:"admonition admonition-caution alert alert--warning"},(0,i.kt)("div",{parentName:"div",className:"admonition-heading"},(0,i.kt)("h5",{parentName:"div"},(0,i.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,i.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"},(0,i.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"}))),"caution")),(0,i.kt)("div",{parentName:"div",className:"admonition-content"},(0,i.kt)("p",{parentName:"div"},"Physical keys do not take keyboard differences and locale into account; so avoid them if possible"))))}m.isMDXComponent=!0}}]);