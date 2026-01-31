package android.zero.studio.layouteditor.fragments.resources;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.zero.studio.layouteditor.ProjectFile;
import android.zero.studio.layouteditor.R;
import android.zero.studio.layouteditor.adapters.ColorResourceAdapter;
import android.zero.studio.layouteditor.adapters.models.ValuesItem;
import android.zero.studio.layouteditor.databinding.FragmentResourcesBinding;
import android.zero.studio.layouteditor.databinding.LayoutValuesItemDialogBinding;
import android.zero.studio.layouteditor.managers.ProjectManager;
import android.zero.studio.layouteditor.tools.ColorPickerDialogFlag;
import android.zero.studio.layouteditor.tools.ValuesResourceParser;
import android.zero.studio.layouteditor.utils.NameErrorChecker;
import android.zero.studio.layouteditor.utils.SBUtils;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @authors: @raredeveloperofc and @itsvks19;
 * @revised: android_zero (Implemented Async Loading & Error Handling)
 */
public class ColorFragment extends Fragment {

    private ValuesResourceParser colorParser;
    private FragmentResourcesBinding binding;
    private ColorResourceAdapter adapter;
    private List<ValuesItem> colorList = new ArrayList<>();
    
    private ExecutorService executorService;

    @Override
    public android.view.View onCreateView(
        @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResourcesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        executorService = Executors.newSingleThreadExecutor();
        ProjectFile project = ProjectManager.getInstance().getOpenedProject();
        
        RecyclerView mRecyclerView = binding.recyclerView;
        adapter = new ColorResourceAdapter(project, colorList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        if (project != null) {
            loadColorsFromXMLAsync(project.getColorsPath());
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow();
        }
        binding = null;
    }

    /**
     * Loads colors from XML in background thread to prevent UI blocking.
     */
    public void loadColorsFromXMLAsync(String filePath) {
        executorService.execute(() -> {
            try {
                InputStream stream = new FileInputStream(filePath);
                colorParser = new ValuesResourceParser(stream, ValuesResourceParser.TAG_COLOR);
                List<ValuesItem> loadedList = colorParser.getValuesList();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded() || binding == null) return;
                        
                        colorList.clear();
                        if (loadedList != null) {
                            colorList.addAll(loadedList);
                        }
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (FileNotFoundException e) {
                postErrorToUI("Colors file not found: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                postErrorToUI("Error loading colors: " + e.getMessage());
            }
        });
    }

    private void postErrorToUI(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded() && binding != null) {
                    SBUtils.make(binding.getRoot(), message)
                        .setFadeAnimation()
                        .setType(SBUtils.Type.INFO)
                        .show();
                }
            });
        }
    }

    public void addColor() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("New Color");

        LayoutValuesItemDialogBinding bind = LayoutValuesItemDialogBinding.inflate(getLayoutInflater());
        TextInputLayout ilName = bind.textInputLayoutName;
        TextInputLayout ilValue = bind.textInputLayoutValue;
        TextInputEditText etName = bind.textinputName;
        TextInputEditText etValue = bind.textinputValue;

        etValue.setFocusable(false);
        etValue.setOnClickListener(
            (v) -> {
                @SuppressLint("SetTextI18n")
                var dialog = new ColorPickerDialog.Builder(requireContext())
                    .setTitle("Choose Color")
                    .setPositiveButton(getString(R.string.confirm),
                        (ColorEnvelopeListener) (envelope, fromUser) -> {
                            etValue.setText("#" + envelope.getHexCode());
                        })
                    .setNegativeButton(getString(R.string.cancel),
                        (d, i) -> d.dismiss())
                    .attachAlphaSlideBar(true)
                    .attachBrightnessSlideBar(true)
                    .setBottomSpace(12);

                var colorView = dialog.getColorPickerView();
                colorView.setFlagView(new ColorPickerDialogFlag(requireContext()));
                dialog.show();
            });
        builder.setView(bind.getRoot());

        builder.setPositiveButton(
            R.string.add,
            (dlg, i) -> {
                String name = etName.getText().toString();
                String value = etValue.getText().toString();
                
                if (name.isEmpty() || value.isEmpty()) return;

                var colorItem = new ValuesItem(name, value);
                colorList.add(colorItem);
                adapter.notifyItemInserted(colorList.indexOf(colorItem));
                adapter.generateColorsXml();
            });
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        etName.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {}
                @Override
                public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {}
                @Override
                public void afterTextChanged(Editable p1) {
                    NameErrorChecker.checkForValues(etName.getText().toString(), ilName, dialog, colorList);
                }
            });
        NameErrorChecker.checkForValues(etName.getText().toString(), ilName, dialog, colorList);
    }
}