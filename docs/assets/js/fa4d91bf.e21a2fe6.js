"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[930],{8215:function(e,n,o){var t=o(7294);n.Z=function(e){var n=e.children,o=e.hidden,a=e.className;return t.createElement("div",{role:"tabpanel",hidden:o,className:a},n)}},6396:function(e,n,o){o.d(n,{Z:function(){return m}});var t=o(7462),a=o(7294),r=o(2389),l=o(9443);var i=function(){var e=(0,a.useContext)(l.Z);if(null==e)throw new Error('"useUserPreferencesContext" is used outside of "Layout" component.');return e},s=o(9521),d=o(6010),p="tabItem_vU9c";function c(e){var n,o,t,r=e.lazy,l=e.block,c=e.defaultValue,m=e.values,u=e.groupId,v=e.className,g=a.Children.map(e.children,(function(e){if((0,a.isValidElement)(e)&&void 0!==e.props.value)return e;throw new Error("Docusaurus error: Bad <Tabs> child <"+("string"==typeof e.type?e.type:e.type.name)+'>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.')})),h=null!=m?m:g.map((function(e){var n=e.props;return{value:n.value,label:n.label}})),k=(0,s.lx)(h,(function(e,n){return e.value===n.value}));if(k.length>0)throw new Error('Docusaurus error: Duplicate values "'+k.map((function(e){return e.value})).join(", ")+'" found in <Tabs>. Every value needs to be unique.');var b=null===c?c:null!=(n=null!=c?c:null==(o=g.find((function(e){return e.props.default})))?void 0:o.props.value)?n:null==(t=g[0])?void 0:t.props.value;if(null!==b&&!h.some((function(e){return e.value===b})))throw new Error('Docusaurus error: The <Tabs> has a defaultValue "'+b+'" but none of its children has the corresponding value. Available values are: '+h.map((function(e){return e.value})).join(", ")+". If you intend to show no default tab, use defaultValue={null} instead.");var f=i(),y=f.tabGroupChoices,N=f.setTabGroupChoices,w=(0,a.useState)(b),j=w[0],$=w[1],x=[],O=(0,s.o5)().blockElementScrollPositionUntilNextRender;if(null!=u){var T=y[u];null!=T&&T!==j&&h.some((function(e){return e.value===T}))&&$(T)}var S=function(e){var n=e.currentTarget,o=x.indexOf(n),t=h[o].value;t!==j&&(O(n),$(t),null!=u&&N(u,t))},A=function(e){var n,o=null;switch(e.key){case"ArrowRight":var t=x.indexOf(e.currentTarget)+1;o=x[t]||x[0];break;case"ArrowLeft":var a=x.indexOf(e.currentTarget)-1;o=x[a]||x[x.length-1]}null==(n=o)||n.focus()};return a.createElement("div",{className:"tabs-container"},a.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,d.Z)("tabs",{"tabs--block":l},v)},h.map((function(e){var n=e.value,o=e.label;return a.createElement("li",{role:"tab",tabIndex:j===n?0:-1,"aria-selected":j===n,className:(0,d.Z)("tabs__item",p,{"tabs__item--active":j===n}),key:n,ref:function(e){return x.push(e)},onKeyDown:A,onFocus:S,onClick:S},null!=o?o:n)}))),r?(0,a.cloneElement)(g.filter((function(e){return e.props.value===j}))[0],{className:"margin-vert--md"}):a.createElement("div",{className:"margin-vert--md"},g.map((function(e,n){return(0,a.cloneElement)(e,{key:n,hidden:e.props.value!==j})}))))}function m(e){var n=(0,r.Z)();return a.createElement(c,(0,t.Z)({key:String(n)},e))}},694:function(e,n,o){o.r(n),o.d(n,{frontMatter:function(){return d},contentTitle:function(){return p},metadata:function(){return c},toc:function(){return m},default:function(){return v}});var t=o(7462),a=o(3366),r=(o(7294),o(3905)),l=o(6396),i=o(8215),s=["components"],d={hide_title:!0},p="Installation",c={unversionedId:"installation",id:"installation",isDocsHomePage:!1,title:"Installation",description:"Doodle apps are built using Gradle, like other Kotlin JS or Multi-Platform projects.",source:"@site/docs/installation.mdx",sourceDirName:".",slug:"/installation",permalink:"/doodle/docs/installation",tags:[],version:"current",frontMatter:{hide_title:!0},sidebar:"tutorialSidebar",previous:{title:"Hello Doodle",permalink:"/doodle/docs/introduction"},next:{title:"Applications",permalink:"/doodle/docs/applications"}},m=[{value:"Pure JS Project",id:"pure-js-project",children:[],level:2},{value:"Pure JVM Project",id:"pure-jvm-project",children:[],level:2},{value:"Multi-platform Project",id:"multi-platform-project",children:[],level:2}],u={toc:m};function v(e){var n=e.components,o=(0,a.Z)(e,s);return(0,r.kt)("wrapper",(0,t.Z)({},u,o,{components:n,mdxType:"MDXLayout"}),(0,r.kt)("h1",{id:"installation"},"Installation"),(0,r.kt)("p",null,"Doodle apps are built using ",(0,r.kt)("a",{parentName:"p",href:"http://www.gradle.org"},"Gradle"),", like other Kotlin JS or Multi-Platform projects.\nLearn more by checking out  the Kotlin ",(0,r.kt)("a",{parentName:"p",href:"https://kotlinlang.org/docs/getting-started.html"},"docs"),"."),(0,r.kt)("h2",{id:"pure-js-project"},"Pure JS Project"),(0,r.kt)("p",null,"You can set up a pure Javascript app with the following build scripts."),(0,r.kt)(l.Z,{groupId:"language",mdxType:"Tabs"},(0,r.kt)(i.Z,{value:"kotlin",label:"Kotlin",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:"title=build.gradle.kts",title:"build.gradle.kts"},'plugins {\n    id ("org.jetbrains.kotlin.js") version "1.5.30"\n}\n\nversion = "1.0.0"\ngroup   = "com.my.cool.app"\n\nrepositories {\n    mavenCentral()\n}\n\nkotlin {\n    js().browser()\n\n    val doodleVersion = "0.7.0" // <--- Latest Doodle version\n\n    dependencies {\n        implementation ("io.nacular.doodle:core:$doodleVersion"   )\n        implementation ("io.nacular.doodle:browser:$doodleVersion")\n\n        // Optional\n        // implementation ("io.nacular.doodle:controls:$doodleVersion" )\n        // implementation ("io.nacular.doodle:animation:$doodleVersion")\n        // implementation ("io.nacular.doodle:themes:$doodleVersion"   )\n    }\n}\n'))),(0,r.kt)(i.Z,{value:"groovy",label:"Groovy",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"title=build.gradle",title:"build.gradle"},"plugins {\n    id 'org.jetbrains.kotlin.js' version '1.5.30'\n}\n\nversion = '1.0.0'\ngroup   = 'com.my.cool.app'\n\nrepositories {\n    mavenCentral()\n}\n\next {\n    doodle_version = '0.7.0' // <--- Latest Doodle version\n}\n\nkotlin {\n    js().browser()\n\n    dependencies {\n        implementation \"io.nacular.doodle:core:$doodle_version\"\n        implementation \"io.nacular.doodle:browser:$doodle_version\"\n\n        // Optional\n        // implementation \"io.nacular.doodle:controls:$doodle_version\"\n        // implementation \"io.nacular.doodle:animation:$doodle_version\"\n        // implementation \"io.nacular.doodle:themes:$doodle_version\"\n    }\n}\n")))),(0,r.kt)("h2",{id:"pure-jvm-project"},"Pure JVM Project"),(0,r.kt)("p",null,"You can set up a pure JVM app with the following build scripts."),(0,r.kt)(l.Z,{groupId:"language",mdxType:"Tabs"},(0,r.kt)(i.Z,{value:"kotlin",label:"Kotlin",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:"title=build.gradle.kts",title:"build.gradle.kts"},'plugins {\n    id ("org.jetbrains.kotlin.jvm") version "1.5.30"\n    application\n}\n\nversion = "1.0.0"\ngroup   = "com.my.cool.app"\n\nrepositories {\n    mavenCentral()\n    maven {\n        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")\n    }\n}\n\nkotlin {\n    target.compilations.all {\n        kotlinOptions {\n            jvmTarget = "11"\n        }\n    }\n\n    val doodleVersion = "0.7.0" // <--- Latest Doodle version\n\n    dependencies {\n        val osName = System.getProperty("os.name")\n        val targetOs = when {\n            osName == "Mac OS X"       -> "macos"\n            osName.startsWith("Win"  ) -> "windows"\n            osName.startsWith("Linux") -> "linux"\n            else                       -> error("Unsupported OS: $osName")\n        }\n\n        val osArch = System.getProperty("os.arch")\n        val targetArch = when (osArch) {\n            "x86_64", "amd64" -> "x64"\n            "aarch64"         -> "arm64"\n            else              -> error("Unsupported arch: $osArch")\n        }\n\n        val target = "$targetOs-$targetArch"\n\n        implementation ("io.nacular.doodle:core:$doodleVersion"               )\n        implementation ("io.nacular.doodle:desktop-jvm-$target:$doodleVersion") // Desktop apps are tied to specific platforms\n\n        // Optional\n        // implementation ("io.nacular.doodle:controls:$doodleVersion" )\n        // implementation ("io.nacular.doodle:animation:$doodleVersion")\n        // implementation ("io.nacular.doodle:themes:$doodleVersion"   )\n    }\n}\n\napplication {\n    mainClass.set("YOUR_CLASS")\n}\n'))),(0,r.kt)(i.Z,{value:"groovy",label:"Groovy",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"title=build.gradle",title:"build.gradle"},'plugins {\n    id \'org.jetbrains.kotlin.jvm\' version \'1.5.30\'\n    id \'application\'\n}\n\nversion = \'1.0.0\'\ngroup   = \'com.my.cool.app\'\n\nrepositories {\n    mavenCentral()\n    maven {\n        url "https://maven.pkg.jetbrains.space/public/p/compose/dev"\n    }\n}\n\next {\n    doodle_version = \'0.7.0\' // <--- Latest Doodle version\n}\n\nkotlin {\n    target.compilations.all {\n        kotlinOptions {\n            jvmTarget = "11"\n        }\n    }\n\n    dependencies {\n        targetOs = ""\n        osName = System.getProperty("os.name")\n        if      (osName ==         "Mac OS X")  targetOs = "macos"\n        else if (osName.startsWith("Win"     )) targetOs = "windows"\n        else if (osName.startsWith("Linux"   )) targetOs = "linux"\n        else                                    error("Unsupported OS: $osName")\n\n        targetArch = ""\n        osArch = System.getProperty("os.arch")\n        switch (osArch) {\n            case ["x86_64", "amd64"]: targetArch = "x64"  ; break\n            case "aarch64"          : targetArch = "arm64"; break\n            default:                  error("Unsupported arch: $osArch")\n        }\n\n        target = "$targetOs-$targetArch"\n\n        implementation ("io.nacular.doodle:core:$doodleVersion"               )\n        implementation ("io.nacular.doodle:desktop-jvm-$target:$doodleVersion") // Desktop apps are tied to specific platforms\n\n        // Optional\n        // implementation "io.nacular.doodle:controls:$doodle_version"\n        // implementation "io.nacular.doodle:animation:$doodle_version"\n        // implementation "io.nacular.doodle:themes:$doodle_version"\n    }\n}\n\napplication {\n    mainClassName = "YOUR_CLASS"\n}\n')))),(0,r.kt)("h2",{id:"multi-platform-project"},"Multi-platform Project"),(0,r.kt)("p",null,"Doodle is a set of Kotlin Multi-platform (MPP) libraries. Which means you can create an MPP for your app as well. The advantage of this\nis that you can write your app entirely (except for ",(0,r.kt)("inlineCode",{parentName:"p"},"main"),") in ",(0,r.kt)("inlineCode",{parentName:"p"},"common")," code and make it available on both Web (JS) and Desktop (JVM). The\nfollowing shows how to create such an app."),(0,r.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,r.kt)("div",{parentName:"div",className:"admonition-heading"},(0,r.kt)("h5",{parentName:"div"},(0,r.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,r.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,r.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,r.kt)("div",{parentName:"div",className:"admonition-content"},(0,r.kt)("p",{parentName:"div"},"App ",(0,r.kt)("a",{parentName:"p",href:"/doodle/docs/applications#app-launch"},(0,r.kt)("strong",{parentName:"a"},"launch code"))," is the only portion that needs to be in ",(0,r.kt)("inlineCode",{parentName:"p"},"js")," or ",(0,r.kt)("inlineCode",{parentName:"p"},"jvm"),"."))),(0,r.kt)(l.Z,{groupId:"language",mdxType:"Tabs"},(0,r.kt)(i.Z,{value:"kotlin",label:"Kotlin",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:"title=build.gradle.kts",title:"build.gradle.kts"},'plugins {\n    id ("org.jetbrains.kotlin.multiplatform") version "1.5.30"\n    application\n}\n\nversion = "1.0.0"\ngroup   = "com.my.cool.app"\n\nrepositories {\n    mavenCentral()\n    maven {\n        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")\n    }\n}\n\nkotlin {\n    js().browser()\n\n    jvm {\n        withJava()\n        compilations.all {\n            kotlinOptions {\n                jvmTarget = "11"\n            }\n        }\n    }\n\n    val doodleVersion = "0.7.0" // <--- Latest Doodle version\n\n    sourceSets {\n        val commonMain by getting {\n            dependencies {\n                implementation ("io.nacular.doodle:core:$doodleVersion")\n\n                // Optional\n                // implementation ("io.nacular.doodle:controls:$doodleVersion" )\n                // implementation ("io.nacular.doodle:animation:$doodleVersion")\n                // implementation ("io.nacular.doodle:themes:$doodleVersion"   )\n            }\n        }\n\n        val jsMain by getting {\n            dependencies {\n                implementation ("io.nacular.doodle:browser:$doodleVersion")\n            }\n        }\n\n        val jvmMain by getting {\n            dependencies {\n                val osName = System.getProperty("os.name")\n                val targetOs = when {\n                    osName == "Mac OS X"       -> "macos"\n                    osName.startsWith("Win"  ) -> "windows"\n                    osName.startsWith("Linux") -> "linux"\n                    else                       -> error("Unsupported OS: $osName")\n                }\n\n                val osArch = System.getProperty("os.arch")\n                val targetArch = when (osArch) {\n                    "x86_64", "amd64" -> "x64"\n                    "aarch64"         -> "arm64"\n                    else              -> error("Unsupported arch: $osArch")\n                }\n\n                val target = "$targetOs-$targetArch"\n\n                implementation ("io.nacular.doodle:desktop-jvm-$target:$doodleVersion") // Desktop apps are tied to specific platforms\n            }\n        }\n    }\n}\n\napplication {\n    mainClass.set("YOUR_CLASS")\n}\n'))),(0,r.kt)(i.Z,{value:"groovy",label:"Groovy",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"title=build.gradle",title:"build.gradle"},'plugins {\n    id \'org.jetbrains.kotlin.multiplatform\' version \'1.5.30\'\n    id \'application\'\n}\n\nversion = \'1.0.0\'\ngroup   = \'com.my.cool.app\'\n\nrepositories {\n    mavenCentral()\n    maven {\n        url "https://maven.pkg.jetbrains.space/public/p/compose/dev"\n    }\n}\n\next {\n    doodle_version = \'0.7.0\' // <--- Latest Doodle version\n}\n\nkotlin {\n    js().browser()\n\n    jvm {\n        withJava()\n        compilations.all {\n            kotlinOptions {\n                jvmTarget = "11"\n            }\n        }\n    }\n\n    sourceSets {\n        commonMain.dependencies {\n            implementation "io.nacular.doodle:core:$doodle_version"\n\n            // Optional\n            // implementation "io.nacular.doodle:controls:$doodle_version"\n            // implementation "io.nacular.doodle:animation:$doodle_version"\n            // implementation "io.nacular.doodle:themes:$doodle_version"\n        }\n\n        jsMain.dependencies {\n            implementation "io.nacular.doodle:browser:$doodle_version"\n        }\n\n        jvmMain.dependencies {\n            targetOs = ""\n            osName = System.getProperty("os.name")\n            if      (osName ==         "Mac OS X")  targetOs = "macos"\n            else if (osName.startsWith("Win"     )) targetOs = "windows"\n            else if (osName.startsWith("Linux"   )) targetOs = "linux"\n            else                                    error("Unsupported OS: $osName")\n\n            targetArch = ""\n            osArch = System.getProperty("os.arch")\n            switch (osArch) {\n                case ["x86_64", "amd64"]: targetArch = "x64"  ; break\n                case "aarch64"          : targetArch = "arm64"; break\n                default:                  error("Unsupported arch: $osArch")\n            }\n\n            target = "$targetOs-$targetArch"\n\n            implementation ("io.nacular.doodle:desktop-jvm-$target:$doodleVersion") // Desktop apps are tied to specific platforms\n        }\n    }\n}\n\napplication {\n    mainClassName = "YOUR_CLASS"\n}\n')))))}v.isMDXComponent=!0}}]);