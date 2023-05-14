/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

@file:OptIn(ExperimentalMaterial3Api::class)

package at.bitfire.icsdroid.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.compose.viewModel
import at.bitfire.icsdroid.R
import at.bitfire.icsdroid.db.entity.Subscription

@Composable
fun EditSubscriptionScreen(
    subscriptionId: Long,
    onNavigateUp: () -> Unit,
    onFinished: () -> Unit,
    model: SubscriptionsModel = viewModel()
) {
    val dbState = model.get(subscriptionId)
        .map { loaded ->
            SubscriptionDetailsState(
                name = loaded?.displayName ?: "",
                url = loaded?.url?.toString() ?: "",
                color = loaded?.color ?: 0
            )
        }.observeAsState()

    // derive SubscriptionDetailsState from dbState, cache key = dbState.value
    val subscriptionDetails = remember(dbState.value) {
        mutableStateOf(dbState.value ?: SubscriptionDetailsState())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateUp()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = { Text(stringResource(R.string.activity_edit_calendar)) },
                actions = {
                    IconButton(onClick = {
                        model.delete(subscriptionId)
                        onFinished()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.edit_calendar_delete))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            SubscriptionDetails(subscriptionDetails.value, onDetailsChanged = { newState ->
                subscriptionDetails.value = newState
            })

            Button(onClick = {
                // TODO validation
                model.update(
                    Subscription(
                        id = subscriptionId,
                        displayName = subscriptionDetails.value.name,
                        url = Uri.parse(subscriptionDetails.value.url),
                        color = subscriptionDetails.value.color.toInt()
                    )
                )

                onFinished()
            }) {
                Text("Save subscription")
            }

        }
    }

}