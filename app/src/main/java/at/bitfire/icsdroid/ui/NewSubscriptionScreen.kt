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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import at.bitfire.icsdroid.R
import at.bitfire.icsdroid.db.entity.Subscription

@Composable
fun NewSubscriptionScreen(
    initialSubscriptionDetailsState: SubscriptionDetailsState,
    onNavigateUp: () -> Unit,
    onFinished: () -> Unit,
    model: SubscriptionsModel = viewModel()
) {
    val subscriptionDetails = rememberSaveable {
        mutableStateOf(initialSubscriptionDetailsState)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onNavigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = { Text(stringResource(R.string.activity_add_calendar)) }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            SubscriptionDetails(
                subscriptionDetails.value,
                onDetailsChanged = { newState ->
                    subscriptionDetails.value = newState
                })

            Button(onClick = {
                // TODO validation
                model.create(Subscription(
                    displayName = subscriptionDetails.value.name,
                    url = Uri.parse(subscriptionDetails.value.url)
                ))

                onFinished()
            }) {
                Text("Create subscription")
            }
        }
    }
}