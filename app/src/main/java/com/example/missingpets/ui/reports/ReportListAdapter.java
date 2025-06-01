package com.example.missingpets.ui.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.missingpets.R;
import com.example.missingpets.data.models.Report;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ReportViewHolder> {
    
    private List<Report> reports;
    private OnReportClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnReportClickListener {
        void onReportClick(Report report);
    }
    
    public ReportListAdapter(List<Report> reports, OnReportClickListener listener) {
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
        Report report = reports.get(position);
        holder.bind(report);
    }
    
    @Override
    public int getItemCount() {
        return reports.size();
    }
    
    public void updateReports(List<Report> newReports) {
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
        
        public void bind(Report report) {
            // Set pet name - for now use "Unknown Pet" since we only have pet ID
            tvPetName.setText("Pet Report #" + (report.getId() != null ? report.getId().substring(0, 8) : "Unknown"));
            
            // Set description
            if (report.getDescription() != null && !report.getDescription().isEmpty()) {
                tvDescription.setText(report.getDescription());
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
            if (report.getLocation() != null) {
                double lat = report.getLocation().getLatitude();
                double lng = report.getLocation().getLongitude();
                tvLocation.setText(String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng));
            } else {
                tvLocation.setText("Location unknown");
            }
            
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
            
            // Set placeholder image based on status
            setPlaceholderImageFromStatus(report.getStatus());
        }
        
        private void setPlaceholderImageFromStatus(String status) {
            if ("lost".equals(status)) {
                ivPetPhoto.setImageResource(R.drawable.ic_pets_missing);
            } else if ("found".equals(status)) {
                ivPetPhoto.setImageResource(R.drawable.ic_pets_found);
            } else {
                ivPetPhoto.setImageResource(R.drawable.ic_pets_other);
            }
        }
    }
}
