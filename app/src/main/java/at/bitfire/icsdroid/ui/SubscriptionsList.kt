/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.bitfire.icsdroid.db.entity.Subscription

@Composable
fun SubscriptionsList(
    model: SubscriptionsModel = viewModel()
) {

    val subscriptionState = model.subscriptions.observeAsState()
    subscriptionState.value?.let { subscriptions ->
        for (subscription in subscriptions)
            SubscriptionCard(subscription)
    }

}

@Composable
fun SubscriptionCard(subscription: Subscription) {
    val context = LocalContext.current
    Card(Modifier.padding(8.dp).fillMaxWidth().clickable {
        val intent = Intent(context, EditCalendarActivity::class.java)
        intent.putExtra(EditCalendarActivity.EXTRA_SUBSCRIPTION_ID, subscription.id)
        context.startActivity(intent)
    }) {
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