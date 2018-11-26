package dmesei.camerascan.Scanned;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import dmesei.camerascan.R;
import dmesei.camerascan.ScannedItemDetailActivity;

public class ScannedItemAdapter extends RecyclerView.Adapter<ScannedItemAdapter.ViewHolder> {

    private List<ScannedItem> scannedItemList;


    //View holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.scannedItemImage);
            textView = view.findViewById(R.id.scannedItemText);
        }
    }

    //Constructor
    public ScannedItemAdapter(List<ScannedItem> scannedItemList) {
        this.scannedItemList = scannedItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final ScannedItem scannedItem = scannedItemList.get(i);

        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.scanned_item_layout, viewGroup, false);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start detail view activity
                Intent detailIntent = new Intent(view.getContext(), ScannedItemDetailActivity.class);
                detailIntent.putExtra("scannedItem", scannedItem); // Put scannedItem as extra
                view.getContext().startActivity(detailIntent);
            }
        });

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ScannedItem scannedItem = scannedItemList.get(i);
        viewHolder.imageView.setImageBitmap(scannedItem.bitmap);
        viewHolder.textView.setText(scannedItem.concepts[0].name);
    }

    @Override
    public int getItemCount() {
        return scannedItemList.size();
    }
}