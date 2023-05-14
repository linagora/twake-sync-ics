/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    val subscriptionDetails = rememberSaveable {
        mutableStateOf(SubscriptionDetailsState(
            name = "Test",
            url = "https://example.com"
        ))
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
        /*EditSubscriptionScreen(subscriptionId, onFinished = {
            //finish()
        }, modifier = Modifier.padding(paddingValues))*/

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
                        url = Uri.parse(subscriptionDetails.value.url)
                    )
                )

                onFinished()
            }) {
                Text("Save subscription")
            }

        }
    }

}