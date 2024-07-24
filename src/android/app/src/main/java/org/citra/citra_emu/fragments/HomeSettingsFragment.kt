// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarin3ds.mandarin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import io.github.mandarin3ds.mandarin.CitraApplication
import io.github.mandarin3ds.mandarin.HomeNavigationDirections
import io.github.mandarin3ds.mandarin.R
import io.github.mandarin3ds.mandarin.adapters.HomeSettingAdapter
import io.github.mandarin3ds.mandarin.databinding.DialogSoftwareKeyboardBinding
import io.github.mandarin3ds.mandarin.databinding.FragmentHomeSettingsBinding
import io.github.mandarin3ds.mandarin.features.settings.model.Settings
import io.github.mandarin3ds.mandarin.features.settings.model.StringSetting
import io.github.mandarin3ds.mandarin.features.settings.ui.SettingsActivity
import io.github.mandarin3ds.mandarin.features.settings.utils.SettingsFile
import io.github.mandarin3ds.mandarin.model.Game
import io.github.mandarin3ds.mandarin.model.HomeSetting
import io.github.mandarin3ds.mandarin.ui.main.MainActivity
import io.github.mandarin3ds.mandarin.utils.GameHelper
import io.github.mandarin3ds.mandarin.utils.PermissionsHandler
import io.github.mandarin3ds.mandarin.viewmodel.HomeViewModel
import io.github.mandarin3ds.mandarin.utils.GpuDriverHelper
import io.github.mandarin3ds.mandarin.utils.Log
import io.github.mandarin3ds.mandarin.viewmodel.DriverViewModel

class HomeSettingsFragment : Fragment() {
    private var _binding: FragmentHomeSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val driverViewModel: DriverViewModel by activityViewModels()

