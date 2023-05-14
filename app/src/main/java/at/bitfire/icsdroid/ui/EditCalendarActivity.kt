/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import at.bitfire.icsdroid.ui.theme.MainTheme

class EditCalendarActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_SUBSCRIPTION_ID = "subscriptionId"
        const val EXTRA_ERROR_MESSAGE = "errorMessage"
        const val EXTRA_THROWABLE = "errorThrowable"
    }


    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        setContent {
            MainTheme {
                val subscriptionId = intent.getLongExtra(EXTRA_SUBSCRIPTION_ID, -1)
                EditSubscriptionScreen(
                    subscriptionId,
                    onNavigateUp = { onNavigateUp() },
                    onFinished = { finish() }
                )
            }
        }
    }

}