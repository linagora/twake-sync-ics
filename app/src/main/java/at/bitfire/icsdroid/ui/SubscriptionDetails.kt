/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Composable
fun SubscriptionDetails(details: SubscriptionDetailsState, onDetailsChanged: (SubscriptionDetailsState) -> Unit) {
    Column {
        Row {
            Box(Modifier
                .width(40.dp)
                .height(40.dp)
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color(details.color))
                .clickable {
                    // TODO color picker
                    val newColor = 0xFF000000.toInt() + Random.nextInt().and(0xFFFFFF)
                    onDetailsChanged(details.copy(color = newColor))
                })

            Column(Modifier.weight(1f)) {
                Text("Name")
                TextField(details.name, onValueChange = { newName ->
                    onDetailsChanged(details.copy(name = newName))
                })
            }
        }

        Text("URL")
        TextField(details.url, onValueChange = { newURL ->
            onDetailsChanged(details.copy(url = newURL))
        })
    }
}

@Parcelize
data class SubscriptionDetailsState(
    val name: String = "",
    val url: String = "",
    val color: Int = Color.LightGray.value.toInt()
) : Parcelable