package com.riccardograziosi.slimlauncher.ui.main

import android.content.*
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.riccardograziosi.slimlauncher.R
import com.riccardograziosi.slimlauncher.adapters.HomeAdapter
import com.riccardograziosi.slimlauncher.databinding.HomeFragmentBinding
import com.riccardograziosi.slimlauncher.models.HomeApp
import com.riccardograziosi.slimlauncher.models.MainViewModel
import com.riccardograziosi.slimlauncher.utils.BaseFragment
import com.riccardograziosi.slimlauncher.utils.OnLaunchAppListener
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment : BaseFragment(), OnLaunchAppListener {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var receiver: BroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        val adapter1 = HomeAdapter(this)
        val adapter2 = HomeAdapter(this)
        binding!!.homeFragmentList.adapter = adapter1
        binding!!.homeFragmentListExp.adapter = adapter2

        viewModel.apps.observe(viewLifecycleOwner) { list ->
            list?.let { apps ->
                adapter1.setItems(apps.filter {
                    it.sortingIndex < 4
                })
                adapter2.setItems(apps.filter {
                    it.sortingIndex >= 4
                })
            }
        }

        setEventListeners()

        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        receiver = ClockReceiver()
        activity?.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun getFragmentView(): ViewGroup = binding!!.root

    override fun onResume() {
        super.onResume()
        updateClock()
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(receiver)
    }

    private fun setEventListeners() {
        binding!!.homeFragmentTime.setOnClickListener {
            try {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                launchActivity(it, intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                // Do nothing, we've failed :(
            }
        }

        binding!!.homeFragmentDate.setOnClickListener {
            try {
                val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                val intent = Intent(Intent.ACTION_VIEW, builder.build())
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                launchActivity(it, intent)
            } catch (e: ActivityNotFoundException) {
                // Do nothing, we've failed :(
            }
        }

        binding!!.homeFragmentCall .setOnClickListener { view ->
            try {
                val pm = context?.packageManager!!
                val intent = Intent(Intent.ACTION_DIAL)
                val componentName = intent.resolveActivity(pm)
                if (componentName == null) launchActivity(view, intent) else
                    pm.getLaunchIntentForPackage(componentName.packageName)?.let {
                        launchActivity(view, it)
                    } ?: run { launchActivity(view, intent) }
            } catch (e: Exception) {
                // Do nothing
            }
        }

        binding!!.homeFragmentCamera .setOnClickListener {
            try {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                launchActivity(it, intent)
            } catch (e: Exception) {
                // Do nothing
            }
        }

        binding!!.homeFragmentApps.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_openAppFragment))
        binding!!.homeFragmentOptions.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_optionsFragment))
    }

    fun updateClock() {
        val active =
            context?.getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
                ?.getInt(getString(R.string.prefs_settings_key_time_format), 0)
        val date = Date()

        val fWatchTime = when (active) {
            1 -> SimpleDateFormat("H:mm", Locale.ROOT)
            2 -> SimpleDateFormat("h:mm aa", Locale.ROOT)
            else -> DateFormat.getTimeInstance(DateFormat.SHORT)
        }
        binding!!.homeFragmentTime .text = fWatchTime.format(date)


        val fWatchDate = SimpleDateFormat("EEE, MMM dd", Locale.ROOT)
        binding!!.homeFragmentDate .text = fWatchDate.format(date)
    }

    override fun onLaunch(app: HomeApp, view: View) {
        try {
            val manager = requireContext().getSystemService(Context.USER_SERVICE) as UserManager
            val launcher =
                requireContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

            val componentName = ComponentName(app.packageName, app.activityName)
            val userHandle = manager.getUserForSerialNumber(app.userSerial)

            launcher.startMainActivity(componentName, userHandle, view.clipBounds, null)
        } catch (e: Exception) {
            // Do no shit yet
        }
    }

    override fun onBack(): Boolean {
        binding!!.root.transitionToStart()
        return true
    }

    override fun onHome() {
        binding!!.root.transitionToEnd()
    }

    inner class ClockReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            updateClock()
        }
    }
}
