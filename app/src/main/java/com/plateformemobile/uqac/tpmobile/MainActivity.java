package com.plateformemobile.uqac.tpmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    // Chemin vers le dossier ou sont contenu les notes
    static final String CAMERA_PIC_DIR = "/DCIM/Text/";
    // Servira pour afficher les notes sous forme de liste
    private List<String> directoryEntries = new ArrayList<String>();
    private ListView listLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Chemin absolu vers le répertoire de travail + CAMERA_PIC_DIR
        final String path = "/storage/sdcard0" + CAMERA_PIC_DIR;
        final File directory = new File(path);

        // Si on pointe bien vers un répertoire
        if (directory.isDirectory()) {

            // Mise à jour de la liste affiché
            updateView(directory);
            // Gérer la sélection d'un item dans la liste
            listLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    final String fileName = directoryEntries.get((int)id);
                    final CharSequence[] items = getResources().getStringArray(R.array.string_array_name);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(R.string.action));
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        // Click listener
                        public void onClick(DialogInterface dialog, int item) {
                            Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
                            switch (item) {
                                case 0: {
                                    // Quand on ouvre le fichier
                                    break;
                                }
                                case 1: {
                                    // Quand on supprime le fichier
                                    File fileToDelete = new File(path + fileName);
                                    fileToDelete.delete();
                                    updateView(directory);
                                    break;
                                }
                                case 2: {
                                    // Quand on retourne en arrière
                                    break;
                                }
                            }
                            // Mettre ici les différentes action a éffectuer
                        }
                    });

                    AlertDialog alert = builder.create();

                    //display dialog box
                    alert.show();
                }
            });
        }
    }

    void updateView(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            // Tris dans l'ordre décroissant de la date
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return -Long.valueOf(f1.lastModified())
                            .compareTo(f2.lastModified());
                }
            });

            // Ajout des fichier dans la liste
            this.directoryEntries.clear();
            for (File file : files) {
                this.directoryEntries.add(file.getName());
            }

            // Affichage de la liste
            final ArrayAdapter<String> directoryList
                    = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, this.directoryEntries);

            listLayout = (ListView) findViewById(R.id.noteList);
            listLayout.setAdapter(directoryList);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Setting", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(id == R.id.action_new_note) {
            Toast.makeText(getApplicationContext(), "New", Toast.LENGTH_SHORT).show();
            // Ajouter ici l'intent pour un nouveau fichier
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}