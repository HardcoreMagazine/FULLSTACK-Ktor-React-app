import component.*
import kotlinx.browser.document
import react.createElement
//import react.dom.br
import react.dom.render
import react.query.QueryClient
import react.query.QueryClientProvider
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.dom.Link
import wrappers.cReactQueryDevtools

val queryClient = QueryClient()

fun main() {
    render(document.getElementById("root")!!) {
        HashRouter {
            QueryClientProvider {
                attrs.client = queryClient
                Link {
                    attrs.to = "/"
                    +"Home"
                }
                +"⠀⠀⠀⠀" //invisible symbols -- unicode/U+2800
                Link {
                    attrs.to = "/students"
                    +"Students"
                }
                +"⠀⠀⠀⠀"
                Link {
                    attrs.to = "/groups"
                    +"Groups"
                }
                +"⠀⠀⠀⠀"
                Link {
                    attrs.to = "/teachers"
                    +"Teachers"
                }
                +"⠀⠀⠀⠀"
                Link {
                    attrs.to = "/lessons"
                    +"Lessons"
                }
                Routes {
                    /**----------------------STUDENTS---------------------**/
                    Route {
                        attrs.index = true
                        attrs.path = "/students"
                        attrs.element = createElement(fcContainerStudentList())
                    }
                    Route {
                        attrs.path = "/students/:id"
                        attrs.element = createElement(fcContainerStudent())
                    }
                    /**----------------------GROUPS-----------------------**/
                    Route {
                        attrs.index = true
                        attrs.path = "/groups"
                        attrs.element = createElement(fcContainerGroupList())
                    }
                    Route {
                        attrs.path = "/groups/:group"
                        attrs.element = createElement(fcContainerGroup())
                    }
                    /**----------------------TEACHERS---------------------**/
                    Route {
                        attrs.index = true
                        attrs.path = "/teachers"
                        attrs.element = createElement(fcContainerTeacherList())
                    }
                    Route {
                        attrs.path = "/teachers/:id"
                        attrs.element = createElement(fcContainerTeacher())
                    }
                    /**----------------------LESSONS----------------------**/
                    Route {
                        attrs.index = true
                        attrs.path = "/lessons"
                        attrs.element = createElement(fcContainerLessonList())
                    }
                    Route {
                        attrs.path = "/lessons/:id/details"
                        attrs.element = createElement(fcContainerLesson())
                    }
                }
                child(cReactQueryDevtools()) {}
            }
        }
    }
}

