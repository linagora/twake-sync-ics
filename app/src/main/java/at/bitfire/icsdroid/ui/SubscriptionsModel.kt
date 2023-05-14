/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import at.bitfire.icsdroid.SyncWorker
import at.bitfire.icsdroid.db.AppDatabase
import at.bitfire.icsdroid.db.entity.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubscriptionsModel(application: Application) : AndroidViewModel(application) {

    /** whether there are running sync workers */
    val isRefreshing = SyncWorker.liveStatus(application).map { workInfos ->
        workInfos.any { it.state == WorkInfo.State.RUNNING }
    }

    private val db = AppDatabase.getInstance(application)
    private val subscriptionsDao = db.subscriptionsDao()

    /** LiveData watching the subscriptions */
    val subscriptions = subscriptionsDao.getAllLive()

    fun create(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        subscriptionsDao.add(subscription)
    }

    fun get(id: Long): LiveData<Subscription> =
        subscriptionsDao.getLiveById(id)

    fun update(subscription: Subscription) = viewModelScope.launch(Dispatchers.IO) {
        subscriptionsDao.update(subscription)
    }

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