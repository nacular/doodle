// Plugin based on https://github.com/njleonzhang/docsify-demo-box-react
window.$docsify.plugins.push(
    function(hook, vm) {
        var currentId = 0

        function startApp(config) {
            setTimeout(function () {
                var element = document.getElementById(config.id)

                var parent = element.parentNode

                if (parent) {
                    var className = parent.className

                    parent.addEventListener("mouseenter", function( event ) {
                      event.target.className = className + " hover"
                    }, false);

                    parent.addEventListener("mouseleave", function( event ) {
                      event.target.className = className
                    }, false);
                }

                element.style.height = config.height+''

                eval(config.run)(element)
            })
        }

        window.$docsify.markdown = {
            renderer: {
                code: function(code, lang) {
                    if (lang == "doodle") {
                        var id     = "a" + currentId++
                        var config = JSON.parse(code)
                        var cls

                        if (config.border == false) {
                            cls = 'class = "doodle"'
                        } else {
                            cls = 'class = "doodle border"'
                        }

                        config.id = id

                        startApp(config)

                        return '<div '+cls+' style="margin:var(--code-block-margin)"/><div id="' + id + '" style="position:relative"></div></div/>'
                    } else {
                        lang = lang || ''
                        hl   = Prism.highlight(code, Prism.languages[lang] || Prism.languages.markup)
                        return '<pre v-pre data-lang="' +lang+ '"><code class="lang-' +lang+ '">' +hl+ '</code></pre>'
                    }
                }
            }
        }
    }
)