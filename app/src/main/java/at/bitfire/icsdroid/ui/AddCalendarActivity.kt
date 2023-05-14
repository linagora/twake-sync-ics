/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import at.bitfire.icsdroid.ui.theme.MainTheme
import kotlin.random.Random

class AddCalendarActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_COLOR = "color"
    }


    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)

        var initialUrl = "https://example.com"
        var initialTitle = "Example"
        var initialColor = 0xFF000000.toInt() + Random.nextInt().and(0xFFFFFF)
        intent?.apply {
            data?.let { uri ->
                initialUrl = uri.toString()
            }
            getStringExtra(EXTRA_TITLE)?.let {
                initialTitle = it
            }
            if (hasExtra(EXTRA_COLOR))
                initialColor = getIntExtra(EXTRA_COLOR, -1)
        }

        setContent {
            MainTheme {
                NewSubscriptionScreen(
                    SubscriptionDetailsState(
                        name = initialTitle,
                        url = initialUrl,
                        color = initialColor
                    ),
                    onNavigateUp = { onNavigateUp() },
                    onFinished = { finish() }
                )
            }
        }

    }

}
