package com.toasterofbread.spmp.ui.layout.apppage.settingspage

import com.toasterofbread.composesettings.ui.item.SettingsDropdownItem
import com.toasterofbread.composesettings.ui.item.SettingsItem
import com.toasterofbread.composesettings.ui.item.SettingsSliderItem
import com.toasterofbread.composesettings.ui.item.SettingsToggleItem
import com.toasterofbread.composesettings.ui.item.SettingsValueState
import com.toasterofbread.spmp.model.Settings
import com.toasterofbread.spmp.model.mediaitem.song.SongAudioQuality
import com.toasterofbread.spmp.resources.getString
import com.toasterofbread.spmp.youtubeapi.formats.VideoFormatsEndpointType

internal fun getDownloadCategory(): List<SettingsItem> {
    return listOf(
        SettingsDropdownItem(
            SettingsValueState(Settings.KEY_VIDEO_FORMATS_METHOD.name),
            getString("s_key_video_formats_endpoint"), null, VideoFormatsEndpointType.values().size
        ) { i ->
            VideoFormatsEndpointType.values()[i].getReadable()
        },

        SettingsToggleItem(
            SettingsValueState(Settings.KEY_AUTO_DOWNLOAD_ENABLED.name),
            getString("s_key_auto_download_enabled"), null
        ),

        SettingsSliderItem(
            SettingsValueState<Int>(Settings.KEY_AUTO_DOWNLOAD_THRESHOLD.name),
            getString("s_key_auto_download_threshold"), getString("s_sub_auto_download_threshold"),
            range = 1f..10f,
            min_label = "1",
            max_label = "10"
        ),

        SettingsDropdownItem(
            SettingsValueState(Settings.KEY_STREAM_AUDIO_QUALITY.name),
            getString("s_key_stream_audio_quality"), getString("s_sub_stream_audio_quality"), 3
        ) { i ->
            when (i) {
                SongAudioQuality.HIGH.ordinal -> getString("s_option_audio_quality_high")
                SongAudioQuality.MEDIUM.ordinal -> getString("s_option_audio_quality_medium")
                else -> getString("s_option_audio_quality_low")
            }
        },

        SettingsDropdownItem(
            SettingsValueState(Settings.KEY_DOWNLOAD_AUDIO_QUALITY.name),
            getString("s_key_download_audio_quality"), getString("s_sub_download_audio_quality"), 3
        ) { i ->
            when (i) {
                SongAudioQuality.HIGH.ordinal -> getString("s_option_audio_quality_high")
                SongAudioQuality.MEDIUM.ordinal -> getString("s_option_audio_quality_medium")
                else -> getString("s_option_audio_quality_low")
            }
        }
    )
}
