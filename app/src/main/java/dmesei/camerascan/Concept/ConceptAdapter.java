package dmesei.camerascan.Concept;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dmesei.camerascan.R;

public class ConceptAdapter extends RecyclerView.Adapter<ConceptAdapter.ViewHolder> {

    private Concept[] conceptArray;


    //View holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView percentageTextView;

        public ViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.nameTextView);
            percentageTextView = view.findViewById(R.id.percentageTextView);
        }
    }

    //Constructor
    public ConceptAdapter(Concept[] conceptArray) {
        this.conceptArray = conceptArray;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.concept_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Concept concept = conceptArray[i];
        viewHolder.nameTextView.setText(concept.name);
        viewHolder.percentageTextView.setText(Double.toString(concept.percentage));
    }

    @Override
    public int getItemCount() {
        return conceptArray.length;
    }
}
