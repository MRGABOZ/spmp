package com.toasterofbread.spmp.ui.layout.apppage.library

import LocalPlayerState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.toasterofbread.spmp.model.mediaitem.MediaItemHolder
import com.toasterofbread.spmp.model.mediaitem.MediaItemSortType
import com.toasterofbread.spmp.platform.PlatformContext
import com.toasterofbread.spmp.platform.vibrateShort
import com.toasterofbread.spmp.resources.getString
import com.toasterofbread.spmp.ui.component.ErrorInfoDisplay
import com.toasterofbread.spmp.ui.component.multiselect.MediaItemMultiSelectContext
import com.toasterofbread.spmp.ui.layout.apppage.AppPage
import com.toasterofbread.spmp.ui.layout.apppage.AppPageState
import com.toasterofbread.spmp.ui.theme.Theme
import com.toasterofbread.utils.common.copy
import com.toasterofbread.utils.composable.PlatformClickableIconButton
import com.toasterofbread.utils.composable.ResizableOutlinedTextField

abstract class LibrarySubPage(val context: PlatformContext) {
    abstract fun getIcon(): ImageVector

    open fun isHidden(): Boolean = false
    open fun enableSearch(): Boolean = true
    open fun enableSorting(): Boolean = true
    open fun getDefaultSortType(): MediaItemSortType = MediaItemSortType.PLAY_COUNT
    open fun nativeSortTypeLabel(): String? = null

    @Composable
    abstract fun Page(
        library_page: LibraryAppPage,
        content_padding: PaddingValues,
        multiselect_context: MediaItemMultiSelectContext,
        modifier: Modifier
    )
}

class LibraryAppPage(override val state: AppPageState): AppPage() {
    val tabs: List<LibrarySubPage> = listOf(
        LibraryPlaylistsPage(state.context),
        LibrarySongsPage(state.context),
        LibraryArtistsPage(state.context),
        LibraryProfilePage(state.context)
    )
    private var current_tab: LibrarySubPage by mutableStateOf(tabs.first())

    private var show_search_field: Boolean by mutableStateOf(false)
    var search_filter: String? by mutableStateOf(null)

    private var show_sort_type_menu: Boolean by mutableStateOf(false)
    var sort_type: MediaItemSortType by mutableStateOf(current_tab.getDefaultSortType())
    var reverse_sort: Boolean by mutableStateOf(false)

    var external_load_error: Throwable? by mutableStateOf(null)

    override fun onOpened(from_item: MediaItemHolder?) {
        setCurrentTab(tabs.first { !it.isHidden() })
    }

    override fun onClosed(next_page: AppPage?) {
        external_load_error = null
    }

    private fun setCurrentTab(tab: LibrarySubPage) {
        show_search_field = false
        search_filter = null
        show_sort_type_menu = false
        sort_type = tab.getDefaultSortType()
        reverse_sort = false

        current_tab = tab
    }

    @Composable
    override fun showTopBar(): Boolean = true

    @Composable
    override fun showTopBarContent(): Boolean = true

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun TopBarContent(modifier: Modifier, close: () -> Unit) {
        val player = LocalPlayerState.current

        MediaItemSortType.SelectionMenu(
            show_sort_type_menu,
            sort_type,
            { show_sort_type_menu = false },
            {
                if (it == sort_type) {
                    reverse_sort = !reverse_sort
                }
                else {
                    sort_type = it
                }
            },
            current_tab.nativeSortTypeLabel()
        )

        Row(
            modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val keyboard_controller = LocalSoftwareKeyboardController.current

            AnimatedVisibility(current_tab.enableSearch()) {
                Crossfade(show_search_field) { searching ->
                    PlatformClickableIconButton(
                        onClick = {
                            if (searching) {
                                keyboard_controller?.hide()
                            }
                            show_search_field = !searching
                            search_filter = null
                        },
                        onAltClick = {
                            if (!searching) {
                                player.openAppPage(player.app_page_state.Search)
                                player.context.vibrateShort()
                            }
                        }
                    ) {
                        Icon(
                            if (searching) Icons.Default.Close else Icons.Default.Search,
                            null
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth().weight(1f)) {
                AnimatedVisibility(
                    show_search_field && current_tab.enableSearch(),
                    enter = fadeIn() + expandHorizontally(clip = false)
                ) {
                    ResizableOutlinedTextField(
                        search_filter ?: "",
                        { search_filter = it },
                        Modifier.height(45.dp).fillMaxWidth().weight(1f),
                        singleLine = true
                    )
                }

                Row(Modifier.fillMaxWidth().weight(1f)) {
                    val shown_tabs = tabs.filter { !it.isHidden() }

                    for (tab in shown_tabs.withIndex()) {
                        Crossfade(tab.value == current_tab) { selected ->
                            Box(
                                Modifier
                                    .fillMaxWidth(
                                        1f / (shown_tabs.size - tab.index)
                                    )
                                    .padding(horizontal = 5.dp)
                            ) {
                                ElevatedFilterChip(
                                    selected,
                                    {
                                        setCurrentTab(tab.value)
                                    },
                                    {
                                        Box(Modifier.fillMaxWidth().padding(end = 8.dp), contentAlignment = Alignment.Center) {
                                            Icon(tab.value.getIcon(), null, Modifier.requiredSizeIn(minWidth = 20.dp, minHeight = 20.dp))
                                        }
                                    },
                                    colors = with(Theme) {
                                        FilterChipDefaults.elevatedFilterChipColors(
                                            containerColor = background,
                                            labelColor = on_background,
                                            selectedContainerColor = accent,
                                            selectedLabelColor = on_accent
                                        )
                                    },
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = Theme.on_background
                                    )
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(current_tab.enableSorting()) {
                IconButton({
                    show_sort_type_menu = !show_sort_type_menu
                }) {
                    Icon(Icons.Default.Sort, null)
                }
            }
        }
    }

    @Composable
    override fun ColumnScope.Page(
        multiselect_context: MediaItemMultiSelectContext,
        modifier: Modifier,
        content_padding: PaddingValues,
        close: () -> Unit
    ) {
        val player = LocalPlayerState.current

        AnimatedVisibility(
            external_load_error != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            var error: Throwable? by remember { mutableStateOf(external_load_error) }
            LaunchedEffect(external_load_error) {
                if (external_load_error != null) {
                    error = external_load_error
                }
            }

            error?.also {
                ErrorInfoDisplay(
                    it,
                    modifier = Modifier.padding(content_padding.copy(bottom = 20.dp)),
                    message = getString("error_yt_feed_parse_failed"),
                    onRetry = {
                        player.app_page_state.SongFeed.retrying = true
                        player.openAppPage(player.app_page_state.SongFeed)
                    },
                    onDismiss = {
                        external_load_error = null
                    },
                    disable_parent_scroll = false
                )
            }
        }

        Crossfade(current_tab, modifier) { tab ->
            tab.Page(
                this@LibraryAppPage,
                content_padding.copy(top = if (external_load_error != null) 0.dp else null),
                multiselect_context,
                Modifier.fillMaxSize()
            )
        }
    }
}
