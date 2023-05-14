/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.parcelize.Parcelize

@Composable
fun SubscriptionDetails(details: SubscriptionDetailsState, onDetailsChanged: (SubscriptionDetailsState) -> Unit) {
    Column(Modifier) {
        Text("Name")
        TextField(details.name, onValueChange = { newName ->
            onDetailsChanged(details.copy(name = newName))
        })

        Text("URL")
        TextField(details.url, onValueChange = { newURL ->
            onDetailsChanged(details.copy(url = newURL))
        })
    }
}

@Parcelize
data class SubscriptionDetailsState(
    val name: String,
    val url: String
) : Parcelable