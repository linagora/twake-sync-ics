/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import at.bitfire.icsdroid.SyncWorker
import at.bitfire.icsdroid.db.AppDatabase
import at.bitfire.icsdroid.db.entity.Subscription

@Composable
fun SubscriptionsList(
    model: SubscriptionsModel = viewModel()
) {

    val subscriptionState = model.subscriptions.observeAsState()
    subscriptionState.value?.let { subscriptions ->
        for (subscription in subscriptions) {
            SubscriptionCard(subscription)
        }
    }

}

@Composable
fun SubscriptionCard(subscription: Subscription) {
    Card(Modifier.padding(8.dp).fillMaxWidth()) {
        Row(Modifier.padding(8.dp)) {
            Box(modifier = Modifier
                .padding(8.dp)
                .width(32.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(Color(subscription.color?.toULong() ?: Color.Red.value)))

            Column {
                Text(subscription.url.toString())

                Text(subscription.displayName)

                if (subscription.errorMessage != null)
                    Text(subscription.errorMessage)
            }
        }
    }
}


@Composable
@Preview
fun SubscriptionCard_Sample() {
    SubscriptionCard(
        Subscription(
            displayName = "Sample Subscription",
            url = Uri.parse("https://example.com")
        )
    )
}


class SubscriptionsModel(application: Application) : AndroidViewModel(application) {

    /** whether there are running sync workers */
    val isRefreshing = SyncWorker.liveStatus(application).map { workInfos ->
        workInfos.any { it.state == WorkInfo.State.RUNNING }
    }

    /** LiveData watching the subscriptions */
    val subscriptions = AppDatabase.getInstance(application)
        .subscriptionsDao()
        .getAllLive()

    init {
        /*viewModelScope.launch(Dispatchers.IO) {
            val subscription = Subscription(
                displayName = "Mäh",
                url = Uri.parse("https://www.wien.gv.at/amtshelfer/feiertage/ics/feiertage.ics")
            )

            /** A list of all the ids of the inserted rows */
            val id = AppDatabase.getInstance(application)
                .subscriptionsDao().add(subscription)
        }*/
    }

}