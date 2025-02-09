package com.riccardograziosi.slimlauncher.ui.options

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.riccardograziosi.slimlauncher.BuildConfig
import com.riccardograziosi.slimlauncher.adapters.AddAppAdapter
import com.riccardograziosi.slimlauncher.data.model.App
import com.riccardograziosi.slimlauncher.databinding.AddAppFragmentBinding
import com.riccardograziosi.slimlauncher.models.AddAppViewModel
import com.riccardograziosi.slimlauncher.utils.BaseFragment
import com.riccardograziosi.slimlauncher.utils.OnAppClickedListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class AddAppFragment : BaseFragment(), OnAppClickedListener {

    private var _binding: AddAppFragmentBinding? = null
    private val binding get() = _binding
    override fun getFragmentView(): ViewGroup = binding!!.root

    private  val viewModel: AddAppViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = AddAppFragmentBinding.inflate(inflater, container, false)
        val adapter = AddAppAdapter(this)

        binding!!.addAppFragmentList.adapter = adapter

        viewModel.apps.observe(viewLifecycleOwner) {
            it?.let { apps ->
                adapter.setItems(apps)
                binding!!.addAppFragmentProgressBar.visibility = View.GONE
            } ?: run {
               binding!!.addAppFragmentProgressBar.visibility = View.VISIBLE
            }
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.setInstalledApps(getInstalledApps())
        viewModel.filterApps("")
        binding!!.addAppFragmentEditText.addTextChangedListener(onTextChangeListener)
    }

    override fun onPause() {
        super.onPause()
        binding!!.addAppFragmentEditText.removeTextChangedListener(onTextChangeListener)
    }

    private val onTextChangeListener: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
            // Do nothing
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Do nothing
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewModel.filterApps(s.toString())
        }
    }

    override fun onAppClicked(app: App) {
        viewModel.addAppToHomeScreen(app)
        Navigation.findNavController(binding!!.root).popBackStack()
    }

    private fun getInstalledApps(): List<App> {
        val list = mutableListOf<App>()

        val manager = requireContext().getSystemService(Context.USER_SERVICE) as UserManager
        val launcher = requireContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val myUserHandle = Process.myUserHandle()

        for (profile in manager.userProfiles) {
            val prefix = if (profile.equals(myUserHandle)) "" else "\uD83C\uDD46 " //Unicode for boxed w
            val profileSerial = manager.getSerialNumberForUser(profile)

            for (activityInfo in launcher.getActivityList(null, profile)) {
                val app = App(
                        appName = prefix + activityInfo.label.toString(),
                        packageName = activityInfo.applicationInfo.packageName,
                        activityName = activityInfo.name,
                        userSerial = profileSerial
                )
                list.add(app)
            }
        }

        list.sortBy{it.appName}

        val filter = mutableListOf<String>()
        filter.add(BuildConfig.APPLICATION_ID)
        return list.filterNot { filter.contains(it.packageName) }
    }
}