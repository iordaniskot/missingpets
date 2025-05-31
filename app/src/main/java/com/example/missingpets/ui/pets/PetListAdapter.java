package com.example.missingpets.ui.pets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.missingpets.R;
import com.example.missingpets.data.models.Pet;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PetListAdapter extends RecyclerView.Adapter<PetListAdapter.PetViewHolder> {
    
    private List<Pet> pets;
    private OnPetClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnPetClickListener {
        void onPetClick(Pet pet);
    }
    
    public PetListAdapter(List<Pet> pets, OnPetClickListener listener) {
        this.pets = pets;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = pets.get(position);
        holder.bind(pet);
    }
    
    @Override
    public int getItemCount() {
        return pets.size();
    }
    
    public void updatePets(List<Pet> newPets) {
        this.pets = newPets;
        notifyDataSetChanged();
    }
      class PetViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPetPhoto;
        private TextView tvPetName;
        private TextView tvPetBreed;
        private com.google.android.material.chip.Chip chipStatus;
        private TextView tvLocation;
        private TextView tvDate;
        private com.google.android.material.button.MaterialButton btnContact;
          public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPetPhoto = itemView.findViewById(R.id.ivPetPhoto);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            tvPetBreed = itemView.findViewById(R.id.tvPetBreed);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnContact = itemView.findViewById(R.id.btnContact);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onPetClick(pets.get(position));
                    }
                }
            });
        }        public void bind(Pet pet) {
            // Use displayName helper method from Pet model
            tvPetName.setText(pet.getDisplayName());
            
            // Set pet breed (type field no longer exists in Pet model)
            StringBuilder breedText = new StringBuilder();
            if (pet.getBreed() != null && !pet.getBreed().isEmpty()) {
                breedText.append(pet.getBreed());
            }
            if (pet.getColor() != null && !pet.getColor().isEmpty()) {
                if (breedText.length() > 0) breedText.append(" • ");
                breedText.append(capitalizeFirst(pet.getColor()));
            }
            tvPetBreed.setText(breedText.length() > 0 ? breedText.toString() : "Pet details");
            
            // Hide status chip since Pet model no longer contains status
            // Status is now in Report model
            chipStatus.setVisibility(View.GONE);
            
            // Hide location since Pet model no longer contains location
            // Location is now in Report model
            tvLocation.setVisibility(View.GONE);
            
            // Set creation date instead of last seen
            if (pet.getCreatedAt() != null) {
                tvDate.setText("Posted: " + dateFormat.format(pet.getCreatedAt()));
            } else {
                tvDate.setText("Date unknown");
            }
            
            // Set contact button click listener
            btnContact.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPetClick(pet);
                }
            });
            
            // Set pet image placeholder based on breed
            setPlaceholderImageFromBreed(pet.getBreed());
        }
          private void setPlaceholderImageFromBreed(String breed) {
            // Set different placeholder icons based on breed (since type field is removed)
            if (breed != null) {
                String lowerBreed = breed.toLowerCase();
                if (lowerBreed.contains("dog") || lowerBreed.contains("retriever") || 
                    lowerBreed.contains("terrier") || lowerBreed.contains("bulldog") ||
                    lowerBreed.contains("poodle") || lowerBreed.contains("shepherd")) {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_dog);
                } else if (lowerBreed.contains("cat") || lowerBreed.contains("persian") || 
                           lowerBreed.contains("siamese") || lowerBreed.contains("maine") ||
                           lowerBreed.contains("bengal") || lowerBreed.contains("ragdoll")) {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_cat);
                } else {
                    ivPetPhoto.setImageResource(R.drawable.ic_pets_other);
                }
            } else {
                ivPetPhoto.setImageResource(R.drawable.ic_pets_other);
            }
        }
        
        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) return text;
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
    }
}
