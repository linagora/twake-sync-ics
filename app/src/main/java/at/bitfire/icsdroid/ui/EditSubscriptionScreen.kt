/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import at.bitfire.icsdroid.db.entity.Subscription

@Composable
fun EditSubscriptionScreen(
    subscriptionId: Long,
    onFinished: () -> Unit,
    model: SubscriptionsModel = viewModel()
) {
    val subscriptionDetails = rememberSaveable {
        mutableStateOf(SubscriptionDetailsState(
            name = "Test",
            url = "https://example.com"
        ))
    }

    Column {
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