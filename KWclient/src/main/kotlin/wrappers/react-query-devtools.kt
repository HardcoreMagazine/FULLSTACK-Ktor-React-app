package wrappers

import react.Props
import react.ReactElement
import react.fc

//enable browserDevelopmentWebpack in order to enable this \/
class ReactQueryDevToolsOption(val initialIsOpen: Boolean = true)
//(see note in build.gradle.kts HOW)

interface QueryError {
    val message: String
}

@JsModule("react-query/devtools")
@JsNonModule
external val ReactQueryDevtools: dynamic

val reactQueryDevtools: (options: dynamic) -> ReactElement = ReactQueryDevtools.ReactQueryDevtools

fun cReactQueryDevtools(options: ReactQueryDevToolsOption = ReactQueryDevToolsOption()) = fc("ReactQueryDevtools") { _: Props ->
    child(reactQueryDevtools(options))
}
