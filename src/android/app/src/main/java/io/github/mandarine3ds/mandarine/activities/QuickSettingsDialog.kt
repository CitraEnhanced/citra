// Copyright 2024 Mandarine Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.activities

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.features.settings.model.AbstractSetting
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

    override val activityView: SettingsActivityView? = null

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
                SingleChoiceSetting(
                    IntSetting.RESOLUTION_FACTOR,
                    R.string.internal_resolution,
                    0,
                    R.array.resolutionFactorNames,
                    R.array.resolutionFactorValues,
                    IntSetting.RESOLUTION_FACTOR.key,
                    IntSetting.RESOLUTION_FACTOR.defaultValue
                )
            )
            add(
                SwitchSetting(
                    IntSetting.ENABLE_CUSTOM_CPU_TICKS,
                    R.string.enable_custom_cpu_ticks,
                    0,
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
                    IntSetting.FORCE_HW_VERTEX_SHADERS,
                    R.string.force_hw_vertex_shaders,
                    0,
                    IntSetting.FORCE_HW_VERTEX_SHADERS.key,
                    IntSetting.FORCE_HW_VERTEX_SHADERS.defaultValue
                )
            )
            add(
                SwitchSetting(
                    IntSetting.DISABLE_SURFACE_TEXTURE_COPY,
                    R.string.disable_surface_texture_copy,
                    0,
                    IntSetting.DISABLE_SURFACE_TEXTURE_COPY.key,
                    IntSetting.DISABLE_SURFACE_TEXTURE_COPY.defaultValue
                )
            )
            add(
                SwitchSetting(
                    IntSetting.DISABLE_FLUSH_CPU_WRITE,
                    R.string.disable_flush_cpu_write,
                    0,
                    IntSetting.DISABLE_FLUSH_CPU_WRITE.key,
                    IntSetting.DISABLE_FLUSH_CPU_WRITE.defaultValue
                )
            )
            add(
                SliderSetting(
                    IntSetting.DELAY_RENDER_THREAD_US,
                    R.string.delay_render_thread,
                    0,
                    0,
                    16000,
                    " μs",
                    IntSetting.DELAY_RENDER_THREAD_US.key,
                    IntSetting.DELAY_RENDER_THREAD_US.defaultValue.toFloat()
                )
            )
        }
        settingsList = sl
    }

    override fun loadSubMenu(menuKey: String) {}

    override fun showToastMessage(message: String?, is_long: Boolean) {
        Toast.makeText(
            context,
            message,
            if (is_long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    override fun putSetting(setting: AbstractSetting) {}

    override fun onSettingChanged() {}
}
