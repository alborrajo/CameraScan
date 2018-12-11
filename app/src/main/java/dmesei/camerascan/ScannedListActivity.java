package dmesei.camerascan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import dmesei.camerascan.Concept.Concept;
import dmesei.camerascan.Scanned.ScannedItem;
import dmesei.camerascan.Scanned.ScannedItemAdapter;

public class ScannedListActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static int MY_PERMISSIONS_REQUEST;

    private String imagePath;

    List<ScannedItem> scannedList;
    ScannedItemAdapter scannedItemAdapter;

    ClarifaiClient clarifaiClient;

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
        scannedList = new ArrayList<>();

            //Load state from preferences
        List<ScannedItem> elementos = StateManager.loadState(this);
        for(ScannedItem elemento: elementos) {
            scannedList.add(elemento);
        }

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

        // INICIAR CLARIFAI
        clarifaiClient = new ClarifaiBuilder(getResources().getString(R.string.clarifai_api_key)).buildSync();

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


    @SuppressLint("StaticFieldLeak")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Vuelta de obtener una imagen de la cámara
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // Mostrar mensaje de Analizando
            Toast.makeText(ScannedListActivity.this, R.string.analyzing, Toast.LENGTH_LONG).show();

            // Enviar petición
            new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<clarifai2.dto.prediction.Concept>>>>() {
                @Override
                protected ClarifaiResponse<List<ClarifaiOutput<clarifai2.dto.prediction.Concept>>> doInBackground(Void... params) {
                    // The default Clarifai model that identifies concepts in images
                    final ConceptModel generalModel = clarifaiClient.getDefaultModels().generalModel();

                    // Use this model to predict, with the image that the user just selected as the input
                    return generalModel.predict()
                            .withInputs(ClarifaiInput.forImage(new File(imagePath)))
                            .executeSync();
                }

                @Override
                protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<clarifai2.dto.prediction.Concept>>> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(ScannedListActivity.this, getResources().getString(R.string.analyzing_error), Toast.LENGTH_LONG).show();
                        return;
                    }
                    final List<ClarifaiOutput<clarifai2.dto.prediction.Concept>> predictions = response.get();
                    if (predictions.isEmpty()) {
                        Toast.makeText(ScannedListActivity.this, getResources().getString(R.string.analyzing_error), Toast.LENGTH_LONG).show();
                        return;
                    } else {

                        // Mostrar mensaje de Respuesta
                        Toast.makeText(ScannedListActivity.this, R.string.analyzing_success, Toast.LENGTH_LONG).show();

                        // Leer respuesta
                        List<clarifai2.dto.prediction.Concept> responseClarifaiConcepts = predictions.get(0).data(); // Obtener lista de Conceptos de Clarifai de la respuesta
                        Concept[] concepts = new Concept[responseClarifaiConcepts.size()]; // Crear array de objetos Concept

                        // Convertir lista de Concepts de Clarifai a array de Concepts nuestros
                        for(int i=0; i < concepts.length; i++) {
                            concepts[i] = new Concept(responseClarifaiConcepts.get(i));
                        }

                        // Crear objeto ScannedItem con los datos
                        ScannedItem newItem = new ScannedItem(imagePath, concepts);

                        scannedList.add(newItem);
                        scannedItemAdapter.notifyDataSetChanged();

                        // Guardar estado
                        StateManager.saveState(ScannedListActivity.this, scannedList);
                    }

                }
            }.execute();


        }
    }

}
