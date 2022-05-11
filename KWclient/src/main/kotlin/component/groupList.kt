package component

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.Props
import react.dom.*
import react.fc
import react.query.useQuery
import react.router.dom.Link
import server.model.Config
import server.model.Item
import wrappers.QueryError
import wrappers.fetchText

external interface GroupListProps : Props {
    var groups: List<String> //list of group NAMES
}

fun fcGroupList() = fc("GroupList") { props: GroupListProps ->
    h3 { +"Groups:" }
    ul {
        props.groups.map {
            li {
                Link {
                    attrs.to = "/groups/$it"
                    +"$it\t"
                }
            }
        }
    }
}

//see explanation in file "lesson.kt" (package 'component')
@Serializable
class ClientItemGroup(
    override val elem: String,
    override val uuid: String,
    override val etag: Long
) : Item<String>

fun fcContainerGroupList() = fc("QueryGroupList") { _: Props ->
    val queryGroups = useQuery<String, QueryError, String, String>(
        "groupList", { fetchText(Config.groupsURL) })
    if (queryGroups.isLoading)
        div { +"Loading ..." }
    else if (queryGroups.isError)
        div { +"Query error. Please contact server administrator at: admin@adminmail." }
    else {
        val groups: List<ClientItemGroup> = Json.decodeFromString(queryGroups.data?:"")
        child(fcGroupList()) {
            attrs.groups = groups.map { it.elem }
        }
    }
}
