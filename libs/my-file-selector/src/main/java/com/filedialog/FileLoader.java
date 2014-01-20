package com.filedialog;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wrd11 on 20/01/14.
 */
public class FileLoader extends AsyncTaskLoader<List<File>> {

    private final String mPath;

    public FileLoader(Context c, String path){
        super(c);
        this.mPath = path;
    }
    @Override
    public List<File> loadInBackground() {
        ArrayList<File> files = new ArrayList<File>();

        final File path = new File(mPath);

        final File[] dir = path.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && !file.getName().startsWith(".");
            }
        });
        if(dir!=null){
            for(File f : dir){
                files.add(f);
            }
        }

        return files;
    }
}
