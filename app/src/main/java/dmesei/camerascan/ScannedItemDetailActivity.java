package dmesei.camerascan;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import dmesei.camerascan.Concept.ConceptAdapter;
import dmesei.camerascan.Scanned.ScannedItem;

public class ScannedItemDetailActivity extends AppCompatActivity {

    private ScannedItem scannedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_item_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get scanned item from extras
        Intent detailIntent = getIntent();
        scannedItem = detailIntent.getParcelableExtra("scannedItem");

        // Fill view with scanned item details
            // Image and title
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setBackground(new BitmapDrawable(getResources(), scannedItem.bitmap)); // Image
        collapsingToolbarLayout.setTitle(scannedItem.concepts[0].name); // Title

            // Concepts
        RecyclerView conceptListView = findViewById(R.id.conceptListView);

        RecyclerView.LayoutManager llm = new LinearLayoutManager(this);
        conceptListView.setLayoutManager(llm);

        ConceptAdapter conceptAdapter = new ConceptAdapter(scannedItem.concepts);
        conceptListView.setAdapter(conceptAdapter);

    }
}
