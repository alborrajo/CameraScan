package dmesei.camerascan;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import dmesei.camerascan.Concept.Concept;
import dmesei.camerascan.Scanned.ScannedItem;
import dmesei.camerascan.Scanned.ScannedItemAdapter;

public class ScannedListActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static int MY_PERMISSIONS_REQUEST;

    private String imagePath;

    List<ScannedItem> scannedList;
    ScannedItemAdapter scannedItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // List
        RecyclerView scannedListView = findViewById(R.id.scannedListView);

            //LayoutManager
        RecyclerView.LayoutManager llm = new LinearLayoutManager(this);
        scannedListView.setLayoutManager(llm);

            //Create list to be assigned to the view
        scannedList = new ArrayList<ScannedItem>();

            //Set adapter for the list
        scannedItemAdapter = new ScannedItemAdapter(scannedList);
        scannedListView.setAdapter(scannedItemAdapter);


        // Set fallback icon for every list element
        ScannedItem.fallbackBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.cuaca);


        // + Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {onFloatingActionButtonClick();}
        });


        //Permiso
        if (ContextCompat.checkSelfPermission(ScannedListActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ScannedListActivity.this,
                    Manifest.permission.CAMERA)) {

            } else {

                ActivityCompat.requestPermissions(ScannedListActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST);


            }
        }


    }

   public void onPause(){
        super.onPause();
        final SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

       Gson gson = new Gson();
       String json = gson.toJson(scannedList);
        editor.putString("lista",json);
        editor.apply();
    }

    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("lista", "");

        Log.d("DEBUG RESUME", json);

        ArrayList<ScannedItem> elementos = gson.fromJson(json, new TypeToken<List<ScannedItem>>(){}.getType());

        scannedList.clear();
        for(ScannedItem elemento: elementos) {
            scannedList.add(elemento);
        }

        scannedItemAdapter.notifyDataSetChanged();

    }


    public void onFloatingActionButtonClick() { //+ Button
        //Permiso camara
        if (ContextCompat.checkSelfPermission(ScannedListActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ScannedListActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(ScannedListActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);


            }
        }
        // Abrir cámara
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { //Si se puede abrir cámara
            // Crear fichero donde se guardará la imagen
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                File imageFile = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",  /* suffix */
                        storageDir      /* directory */
                );

                // File path
                imagePath = imageFile.getAbsolutePath();

                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);


                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } catch(IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

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


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Vuelta de obtener una imagen de la cámara
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ScannedItem newItem = new ScannedItem(
                    imagePath,
                    new Concept[]{
                            new Concept("Probando 1", 0.95),
                            new Concept("Probando 2", 0.90),
                            new Concept("Probando 3", 0.8),
                    }
            );

            scannedList.add(newItem);
            scannedItemAdapter.notifyDataSetChanged();

            // APAÑO CUTRE /!\
            // Resulta que se ejecuta primero el onActivityResult
            // y luego el onResume
            // Por tanto se añadía el item a la lista Y LUEGO se cargaba la lista de las preferencias
            // Por ello nunca se guardaban los elementos nuevos a la lista
            // Solución cutre: Llamar al onPause al volver de la actividad
            onPause(); //TODO Buscar forma mejor

        }
    }

}
