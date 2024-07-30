// Copyright 2024 Mandarine Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.activities

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.features.settings.model.AbstractSetting
import io.github.mandarine3ds.mandarine.features.settings.model.BooleanSetting
import io.github.mandarine3ds.mandarine.features.settings.model.IntSetting
import io.github.mandarine3ds.mandarine.features.settings.model.view.SettingsItem
import io.github.mandarine3ds.mandarine.features.settings.model.view.SingleChoiceSetting
import io.github.mandarine3ds.mandarine.features.settings.model.view.SliderSetting
import io.github.mandarine3ds.mandarine.features.settings.model.view.SwitchSetting
import io.github.mandarine3ds.mandarine.features.settings.ui.SettingsActivityView
import io.github.mandarine3ds.mandarine.features.settings.ui.SettingsAdapter
import io.github.mandarine3ds.mandarine.features.settings.ui.SettingsFragmentView

class QuickSettingsDialog : DialogFragment(), SettingsFragmentView {
    companion object {
        const val TAG = "QuickSettingsDialog"
        fun newInstance(): QuickSettingsDialog {
            return QuickSettingsDialog()
        }
    }

    private lateinit var adapter: SettingsAdapter
    private lateinit var settingsList: ArrayList<SettingsItem>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val contents = requireActivity().layoutInflater.inflate(R.layout.dialog_quick_settings, null) as ViewGroup
        val recyclerView = contents.findViewById<RecyclerView>(R.id.list_quick_settings)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        adapter = SettingsAdapter(this, requireContext())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        builder.setView(contents)
        loadSettingsList()
        showSettingsList(settingsList)
        return builder.create()
    }

    override fun showSettingsList(settingsList: ArrayList<SettingsItem>) {
        adapter.setSettingsList(settingsList)
    }

    override fun loadSettingsList() {
        val sl = ArrayList<SettingsItem>()
        sl.apply {
            add(
                SwitchSetting(
                    BooleanSetting.EXPAND_TO_CUTOUT_AREA,
                    R.string.expand_to_cutout_area,
                    R.string.expand_to_cutout_area_description,
                    BooleanSetting.EXPAND_TO_CUTOUT_AREA.key,
                    BooleanSetting.EXPAND_TO_CUTOUT_AREA.defaultValue
                )
            )
            add(
                SingleChoiceSetting(
                    IntSetting.FRAME_SKIP,
                    R.string.frame_skip,
                    R.string.frame_skip_description,
                    R.array.frameSkipNames,
                    R.array.frameSkipValues,
                    IntSetting.FRAME_SKIP.key,
                    IntSetting.FRAME_SKIP.defaultValue
                )
            )
            add(
                SwitchSetting(
                    IntSetting.ENABLE_CUSTOM_CPU_TICKS,
                    R.string.enable_custom_cpu_ticks,
                    R.string.enable_custom_cpu_ticks_description,
                    IntSetting.ENABLE_CUSTOM_CPU_TICKS.key,
                    IntSetting.ENABLE_CUSTOM_CPU_TICKS.defaultValue
                )
            )
            add(
                SliderSetting(
                    IntSetting.CUSTOM_CPU_TICKS,
                    R.string.custom_cpu_ticks,
                    0,
                    77,
                    65535,
                    "",
                    IntSetting.CUSTOM_CPU_TICKS.key,
                    IntSetting.CUSTOM_CPU_TICKS.defaultValue.toFloat()
                )
            )
            add(
                SwitchSetting(
                    IntSetting.USE_FRAME_LIMIT,
                    R.string.frame_limit_enable,
                    R.string.frame_limit_enable_description,
                    IntSetting.USE_FRAME_LIMIT.key,
                    IntSetting.USE_FRAME_LIMIT.defaultValue
                )
            )
            add(
                SliderSetting(
                    IntSetting.FRAME_LIMIT,
                    R.string.frame_limit_slider,
                    R.string.frame_limit_slider_description,
                    1,
                    200,
                    "%",
                    IntSetting.FRAME_LIMIT.key,
                    IntSetting.FRAME_LIMIT.defaultValue.toFloat()
                )
            )
        }
        settingsList = sl
    }

    override val activityView: SettingsActivityView? = null

    override fun loadSubMenu(menuKey: String) {}

    override fun showToastMessage(message: String?, is_long: Boolean) {}

    override fun putSetting(setting: AbstractSetting) {}

    override fun onSettingChanged() {}
}
