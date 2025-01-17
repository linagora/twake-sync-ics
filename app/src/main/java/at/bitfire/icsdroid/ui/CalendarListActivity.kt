/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.WorkInfo
import at.bitfire.icsdroid.*
import at.bitfire.icsdroid.databinding.CalendarListActivityBinding
import at.bitfire.icsdroid.databinding.CalendarListItemBinding
import at.bitfire.icsdroid.db.AppDatabase
import at.bitfire.icsdroid.db.entity.Subscription
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.util.*

class CalendarListActivity: AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        /**
         * Set this extra to request calendar permission when the activity starts.
         */
        const val EXTRA_REQUEST_CALENDAR_PERMISSION = "permission"

        const val PRIVACY_POLICY_URL = "https://icsx5.bitfire.at/privacy/"
    }

    private val model by viewModels<SubscriptionsModel>()
    private lateinit var binding: CalendarListActivityBinding

    /** Stores the calendar permission request for asking for calendar permissions during runtime */
    private lateinit var requestCalendarPermissions: () -> Unit

    /** Stores the post notification permission request for asking for permissions during runtime */
    private lateinit var requestNotificationPermission: () -> Unit

    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_activity_calendar_list)

        // Register the calendar permission request
        requestCalendarPermissions = PermissionUtils.registerCalendarPermissionRequest(this) {
            SyncWorker.run(this)
        }

        // Register the notifications permission request
        requestNotificationPermission = PermissionUtils.registerNotificationPermissionRequest(this)

        binding = DataBindingUtil.setContentView(this, R.layout.calendar_list_activity)
        binding.lifecycleOwner = this
        binding.model = model

        val defaultRefreshColor = ContextCompat.getColor(this, R.color.lightblue)
        binding.refresh.setColorSchemeColors(defaultRefreshColor)
        binding.refresh.setOnRefreshListener(this)
        binding.refresh.setSize(SwipeRefreshLayout.LARGE)

        // show whether sync is running
        model.isRefreshing.observe(this) { isRefreshing ->
            binding.refresh.isRefreshing = isRefreshing
        }

        // calendars
        val subscriptionAdapter = SubscriptionListAdapter(this)
        subscriptionAdapter.clickListener = { calendar ->
            val intent = Intent(this, EditCalendarActivity::class.java)
            intent.putExtra(EditCalendarActivity.EXTRA_SUBSCRIPTION_ID, calendar.id)
            startActivity(intent)
        }
        binding.calendarList.adapter = subscriptionAdapter

        binding.fab.setOnClickListener {
            onAddCalendar()
        }

        // If EXTRA_PERMISSION is true, request the calendar permissions
        val requestPermissions = intent.getBooleanExtra(EXTRA_REQUEST_CALENDAR_PERMISSION, false)
        if (requestPermissions && !PermissionUtils.haveCalendarPermissions(this))
            requestCalendarPermissions()

        model.subscriptions.observe(this) { subscriptions ->
            subscriptionAdapter.submitList(subscriptions)

            val colors = mutableSetOf<Int>()
            colors += defaultRefreshColor
            colors.addAll(subscriptions.mapNotNull { it.color })
            binding.refresh.setColorSchemeColors(*colors.toIntArray())
        }

        // startup fragments
        if (savedInstanceState == null)
            ServiceLoader
                .load(StartupFragment::class.java)
                .forEach { it.initialize(this) }

        // check sync settings when sync interval has been edited
        supportFragmentManager.registerFragmentLifecycleCallbacks(object: FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f is SyncIntervalDialogFragment)
                    checkSyncSettings()
            }
        }, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_calendar_list, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.force_dark_mode).isChecked = Settings(this).forceDarkMode()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        checkSyncSettings()
    }


    @SuppressLint("ShowToast")
    private fun checkSyncSettings() {
        snackBar?.dismiss()
        snackBar = null

        when {
            // notification permissions are granted
            !PermissionUtils.haveNotificationPermission(this) -> {
                snackBar = Snackbar.make(binding.coordinator, R.string.notification_permissions_required, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.permissions_grant) { requestNotificationPermission() }
                    .also { it.show() }
            }

            // calendar permissions are granted
            !PermissionUtils.haveCalendarPermissions(this) -> {
                snackBar = Snackbar.make(binding.coordinator, R.string.calendar_permissions_required, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.permissions_grant) { requestCalendarPermissions() }
                    .also { it.show() }
            }

            // periodic sync enabled AND Android >= 6 AND not whitelisted from battery saving AND sync interval < 1 day
            Build.VERSION.SDK_INT >= 23 &&
                    !(getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID) &&
                    AppAccount.syncInterval(this) < 86400 -> {
                snackBar = Snackbar.make(binding.coordinator, R.string.calendar_list_battery_whitelist, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.calendar_list_battery_whitelist_settings) {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            startActivity(intent)
                        }.also {
                            it.show()
                        }
            }
        }
    }


    /* actions */

    fun onAddCalendar() {
        startActivity(Intent(this, AddCalendarActivity::class.java))
    }

    override fun onRefresh() {
        SyncWorker.run(this, true)
    }

    fun onRefreshRequested(item: MenuItem) {
        onRefresh()
    }

    fun onShowInfo(item: MenuItem) {
        startActivity(Intent(this, InfoActivity::class.java))
    }

    fun onSetSyncInterval(item: MenuItem) {
        SyncIntervalDialogFragment().show(supportFragmentManager, "sync_interval")
    }

    fun onToggleDarkMode(item: MenuItem) {
        val settings = Settings(this)
        val newMode = !settings.forceDarkMode()
        settings.forceDarkMode(newMode)
        AppCompatDelegate.setDefaultNightMode(
                if (newMode)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }

    fun onShowPrivacyPolicy(item: MenuItem) {
        UriUtils.launchUri(this, Uri.parse(PRIVACY_POLICY_URL))
    }


    class SubscriptionListAdapter(
        val context: Context
    ): ListAdapter<Subscription, SubscriptionListAdapter.ViewHolder>(object: DiffUtil.ItemCallback<Subscription>() {

        override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription) =
            // compare all displayed fields
            oldItem.url == newItem.url &&
            oldItem.displayName == newItem.displayName &&
            oldItem.lastSync == newItem.lastSync &&
            oldItem.color == newItem.color &&
            oldItem.errorMessage == newItem.errorMessage

    }) {

        class ViewHolder(val binding: CalendarListItemBinding): RecyclerView.ViewHolder(binding.root)

        var clickListener: ((Subscription) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            Log.i(Constants.TAG, "Creating view holder")
            val binding = CalendarListItemBinding.inflate(LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val subscription = currentList[position]

            holder.binding.root.setOnClickListener {
                clickListener?.let { listener ->
                    listener(subscription)
                }
            }

            holder.binding.apply {
                url.text = subscription.url.toString()
                title.text = subscription.displayName

                syncStatus.text = subscription.lastSync?.let { lastSync ->
                    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT)
                        .format(Date(lastSync))
                } ?: context.getString(R.string.calendar_list_not_synced_yet)

                subscription.color?.let {
                    color.setColor(it)
                }
            }

            val errorMessage = subscription.errorMessage
            if (errorMessage == null)
                holder.binding.errorMessage.visibility = View.GONE
            else {
                holder.binding.errorMessage.text = errorMessage
                holder.binding.errorMessage.visibility = View.VISIBLE
            }
        }

    }

    class SubscriptionsModel(application: Application): AndroidViewModel(application) {

        /** whether there are running sync workers */
        val isRefreshing = SyncWorker.liveStatus(application).map { workInfos ->
            workInfos.any { it.state == WorkInfo.State.RUNNING }
        }

        /** LiveData watching the subscriptions */
        val subscriptions = AppDatabase.getInstance(application)
            .subscriptionsDao()
            .getAllLive()

    }

}