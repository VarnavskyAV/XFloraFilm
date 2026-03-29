package com.alaka_ala.florafilm.ui.fragments.settings;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentSettingsBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.utils.appUpdate.AppUpdateManager;
import com.alaka_ala.florafilm.utils.appUpdate.models.UpdateInfo;
import com.alaka_ala.florafilm.utils.preferences.AppPreferences;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private AppUpdateManager appUpdateManager;

    private Button checkForUpdateButton;
    private TextView versionInfoTextView;
    private ProgressBar updateProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUpdateManager = AppUpdateManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        // скрываем нижнюю навигацию при выходе из этого фрагмента.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appUpdate(view);
        torrentView(view);
        cdnInit(view);

    }

    private void cdnInit(View view) {
        // LUMEX
        MaterialCheckBox lumex = view.findViewById(R.id.checkBoxLumex);
        if (lumex != null) {
            lumex.setChecked(AppPreferences.CDNSettings.Lumex.isLumexActive(getContext()));
            lumex.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AppPreferences.CDNSettings.Lumex.enableLumex(getContext());
                } else {
                    AppPreferences.CDNSettings.Lumex.disableLumex(getContext());
                }
            });
        }

        // HDVB
        MaterialCheckBox hdvb = view.findViewById(R.id.checkBoxHDVB);
        if (hdvb != null) {
            hdvb.setChecked(AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext()));
            hdvb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AppPreferences.CDNSettings.HDVB.enableHDVB(getContext());
                } else {
                    AppPreferences.CDNSettings.HDVB.disableHDVB(getContext());
                }
            });
        }

    }

    private void torrentView(View view) {
        MaterialSwitch switchTorrentView = view.findViewById(R.id.switchTorrentView);
        switchTorrentView.setChecked(AppPreferences.ViewTorrent.isViewTorrent(getContext()));
        switchTorrentView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                AppPreferences.ViewTorrent.setViewTorrent(getContext(), isChecked);
            }
        });
    }


    private void appUpdate(@NonNull View view) {
        // Находим View из подключенного layout
        checkForUpdateButton = view.findViewById(R.id.check_for_update_button);
        versionInfoTextView = view.findViewById(R.id.version_info);
        updateProgressBar = view.findViewById(R.id.update_progress_bar);

        setInitialVersionInfo();

        checkForUpdateButton.setOnClickListener(v -> {
            updateProgressBar.setVisibility(View.VISIBLE);
            checkForUpdateButton.setEnabled(false);
            checkForUpdate();
        });
    }

    /**
     * Устанавливает информацию о текущей версии приложения в TextView.
     */
    private void setInitialVersionInfo() {
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            String version = pInfo.versionName;
            versionInfoTextView.setText("Текущая версия: " + version);
        } catch (Exception e) {
            versionInfoTextView.setText("Текущая версия: N/A");
        }
    }

    /**
     * Запускает проверку наличия обновлений с помощью AppUpdateManager.
     * Обрабатывает результат с помощью UpdateCheckCallback.
     */
    private void checkForUpdate() {
        appUpdateManager.checkForUpdate(new AppUpdateManager.UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(UpdateInfo updateInfo) {
                requireActivity().runOnUiThread(() -> {
                    updateProgressBar.setVisibility(View.INVISIBLE);
                    checkForUpdateButton.setEnabled(true);
                    showUpdateDialog(updateInfo);
                });
            }

            @Override
            public void onNoUpdateAvailable() {
                requireActivity().runOnUiThread(() -> {
                    updateProgressBar.setVisibility(View.INVISIBLE);
                    checkForUpdateButton.setEnabled(true);
                    Toast.makeText(getContext(), "У вас установлена последняя версия.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    updateProgressBar.setVisibility(View.INVISIBLE);
                    checkForUpdateButton.setEnabled(true);
                    Toast.makeText(getContext(), "Ошибка: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Показывает диалоговое окно с информацией о доступном обновлении.
     * Предлагает пользователю скачать и установить новую версию.
     * @param updateInfo Информация о доступном обновлении.
     */
    private void showUpdateDialog(UpdateInfo updateInfo) {
        new AlertDialog.Builder(getContext())
                .setTitle("Доступно обновление")
                .setMessage("Новая версия: " + updateInfo.getVersionName() + "\n" +
                            "Размер: " + String.format("%.2f", updateInfo.getFileSize() / (1024.0 * 1024.0)) + " MB" + "\n\n" +
                            "Хотите скачать и установить?")
                .setPositiveButton("Скачать", (dialog, which) -> {
                    Toast.makeText(getContext(), "Началась загрузка обновления...", Toast.LENGTH_SHORT).show();
                    appUpdateManager.downloadUpdate(updateInfo);
                })
                .setNegativeButton("Позже", null)
                .show();
    }

    /**
     * Вызывается при уничтожении View фрагмента.
     * Обнуляем binding, чтобы избежать утечек памяти.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Вызывается при уничтожении фрагмента.
     * Очищаем ресурсы AppUpdateManager, чтобы избежать утечек BroadcastReceiver.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (appUpdateManager != null) {
            appUpdateManager.cleanup();
        }
    }
}