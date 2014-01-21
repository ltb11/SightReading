package com.filedialog;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Created by wrd11 on 20/01/14.
 */
public class FileLoader extends AsyncTaskLoader<List<File>> {

    private final String mPath;
    private Set<String> mExtensions;

    public FileLoader(Context c, String path, String[] extensions){
        super(c);
        this.mPath = path; 
        mExtensions = new HashSet<String>(Arrays.asList(extensions));
    }
    @Override
    public List<File> loadInBackground() {
        ArrayList<File> files = new ArrayList<File>();
        Log.e("Will", "Trying to load files at " + mPath);
        final File path = new File(mPath);

        final File[] dir = path.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && checkExtension(file);
            }
        });
        if(dir!=null){
            for(File f : dir){
                files.add(f);
            }
        }
        return files;
    }
    
    private boolean checkExtension(File file){
        String uri = file.toString();
        String extension = uri.substring(uri.lastIndexOf(".")+1,uri.length()-1);
        return mExtensions.contains(extension);
    }

}
