package dmesei.camerascan.Scanned;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import dmesei.camerascan.R;
import dmesei.camerascan.ScannedItemDetailActivity;

public class ScannedItemAdapter extends RecyclerView.Adapter<ScannedItemAdapter.ViewHolder> {

    private List<ScannedItem> scannedItemList;
    private Map<ScannedItem, Bitmap> loadedThumbnails;

    private LazyLoadCallback lazyLoadCallback;
    private OnItemClickListener onItemClickListener;
    private OnCreateContextMenuListener onCreateContextMenuListener;

    public Bitmap fallbackBitmap;

    public int currentPosition; // For Context Menu

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
        this.loadedThumbnails = new WeakHashMap<>(scannedItemList.size());
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnCreateContextMenuListener(OnCreateContextMenuListener onCreateContextMenuListener) {
        this.onCreateContextMenuListener = onCreateContextMenuListener;
    }

    public void setLazyLoadCallback(LazyLoadCallback lazyLoadCallback) {
        this.lazyLoadCallback = lazyLoadCallback;
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
        viewHolder.itemView.setOnClickListener((View view) ->
            onItemClickListener.onClick(viewHolder.itemView, scannedItem)
        );

        viewHolder.itemView.setOnCreateContextMenuListener((ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) -> {
            currentPosition = index;
            onCreateContextMenuListener.onCreateContextMenu(menu, view, contextMenuInfo, scannedItem);
        });


        // Lazy load image
        if(loadedThumbnails.get(scannedItem) == null) {
            // If the callback is set: Use lazy loading
            // If it isn't: Load synchronously
            if(lazyLoadCallback == null) {
                Bitmap loadedBitmap = scannedItem.getBitmapWithSize(viewHolder.imageView.getWidth(), viewHolder.imageView.getHeight());
                if(loadedBitmap == null) {loadedBitmap = fallbackBitmap;} // Permanently set fallback if the image cant be found
                loadedThumbnails.put(scannedItem, loadedBitmap);
                viewHolder.imageView.setImageBitmap(loadedBitmap);
            } else {
                new LazyLoadScannedItem(index, scannedItem, lazyLoadCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                viewHolder.imageView.setImageBitmap(fallbackBitmap); // Temporarily use the fallback bitmap
            }
        } else {
            viewHolder.imageView.setImageBitmap(loadedThumbnails.get(scannedItem));
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

    public interface OnCreateContextMenuListener {
        boolean onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo, ScannedItem scannedItem);
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

            if(loadedBitmap == null) {loadedThumbnails.put(scannedItem, fallbackBitmap);} // Permanently set fallback if the image cant be found
            else {loadedThumbnails.put(scannedItem, loadedBitmap);}

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            this.lazyLoadCallback.callback(index); /* THIS CALLBACK SHOULD CALL .notifyItemChanged(index) */
        }
    }
}
