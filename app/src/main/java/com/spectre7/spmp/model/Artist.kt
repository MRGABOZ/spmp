package com.spectre7.spmp.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.spectre7.spmp.api.isSubscribedToArtist
import com.spectre7.spmp.api.subscribeOrUnsubscribeArtist
import com.spectre7.spmp.ui.component.ArtistPreviewLong
import com.spectre7.spmp.ui.component.ArtistPreviewSquare
import com.spectre7.spmp.ui.layout.PlayerViewContext
import com.spectre7.utils.sendToast
import kotlin.concurrent.thread

class Artist private constructor (
    id: String
): MediaItemWithLayouts(id) {

    init {
        supplyArtist(this, true)
    }

    private var _subscribe_channel_id: String? by mutableStateOf(null)
    val subscribe_channel_id: String?
        get() = _subscribe_channel_id

    fun supplySubscribeChannelId(value: String?, certain: Boolean): MediaItem {
        if (value != null && (_subscribe_channel_id == null || certain)) {
            _subscribe_channel_id = value
        }
        return this
    }

    private var _subscriber_count_text: String? by mutableStateOf(null)
    val subscriber_count_text: String? get() = _subscriber_count_text

    fun supplySubscriberCountText(value: String?, certain: Boolean): MediaItem {
        if (value != null && (_subscriber_count_text == null || certain)) {
            _subscriber_count_text = value
        }
        return this
    }

    var subscribed: Boolean? by mutableStateOf(null)

    override fun isLoaded(): Boolean {
        return super.isLoaded() && _subscribe_channel_id != null
    }

    override fun getJsonMapValues(klaxon: Klaxon): String {
        return super.getJsonMapValues(klaxon) + "\"subscribe_channel_id\": ${stringToJson(subscribe_channel_id)},"
    }

    override fun supplyFromJsonObject(data: JsonObject, klaxon: Klaxon): MediaItem {
        _subscribe_channel_id = data.string("subscribe_channel_id")
        return super.supplyFromJsonObject(data, klaxon)
    }

    companion object {
        private val artists: MutableMap<String, Artist> = mutableMapOf()
        val UNKNOWN = Artist.fromId("").supplyTitle("Unknown", true).supplyDescription("No known artist attached to media", true) as Artist

        @Synchronized
        fun fromId(id: String): Artist {
            return artists.getOrElse(id) {
                val ret = Artist(id)
                artists[id] = ret
                return ret
            }.getOrReplacedWith() as Artist
        }
    }

    @Composable
    override fun PreviewSquare(content_colour: () -> Color, playerProvider: () -> PlayerViewContext, enable_long_press_menu: Boolean, modifier: Modifier) {
        ArtistPreviewSquare(this, content_colour, playerProvider, enable_long_press_menu, modifier)
    }

    @Composable
    override fun PreviewLong(content_colour: () -> Color, playerProvider: () -> PlayerViewContext, enable_long_press_menu: Boolean, modifier: Modifier) {
        ArtistPreviewLong(this, content_colour, playerProvider, enable_long_press_menu, modifier)
    }

    override val url: String get() = "https://music.youtube.com/channel/$id"

    fun getFormattedSubscriberCount(): String {
        return subscriber_count_text ?: "Unknown"
//        val subs = subscriber_count.toInt()
//        if (subs >= 1000000) {
//            return "${subs / 1000000}M"
//        }
//        else if (subs >= 1000) {
//            return "${subs / 1000}K"
//        }
//        else {
//            return "$subs"
//        }
    }

    fun updateSubscribed() {
        subscribed = isSubscribedToArtist(this).getNullableDataOrThrow()
    }

    fun toggleSubscribe(toggle_before_fetch: Boolean = false, notify_failure: Boolean = false) {
        thread {
            if (subscribed == null) {
                throw IllegalStateException()
            }

            val target = !subscribed!!

            if (toggle_before_fetch) {
                subscribed = target
            }

            subscribeOrUnsubscribeArtist(this, target).getDataOrThrow()
            updateSubscribed()

            if (notify_failure && subscribed != target) {
                sendToast(
                    if (target) "Subscribing to $title failed"
                    else        "Unsubscribed from $title failed"
                )
            }
        }
    }

}