package dmesei.camerascan;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import dmesei.camerascan.Concept.Concept;
import dmesei.camerascan.Scanned.ScannedItem;
import dmesei.camerascan.Scanned.ScannedItemAdapter;

public class ScannedListActivity extends AppCompatActivity {

    List scannedList;
    ScannedItemAdapter scannedItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // List
        RecyclerView scannedListView = findViewById(R.id.scannedListView);

        RecyclerView.LayoutManager llm = new LinearLayoutManager(this);
        scannedListView.setLayoutManager(llm);

        scannedList = new ArrayList<ScannedItem>();
        ScannedItem.fallbackBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.cuaca); // Set fallback Bitmap

        //TODO: Cambiar por lectura de la BD/almacenamiento
        scannedList.add(new ScannedItem(
                "dhwaudihwui",
                new Concept[]{
                        new Concept("Gato 1", .92),
                        new Concept("Concepto 2", .90),
                        new Concept("Concepto 3", .82),
                        new Concept("Concepto 4", .72),
                }
        ));
        scannedList.add(new ScannedItem(
                "fjdfhdsu",
                new Concept[]{
                        new Concept("Gato 2", .92),
                        new Concept("adsadsa", .90),
                        new Concept("dsadsab", .82),
                        new Concept("cdsads", .72),
                }
        ));
        scannedList.add(new ScannedItem(
                "fjhfueshufes",
                new Concept[]{
                        new Concept("Gato 3", .92),
                        new Concept("dale a tu cuerpo", .90),
                        new Concept("alegria macarena", .82),
                        new Concept("eeee macarena", .72),
                }
        ));

        scannedItemAdapter = new ScannedItemAdapter(scannedList);

        scannedListView.setAdapter(scannedItemAdapter);


        // + Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {onFloatingActionButtonClick();}
        });
    }


    public void onFloatingActionButtonClick() { //+ Button
        //TODO: Llamar a lo de abrir camara/galeria y hacer toda la liada
        scannedList.add(new ScannedItem(
                "dsadskaidsk",
                new Concept[]{
                        new Concept("Gato Extra", .92),
                        new Concept("aaa", .90),
                        new Concept("aaa", .82),
                        new Concept("aay", .72),
                }
        ));
        scannedItemAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanned_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.clear_scanned_list) {
            scannedList.clear(); //TODO: Borrar tambien de la BD o almacenamiento, o donde sea
            scannedItemAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
