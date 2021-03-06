package com.mathewsachin.fategrandautomata.ui.prefs

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.root.RootScreenshotService
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var storageProvider: StorageProvider

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        findPreference<Preference>(getString(R.string.pref_nav_fine_tune))?.let {
            it.setOnPreferenceClickListener {
                val action = MoreSettingsFragmentDirections
                    .actionMoreSettingsFragmentToFineTuneSettingsFragment()

                nav(action)

                true
            }
        }

        findPreference<ListPreference>(getString(R.string.pref_boost_item))?.apply {
            entries = listOf(R.string.p_boost_item_disabled, R.string.p_boost_item_skip)
                .map { context.getString(it) }
                .toTypedArray() +
                    (1..3).map {
                        context.getString(R.string.p_boost_item_number, it)
                    }

            entryValues = (-1..3)
                .map { it.toString() }
                .toTypedArray()
        }

        findPreference<Preference>(getString(R.string.pref_nav_storage))?.let {
            it.setOnPreferenceClickListener {
                pickDir.launch(Uri.EMPTY)

                true
            }

            it.summary = storageProvider.rootDirName
        }
    }

    private val pickDir = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUrl ->
        if (dirUrl != null) {
            storageProvider.setRoot(dirUrl)

            findPreference<Preference>(getString(R.string.pref_nav_storage))
                ?.summary = storageProvider.rootDirName
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: MainSettingsViewModel by activityViewModels()

        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_record_screen))?.let {
            vm.useRootForScreenshots.observe(viewLifecycleOwner) { root ->
                it.isEnabled = !root

                if (root) {
                    it.isChecked = false
                }
            }
        }
    }
}
