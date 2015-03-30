package com.plateformemobile.uqac.tpmobile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ListViewLoader extends ListActivity {
    private List<String> directoryEntries = new ArrayList<String>();
    public static String addLocation="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        File directory = new File(i.getStringExtra("directory"));
        if (directory.isDirectory()){
            File[] files = directory.listFiles();
            //sort in descending date order
            Arrays.sort(files, new Comparator<File>(){
                public int compare(File f1, File f2) {
                    return -Long.valueOf(f1.lastModified())
                            .compareTo(f2.lastModified());
                }
            });
            //fill list with files
            this.directoryEntries.clear();
            for (File file : files){
                this.directoryEntries.add(file.getPath());
            }
            ArrayAdapter<String> directoryList
                    = new ArrayAdapter<String>(this,
                    R.layout.activity_list_view_loader, this.directoryEntries);
            //alphabetize entries
            //directoryList.sort(null);
            this.setListAdapter(directoryList);
        }
    }
    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        File clickedFile = new File(this.directoryEntries.get(pos));
        Intent i = getIntent();
        i.putExtra("clickedFile", clickedFile.toString());
        setResult(RESULT_OK, i);
        finish();
    }
}
