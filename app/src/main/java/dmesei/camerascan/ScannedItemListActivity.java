package dmesei.camerascan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import dmesei.camerascan.Concept.Concept;
import dmesei.camerascan.Scanned.ScannedItem;
import dmesei.camerascan.Scanned.ScannedItemAdapter;

public class ScannedItemListActivity extends AppCompatActivity {

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
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setReverseLayout(true); // Reverse order
        llm.setStackFromEnd(true); // Reverse order and start from top
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

            //Set callbacks
        scannedItemAdapter.setLazyLoadCallback((int index) ->
            scannedItemAdapter.notifyItemChanged(index)
        );
        scannedItemAdapter.setOnItemClickListener((View view, final ScannedItem scannedItem) -> {
            // Start detail view activity
            Intent detailIntent = new Intent(view.getContext(), ScannedItemDetailActivity.class);
            detailIntent.putExtra("scannedItem",scannedItem); // Put scannedItem as extra
            view.getContext().startActivity(detailIntent);
        });
        scannedItemAdapter.setOnCreateContextMenuListener((menu, view, contextMenuInfo, scannedItem) -> {
           new MenuInflater(view.getContext()).inflate(R.menu.menu_scanned_item, menu);
           return true;
        });

        registerForContextMenu(scannedListView);
        scannedListView.setAdapter(scannedItemAdapter);

        // Set fallback icon for every list element
        scannedItemAdapter.fallbackBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);


        // + Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> onFloatingActionButtonClick());


        //Permiso
        if (ContextCompat.checkSelfPermission(ScannedItemListActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ScannedItemListActivity.this,
                    Manifest.permission.CAMERA)) {

            } else {

                ActivityCompat.requestPermissions(ScannedItemListActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST);


            }
        }

        // INICIAR CLARIFAI
        clarifaiClient = new ClarifaiBuilder(getResources().getString(R.string.clarifai_api_key)).buildSync();

    }

    public void onFloatingActionButtonClick() { //+ Button
        //Permiso camara
        if (ContextCompat.checkSelfPermission(ScannedItemListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            && !ActivityCompat.shouldShowRequestPermissionRationale(ScannedItemListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(ScannedItemListActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
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
            scannedList.clear();
            StateManager.saveState(ScannedItemListActivity.this, scannedList); // Borrar tambien de la "BD"
            scannedItemAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.delete:
                int scannedItemIndex = scannedItemAdapter.currentPosition;
                scannedList.remove(scannedItemIndex);
                scannedItemAdapter.notifyItemRemoved(scannedItemIndex);
        }
        return super.onContextItemSelected(item);
    }


    @SuppressLint("StaticFieldLeak")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Vuelta de obtener una imagen de la cámara
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // Mostrar mensaje de Analizando
            Toast.makeText(ScannedItemListActivity.this, R.string.analyzing, Toast.LENGTH_LONG).show();

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
                        Toast.makeText(ScannedItemListActivity.this, getResources().getString(R.string.analyzing_error), Toast.LENGTH_LONG).show();
                        return;
                    }
                    final List<ClarifaiOutput<clarifai2.dto.prediction.Concept>> predictions = response.get();
                    if (predictions.isEmpty()) {
                        Toast.makeText(ScannedItemListActivity.this, getResources().getString(R.string.analyzing_error), Toast.LENGTH_LONG).show();
                        return;
                    } else {

                        // Mostrar mensaje de Respuesta
                        Toast.makeText(ScannedItemListActivity.this, R.string.analyzing_success, Toast.LENGTH_LONG).show();

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
                        scannedItemAdapter.notifyItemInserted(scannedList.size()-1);

                        // Guardar estado
                        StateManager.saveState(ScannedItemListActivity.this, scannedList);
                    }

                }
            }.execute();


        }
    }

}
