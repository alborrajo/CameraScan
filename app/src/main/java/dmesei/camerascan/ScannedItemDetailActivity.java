package dmesei.camerascan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import dmesei.camerascan.Concept.ConceptAdapter;
import dmesei.camerascan.Scanned.ScannedItem;

public class ScannedItemDetailActivity extends AppCompatActivity {

    private ScannedItem scannedItem;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView collapsingToolbarLayoutImageView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_item_detail);

        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayoutImageView = findViewById(R.id.imageView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get scanned item from extras
        Intent detailIntent = getIntent();
        scannedItem = detailIntent.getParcelableExtra("scannedItem");

        // Set toolbar title
        collapsingToolbarLayout.setTitle(scannedItem.concepts[0].name);

        // Set toolbar X button action (Press back)
        toolbar.setNavigationOnClickListener((View view) -> this.onBackPressed());

        // Show image in fullscreen when clicking on the toolbar
        collapsingToolbarLayoutImageView.setOnClickListener(view -> {
            // Start scanned item image activity
            Intent imageIntent = new Intent(view.getContext(), ScannedItemImageActivity.class);
            imageIntent.putExtra("scannedItem",scannedItem); // Put scannedItem as extra
            view.getContext().startActivity(imageIntent);
        });


            // Concepts
        RecyclerView conceptListView = findViewById(R.id.conceptListView);

        RecyclerView.LayoutManager llm = new LinearLayoutManager(this);
        conceptListView.setLayoutManager(llm);

        ConceptAdapter conceptAdapter = new ConceptAdapter(scannedItem.concepts);
        conceptListView.setAdapter(conceptAdapter);


        // Load scanned item image when the toolbar layout loads
        collapsingToolbarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid it getting called over and over
                // We just want it to run the first time, when the view is loaded
                collapsingToolbarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get toolbar width in pixels
                final int toolbarWidth = collapsingToolbarLayout.getWidth();

                // Load bitmap asynchronously
                new AsyncTask<Void, Void, Void>() {

                    Bitmap scannedItemBitmap = null;

                    @Override
                    protected Void doInBackground(Void... voids) {
                        scannedItemBitmap = scannedItem.getBitmapWithSize(toolbarWidth, 1);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        collapsingToolbarLayoutImageView.setImageBitmap(scannedItemBitmap);
                    }
                }.execute();
            }
        });
    }

}
