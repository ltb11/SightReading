package com.filedialog;

import android.support.v4.content.AsyncTaskLoader;
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
    private final String mExtensions;

    public FileLoader(Context c, String path, String extensions){
        super(c);
        this.mPath = path;
        this.mExtensions = extensions;
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
