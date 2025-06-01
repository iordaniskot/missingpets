package com.example.missingpets.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.missingpets.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFiltersApplied(FilterOptions filterOptions);
    }

    public static class FilterOptions {
        public String selectedBreed;
        public String selectedColor;
        public int radiusKm;
        public int daysAgo;

        public FilterOptions() {
            this.selectedBreed = "";
            this.selectedColor = "";
            this.radiusKm = 50; // Default 50km radius
            this.daysAgo = 30; // Default 30 days
        }
    }

    private FilterListener filterListener;
    private FilterOptions currentFilters;

    private ChipGroup chipGroupBreeds;
    private ChipGroup chipGroupColors;
    private SeekBar seekBarRadius;
    private SeekBar seekBarDays;
    private TextView tvRadiusValue;
    private TextView tvDaysValue;
    private MaterialButton btnApply;
    private MaterialButton btnReset;

    public static FilterBottomSheetDialogFragment newInstance(FilterOptions currentFilters) {
        FilterBottomSheetDialogFragment fragment = new FilterBottomSheetDialogFragment();
        fragment.currentFilters = currentFilters != null ? currentFilters : new FilterOptions();
        return fragment;
    }

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupFilters();
        setupListeners();
        updateUIWithCurrentFilters();
    }

    private void initViews(View view) {
        chipGroupBreeds = view.findViewById(R.id.chipGroupBreeds);
        chipGroupColors = view.findViewById(R.id.chipGroupColors);
        seekBarRadius = view.findViewById(R.id.seekBarRadius);
        seekBarDays = view.findViewById(R.id.seekBarDays);
        tvRadiusValue = view.findViewById(R.id.tvRadiusValue);
        tvDaysValue = view.findViewById(R.id.tvDaysValue);
        btnApply = view.findViewById(R.id.btnApply);
        btnReset = view.findViewById(R.id.btnReset);
    }

    private void setupFilters() {
        // Setup breed chips
        String[] breeds = {"Dog", "Cat", "Bird", "Rabbit", "Other"};
        for (String breed : breeds) {
            Chip chip = (Chip) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_filter_chip, chipGroupBreeds, false);
            chip.setText(breed);
            chip.setCheckable(true);
            chipGroupBreeds.addView(chip);
        }

        // Setup color chips
        String[] colors = {"Brown", "Black", "White", "Golden", "Gray", "Mixed"};
        for (String color : colors) {
            Chip chip = (Chip) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_filter_chip, chipGroupColors, false);
            chip.setText(color);
            chip.setCheckable(true);
            chipGroupColors.addView(chip);
        }

        // Setup seek bars
        seekBarRadius.setMax(200); // Max 200km
        seekBarDays.setMax(365); // Max 1 year
    }

    private void setupListeners() {
        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = Math.max(1, progress); // Minimum 1km
                tvRadiusValue.setText(radius + " km");
                currentFilters.radiusKm = radius;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarDays.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int days = Math.max(1, progress); // Minimum 1 day
                tvDaysValue.setText(days + " days");
                currentFilters.daysAgo = days;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        chipGroupBreeds.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                currentFilters.selectedBreed = selectedChip.getText().toString();
            } else {
                currentFilters.selectedBreed = "";
            }
        });

        chipGroupColors.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                currentFilters.selectedColor = selectedChip.getText().toString();
            } else {
                currentFilters.selectedColor = "";
            }
        });

        btnApply.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onFiltersApplied(currentFilters);
            }
            dismiss();
        });

        btnReset.setOnClickListener(v -> {
            resetFilters();
        });
    }

    private void updateUIWithCurrentFilters() {
        // Set radius
        seekBarRadius.setProgress(currentFilters.radiusKm);
        tvRadiusValue.setText(currentFilters.radiusKm + " km");

        // Set days
        seekBarDays.setProgress(currentFilters.daysAgo);
        tvDaysValue.setText(currentFilters.daysAgo + " days");

        // Set breed selection
        if (!currentFilters.selectedBreed.isEmpty()) {
            for (int i = 0; i < chipGroupBreeds.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupBreeds.getChildAt(i);
                if (chip.getText().toString().equals(currentFilters.selectedBreed)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        // Set color selection
        if (!currentFilters.selectedColor.isEmpty()) {
            for (int i = 0; i < chipGroupColors.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupColors.getChildAt(i);
                if (chip.getText().toString().equals(currentFilters.selectedColor)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private void resetFilters() {
        currentFilters = new FilterOptions();
        chipGroupBreeds.clearCheck();
        chipGroupColors.clearCheck();
        updateUIWithCurrentFilters();
    }
}
