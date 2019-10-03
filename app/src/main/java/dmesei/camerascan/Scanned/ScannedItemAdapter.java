package dmesei.camerascan.Scanned;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dmesei.camerascan.R;
import dmesei.camerascan.ScannedItemDetailActivity;

public class ScannedItemAdapter extends RecyclerView.Adapter<ScannedItemAdapter.ViewHolder> {

    private List<ScannedItem> scannedItemList;
    private List<Bitmap> loadedThumbnails;

    private LazyLoadCallback lazyLoadCallback;
    private OnItemClickListener onItemClickListener;

    public Bitmap fallbackBitmap;

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
        this.loadedThumbnails = new ArrayList<>(scannedItemList.size());
    }

    public void setLazyLoadCallback(LazyLoadCallback lazyLoadCallback) {
        this.lazyLoadCallback = lazyLoadCallback;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.scanned_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int index) {
        final ScannedItem scannedItem = scannedItemList.get(index);

        // Set callbacks for each item
        viewHolder.itemView.setOnClickListener((View view) -> {
            onItemClickListener.onClick(viewHolder.itemView, scannedItem);
        });


        // Expand list if necessary
        for(int ltSize=loadedThumbnails.size(); ltSize <= scannedItemList.size(); ltSize++) {loadedThumbnails.add(null);}

        // Lazy load image
        if(loadedThumbnails.get(index) == null) {
            // Use lazy loading only if the callback is set
            // If it isn't, load synchronously
            if(lazyLoadCallback == null) {
                Bitmap loadedBitmap = scannedItem.getBitmapWithSize(64, 64);
                if(loadedBitmap == null) {loadedBitmap = fallbackBitmap;} // Permanently set fallback if the image cant be found
                loadedThumbnails.set(index, loadedBitmap);
                viewHolder.imageView.setImageBitmap(loadedBitmap);
            } else {
                new LazyLoadScannedItem(index, scannedItem, lazyLoadCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                viewHolder.imageView.setImageBitmap(fallbackBitmap); // Temporarily use the fallback bitmap
            }
        } else {
            viewHolder.imageView.setImageBitmap(loadedThumbnails.get(index));
        }

        viewHolder.textView.setText(scannedItem.concepts[0].name);

    }

    @Override
    public int getItemCount() {
        return scannedItemList.size();
    }


    public interface OnItemClickListener {
        void onClick(View view, ScannedItem scannedItem);
    }

    public interface LazyLoadCallback {
        void callback(int index);
    }

    private class LazyLoadScannedItem extends AsyncTask<Void, Void, Void> {

        int index;
        ScannedItem scannedItem;
        LazyLoadCallback lazyLoadCallback;

        public LazyLoadScannedItem(int i, ScannedItem si, LazyLoadCallback llc) {
            index = i;
            scannedItem = si;
            lazyLoadCallback = llc;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Bitmap loadedBitmap = scannedItem.getBitmapWithSize(64, 64);

            if(loadedBitmap == null) {loadedThumbnails.set(index, fallbackBitmap);} // Permanently set fallback if the image cant be found
            else {loadedThumbnails.set(index, loadedBitmap);}

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            this.lazyLoadCallback.callback(index); /* THIS CALLBACK SHOULD CALL .notifyItemChanged(index) */
        }
    }
}
