package com.example.missingpets.ui.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.missingpets.R;
import com.example.missingpets.data.models.ReportWithPet;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EnhancedReportListAdapter extends RecyclerView.Adapter<EnhancedReportListAdapter.ReportViewHolder> {
    
    private List<ReportWithPet> reports;
    private OnReportClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnReportClickListener {
        void onReportClick(ReportWithPet report);
    }
    
    public EnhancedReportListAdapter(List<ReportWithPet> reports, OnReportClickListener listener) {
        this.reports = reports;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportWithPet report = reports.get(position);
        holder.bind(report);
    }
    
    @Override
    public int getItemCount() {
        return reports.size();
    }
    
    public void updateReports(List<ReportWithPet> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }
    
    class ReportViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPetPhoto;
        private TextView tvPetName;
        private TextView tvDescription;
        private com.google.android.material.chip.Chip chipStatus;
        private TextView tvLocation;
        private TextView tvDate;
        private com.google.android.material.button.MaterialButton btnContact;
        
        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPetPhoto = itemView.findViewById(R.id.ivPetPhoto);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            tvDescription = itemView.findViewById(R.id.tvPetBreed); // Reusing this field for description
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnContact = itemView.findViewById(R.id.btnContact);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onReportClick(reports.get(position));
                    }
                }
            });
        }
        
        public void bind(ReportWithPet report) {
            // Set pet name using the enhanced display title
            tvPetName.setText(report.getDisplayTitle());
            
            // Set description or pet breed info
            if (report.getDescription() != null && !report.getDescription().isEmpty()) {
                tvDescription.setText(report.getDescription());
            } else if (report.getPet() != null) {
                // Show pet breed and color info
                StringBuilder petInfo = new StringBuilder();
                if (report.getPet().getBreed() != null && !report.getPet().getBreed().isEmpty()) {
                    petInfo.append(report.getPet().getBreed());
                }
                if (report.getPet().getColor() != null && !report.getPet().getColor().isEmpty()) {
                    if (petInfo.length() > 0) petInfo.append(" • ");
                    petInfo.append(capitalizeFirst(report.getPet().getColor()));
                }
                tvDescription.setText(petInfo.length() > 0 ? petInfo.toString() : "No description available");
            } else {
                tvDescription.setText("No description available");
            }
            
            // Set status chip
            chipStatus.setVisibility(View.VISIBLE);
            if ("lost".equals(report.getStatus())) {
                chipStatus.setText("MISSING");
                chipStatus.setChipBackgroundColorResource(R.color.error);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
            } else if ("found".equals(report.getStatus())) {
                chipStatus.setText("FOUND");
                chipStatus.setChipBackgroundColorResource(R.color.success);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
            } else {
                chipStatus.setText("UNKNOWN");
                chipStatus.setChipBackgroundColorResource(R.color.text_secondary);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
            }
            
            // Set location
            tvLocation.setVisibility(View.VISIBLE);
            tvLocation.setText(report.getLocationString());
            
            // Set report date
            if (report.getCreatedAt() != null) {
                tvDate.setText("Reported: " + dateFormat.format(report.getCreatedAt()));
            } else {
                tvDate.setText("Date unknown");
            }
            
            // Set contact button click listener
            btnContact.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReportClick(report);
                }
            });
            
            // Set pet image based on pet breed or status
            setPetImage(report);
        }
        
        private void setPetImage(ReportWithPet report) {
            if (report.getPet() != null && report.getPet().getBreed() != null) {
                // Use breed-based image if pet information is available
                String breed = report.getPet().getBreed().toLowerCase();
                if (breed.contains("dog") || breed.contains("retriever") || 
                    breed.contains("terrier") || breed.contains("bulldog") ||
                    breed.contains("poodle") || breed.contains("shepherd")) {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_dog);
                } else if (breed.contains("cat") || breed.contains("persian") || 
                           breed.contains("siamese") || breed.contains("maine") ||
                           breed.contains("bengal") || breed.contains("ragdoll")) {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_cat);
                } else {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_other);
                }
            } else {
                // Fallback to status-based image
                if ("lost".equals(report.getStatus())) {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_missing);
                } else if ("found".equals(report.getStatus())) {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_found);
                } else {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_other);
                }
            }
        }
        
        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) return text;
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
    }
}
