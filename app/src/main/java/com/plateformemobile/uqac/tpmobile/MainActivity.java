package com.plateformemobile.uqac.tpmobile;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    static final String CAMERA_PIC_DIR = "/DCIM/Text/";
    File noteDisplay;
    private List<String> directoryEntries = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textDisplay = (TextView)findViewById(R.id.noteText);

        textDisplay.append("toto");

        String path = "/storage/sdcard0" + CAMERA_PIC_DIR;

        File directory = new File(path);
        textDisplay.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            //sort in descending date order
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return -Long.valueOf(f1.lastModified())
                            .compareTo(f2.lastModified());
                }
            });

            this.directoryEntries.clear();
            for (File file : files) {
                this.directoryEntries.add(file.getName());
            }

            ArrayAdapter<String> directoryList
                    = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, this.directoryEntries);
            //alphabetize entries
            //directoryList.sort(null);

            ListView list = (ListView)findViewById(R.id.noteList);
            list.setAdapter(directoryList);
        }
        else {
            textDisplay.append("toto3");
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
