/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.accompanist.themeadapter.material.MdcTheme

class AddCalendarActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_COLOR = "color"
    }

    //private val subscriptionSettingsModel by viewModels<SubscriptionSettingsFragment.SubscriptionSettingsModel>()


    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)

        setContent {
            MdcTheme {
                NewSubscriptionScreen(onFinished = {
                    finish()
                })
            }
        }

        /*
            intent?.apply {
                data?.let { uri ->
                    subscriptionSettingsModel.url.value = uri.toString()
                }
                getStringExtra(EXTRA_TITLE)?.let {
                    subscriptionSettingsModel.title.value = it
                }
                if (hasExtra(EXTRA_COLOR))
                    subscriptionSettingsModel.color.value = getIntExtra(EXTRA_COLOR, LocalCalendar.DEFAULT_COLOR)
            }

        }*/
    }

}
