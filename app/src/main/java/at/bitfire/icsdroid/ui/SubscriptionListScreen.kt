/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)

package at.bitfire.icsdroid.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import at.bitfire.icsdroid.R
import at.bitfire.icsdroid.SyncWorker

@Composable
fun SubscriptionListScreen(
    onAddCalendar: () -> Unit,
    onShowAbout: () -> Unit,
    model: SubscriptionsModel = viewModel()
) {
    val context = LocalContext.current

    val refreshing = model.isRefreshing.observeAsState(false)
    val refreshState = rememberPullRefreshState(refreshing.value, onRefresh = {
        SyncWorker.run(context, true)
    })

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddCalendar() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.activity_add_calendar))
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_activity_calendar_list)) },
                actions = {
                    IconButton(onClick = {
                        onShowAbout()
                    }) {
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                }
            )
        },
        modifier = Modifier.pullRefresh(refreshState)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(paddingValues)
        ) {
            item {
                SubscriptionsList()
            }
        }

        PullRefreshIndicator(refreshing = refreshing.value, state = refreshState)
    }
}