    private val preferences get() =
        PreferenceManager.getDefaultSharedPreferences(CitraApplication.appContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainActivity = requireActivity() as MainActivity

        val optionsList = listOf(
            HomeSetting(
                R.string.grid_menu_core_settings,
                R.string.settings_description,
                R.drawable.ic_settings,
                { SettingsActivity.launch(requireContext(), SettingsFile.FILE_NAME_CONFIG, "") }
            ),
            HomeSetting(
                R.string.artic_base_connect,
                R.string.artic_base_connect_description,
                R.drawable.ic_network,
                {
                    val inflater = LayoutInflater.from(context)
                    val inputBinding = DialogSoftwareKeyboardBinding.inflate(inflater)
                    var textInputValue: String = preferences.getString("last_artic_base_addr", "")!!

                    inputBinding.editTextInput.setText(textInputValue)
                    inputBinding.editTextInput.doOnTextChanged { text, _, _, _ ->
                        textInputValue = text.toString()
                    }

                    val dialog = context?.let {
                        MaterialAlertDialogBuilder(it)
                            .setView(inputBinding.root)
                            .setTitle(getString(R.string.artic_base_enter_address))
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                if (textInputValue.isNotEmpty()) {
                                    preferences.edit()
                                        .putString("last_artic_base_addr", textInputValue)
                                        .apply()
                                    val menu = Game(
                                        title = getString(R.string.artic_base),
                                        path = "articbase://$textInputValue",
                                        filename = ""
                                    )
                                    val action =
                                        HomeNavigationDirections.actionGlobalEmulationActivity(menu)
                                    binding.root.findNavController().navigate(action)
                                }
                            }
                            .setNegativeButton(android.R.string.cancel) {_, _ -> }
                            .show()
                    }
                }
            ),
            HomeSetting(
                R.string.system_files,
                R.string.system_files_description,
                R.drawable.ic_system_update,
                {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    parentFragmentManager.primaryNavigationFragment?.findNavController()
                        ?.navigate(R.id.action_homeSettingsFragment_to_systemFilesFragment)
                }
            ),
            HomeSetting(
                R.string.install_game_content,
                R.string.install_game_content_description,
                R.drawable.ic_install,
                { mainActivity.ciaFileInstaller.launch(true) }
            ),
            HomeSetting(
                R.string.share_log,
                R.string.share_log_description,
                R.drawable.ic_share,
                { shareLog() }
            ),
            HomeSetting(
                R.string.gpu_driver_manager,
                R.string.install_gpu_driver_description,
                R.drawable.ic_install_driver,
                {
                    binding.root.findNavController()
                        .navigate(R.id.action_homeSettingsFragment_to_driverManagerFragment)
                },
                { GpuDriverHelper.supportsCustomDriverLoading() },
                R.string.custom_driver_not_supported,
                R.string.custom_driver_not_supported_description,
                driverViewModel.selectedDriverMetadata
            ),
            HomeSetting(
                R.string.select_mandarin_user_folder,
                R.string.select_mandarin_user_folder_home_description,
                R.drawable.ic_home,
                { mainActivity.openCitraDirectory.launch(null) },
                details = homeViewModel.userDir
            ),
            HomeSetting(
                R.string.select_games_folder,
                R.string.select_games_folder_description,
                R.drawable.ic_add,
                { getGamesDirectory.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).data) },
                details = homeViewModel.gamesDir
            ),
            HomeSetting(
                R.string.preferences_theme,
                R.string.theme_and_color_description,
                R.drawable.ic_palette,
                { SettingsActivity.launch(requireContext(), Settings.SECTION_THEME, "") }
            ),
            HomeSetting(
                R.string.about,
                R.string.about_description,
                R.drawable.ic_info_outline,
                {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    parentFragmentManager.primaryNavigationFragment?.findNavController()
                        ?.navigate(R.id.action_homeSettingsFragment_to_aboutFragment)
                }
            )
        )

        binding.homeSettingsList.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                resources.getInteger(R.integer.game_grid_columns)
            )
            adapter = HomeSettingAdapter(
                requireActivity() as AppCompatActivity,
                viewLifecycleOwner,
                optionsList
            )
        }

        setInsets()
    }

    override fun onStart() {
        super.onStart()
        exitTransition = null
        homeViewModel.setNavigationVisibility(visible = true, animated = true)
        homeViewModel.setStatusBarShadeVisibility(visible = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val getGamesDirectory =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { result ->
            if (result == null) {
                return@registerForActivityResult
            }

            requireContext().contentResolver.takePersistableUriPermission(
                result,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // When a new directory is picked, we currently will reset the existing games
            // database. This effectively means that only one game directory is supported.
            preferences.edit()
                .putString(GameHelper.KEY_GAME_PATH, result.toString())
                .apply()

            Toast.makeText(
                CitraApplication.appContext,
                R.string.games_dir_selected,
                Toast.LENGTH_LONG
            ).show()

            homeViewModel.setGamesDir(requireActivity(), result.path!!)
        }

    private fun shareLog() {
        val logDirectory = DocumentFile.fromTreeUri(
            requireContext(),
            PermissionsHandler.mandarinDirectory
        )?.findFile("log")
        val currentLog = logDirectory?.findFile("mandarin_log.txt")
        val oldLog = logDirectory?.findFile("mandarin_log.txt.old.txt")

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
        }
        if (!Log.gameLaunched && oldLog?.exists() == true) {
            intent.putExtra(Intent.EXTRA_STREAM, oldLog.uri)
            startActivity(Intent.createChooser(intent, getText(R.string.share_log)))
        } else if (currentLog?.exists() == true) {
            intent.putExtra(Intent.EXTRA_STREAM, currentLog.uri)
            startActivity(Intent.createChooser(intent, getText(R.string.share_log)))
        } else {
            Toast.makeText(
                requireContext(),
                getText(R.string.share_log_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setInsets() =
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { view: View, windowInsets: WindowInsetsCompat ->
            val barInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val spacingNavigation = resources.getDimensionPixelSize(R.dimen.spacing_navigation)
            val spacingNavigationRail =
                resources.getDimensionPixelSize(R.dimen.spacing_navigation_rail)

            val leftInsets = barInsets.left + cutoutInsets.left
            val rightInsets = barInsets.right + cutoutInsets.right

            binding.scrollViewSettings.updatePadding(
                top = barInsets.top,
                bottom = barInsets.bottom
            )

            val mlpScrollSettings = binding.scrollViewSettings.layoutParams as MarginLayoutParams
            mlpScrollSettings.leftMargin = leftInsets
            mlpScrollSettings.rightMargin = rightInsets
            binding.scrollViewSettings.layoutParams = mlpScrollSettings

            binding.linearLayoutSettings.updatePadding(bottom = spacingNavigation)

            if (ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                binding.linearLayoutSettings.updatePadding(left = spacingNavigationRail)
            } else {
                binding.linearLayoutSettings.updatePadding(right = spacingNavigationRail)
            }

            windowInsets
        }
}
