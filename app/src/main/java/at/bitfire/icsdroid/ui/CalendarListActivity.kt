/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import at.bitfire.icsdroid.R
import at.bitfire.icsdroid.SyncWorker
import at.bitfire.icsdroid.UriUtils
import at.bitfire.icsdroid.ui.legacy.SyncIntervalDialogFragment
import com.google.accompanist.themeadapter.material.MdcTheme
import com.google.android.material.snackbar.Snackbar

class CalendarListActivity: AppCompatActivity() {

    companion object {
        /**
         * Set this extra to request calendar permission when the activity starts.
         */
        const val EXTRA_REQUEST_CALENDAR_PERMISSION = "permission"

        const val PRIVACY_POLICY_URL = "https://icsx5.bitfire.at/privacy/"
    }

    /** Stores the calendar permission request for asking for calendar permissions during runtime */
    private lateinit var requestCalendarPermissions: () -> Unit

    /** Stores the post notification permission request for asking for permissions during runtime */
    private lateinit var requestNotificationPermission: () -> Unit

    private var snackBar: Snackbar? = null

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_activity_calendar_list)

        /*// Register the calendar permission request
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
        }, false)*/

        setContent {
            MdcTheme {
                val refreshing = remember { mutableStateOf(false) }
                val refreshState = rememberPullRefreshState(refreshing.value, onRefresh = {
                    SyncWorker.run(this, true)
                })

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = { onAddCalendar() }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.activity_add_calendar))
                        }
                    },
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.title_activity_calendar_list)) }
                        )
                    },
                    modifier = Modifier.pullRefresh(refreshState)
                ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(paddingValues)
                    ) {
                        item {
                            SubscriptionsList()
                        }
                    }

                    PullRefreshIndicator(refreshing = refreshing.value, state = refreshState)
                }
            }
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
    }*/


    /* actions */

    fun onAddCalendar() {
        startActivity(Intent(this, AddCalendarActivity::class.java))
    }

    /*override fun onRefresh() {
        SyncWorker.run(this, true)
    }*/

    /*fun onRefreshRequested(item: MenuItem) {
        onRefresh()
    }*/

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

}