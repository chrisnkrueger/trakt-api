package app.moviebase.trakt.api

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.core.getByPaths
import app.moviebase.trakt.core.parameterEndAt
import app.moviebase.trakt.core.parameterExtended
import app.moviebase.trakt.core.parameterLimit
import app.moviebase.trakt.core.parameterPage
import app.moviebase.trakt.core.parameterStartAt
import app.moviebase.trakt.core.postByPaths
import app.moviebase.trakt.model.TraktHistoryItem
import app.moviebase.trakt.model.TraktList
import app.moviebase.trakt.model.TraktListMediaType
import app.moviebase.trakt.model.TraktSyncItems
import app.moviebase.trakt.model.TraktSyncResponse
import app.moviebase.trakt.model.TraktUser
import app.moviebase.trakt.model.TraktUserListItem
import app.moviebase.trakt.model.TraktUserSettings
import app.moviebase.trakt.model.TraktUserSlug
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.Instant

class TraktUsersApi(
    private val client: HttpClient,
) {
    suspend fun getSettings(): TraktUserSettings = client.getByPaths("users")

    suspend fun getProfile(
        userSlug: TraktUserSlug,
        extended: TraktExtended? = null,
    ): TraktUser =
        client.getByPaths(*pathUsers(userSlug)) {
            extended?.let { parameterExtended(it) }
        }

    suspend fun createList(
        userSlug: TraktUserSlug = TraktUserSlug.ME,
        list: TraktList,
    ): TraktList =
        client.postByPaths(*pathUsers(userSlug, "lists")) {
            contentType(ContentType.Application.Json)
            setBody(list)
        }

    suspend fun getLists(userSlug: TraktUserSlug = TraktUserSlug.ME): List<TraktList> = client.getByPaths(*pathUsers(userSlug, "lists"))

    suspend fun getListItems(
        userSlug: TraktUserSlug = TraktUserSlug.ME,
        listId: String,
        extended: TraktExtended? = null,
    ): List<TraktUserListItem> =
        client.getByPaths(*pathLists(userSlug, listId)) {
            extended?.let { parameterExtended(it) }
        }

    suspend fun addListItems(
        userSlug: TraktUserSlug = TraktUserSlug.ME,
        listId: String,
        items: TraktSyncItems,
    ): TraktSyncResponse =
        client.postByPaths(*pathLists(userSlug, listId)) {
            contentType(ContentType.Application.Json)
            setBody(items)
        }

    suspend fun removeListItems(
        userSlug: TraktUserSlug = TraktUserSlug.ME,
        listId: String,
        items: TraktSyncItems,
    ): TraktSyncResponse =
        client.postByPaths(*pathLists(userSlug, listId, "remove")) {
            contentType(ContentType.Application.Json)
            setBody(items)
        }

    /**
     * Example: users/id/history/type/item_id?start_at=2016-06-01T00%3A00%3A00.000Z&end_at=2016-07-01T23%3A59%3A59.000Z
     */
    suspend fun getHistory(
        userSlug: TraktUserSlug = TraktUserSlug.ME,
        listType: TraktListMediaType? = null,
        itemId: Int? = null,
        extended: TraktExtended? = null,
        startAt: Instant? = null,
        endAt: Instant? = null,
        page: Int? = null,
        limit: Int? = null,
    ): List<TraktHistoryItem> =
        client.getByPaths(*pathHistory(userSlug, listType, itemId)) {
            extended?.let { parameterExtended(extended) }
            startAt?.let { parameterStartAt(it) }
            endAt?.let { parameterEndAt(it) }
            page?.let { parameterPage(it) }
            limit?.let { parameterLimit(it) }
        }

    /**
     * Path: users/userSlug
     */
    private fun pathUsers(
        userSlug: TraktUserSlug,
        vararg paths: String,
    ) = arrayOf("users", userSlug.name, *paths)

    /**
     * Path: /users/userSlug/history/type/item_id
     */
    private fun pathHistory(
        userSlug: TraktUserSlug,
        listType: TraktListMediaType?,
        itemId: Int?,
        vararg paths: String,
    ) = listOfNotNull("users", userSlug.name, "history", listType?.value, itemId?.toString(), *paths).toTypedArray()

    /**
     * Path: users/{userSlug}/lists/{id}/items
     */
    private fun pathLists(
        userSlug: TraktUserSlug,
        listId: String,
        vararg paths: String,
    ) = pathUsers(userSlug, "lists", listId, "items", *paths)
}
