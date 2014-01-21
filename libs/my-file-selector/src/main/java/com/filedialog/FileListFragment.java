package com.filedialog;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.widget.ListView;
import android.view.View;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;


import java.io.File;
import java.util.List;


public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<File>>{

    public static final String PATH = "path";
    public static final String EXTENSIONS = "extensions";
    private static final String[] DEFAULT_EXTENSIONS = {"midi"};
    private FileListAdapter mAdapter;
    private String mPath;
    private String mExtension;
    private CallBacks mListener;
    private String midi;

    public interface CallBacks {
        public void onFileSelected(File file);
    }

    public static FileListFragment newInstance(String path, String[] extensionsToShow){
        FileListFragment frag = new FileListFragment();
        Bundle args = new Bundle();
        args.putString(PATH,path);
        args.putStringArray(EXTENSIONS,extensionsToShow);
        frag.setArguments(args);
        Log.e("Will", "Trying to make fragment for " + path);
        return frag;
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);


        try {
            mListener = (CallBacks) activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callbacks");
        }
    }

    @Override
    public void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mAdapter = new FileListAdapter(getActivity());
        if(getArguments() == null) Log.e("Will", "Arguments null");
        mPath = getArguments() != null ? getArguments().getString(PATH):
            Environment.getExternalStorageDirectory().getAbsolutePath();
        mExtension = getArguments() != null ? getArguments().getStringArray(EXTENSIONS):
                DEFAULT_EXTENSIONS;
        Log.e("Will", "Created fragment for " + mPath);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        setEmptyText(getString(R.string.empty_dir));
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int index, long id){
        FileListAdapter adapter = (FileListAdapter) lv.getAdapter();
        if(adapter != null){
            File f = adapter.getItem(index);
            mPath = f.getAbsolutePath();
            mListener.onFileSelected(f);
        }
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle args){
        Log.e("Will", "Loader created with " + mPath);
        return new FileLoader(getActivity(),mPath,mExtensions);
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data){
        for(File f : data){
            Log.e("Will", "Loader returned: " f.toString());
        }
        mAdapter.setListItems(data);
        if(isResumed()){
            setListShown(true);
        } else{
            setListShownNoAnimation(true);
        }
    }
    @Override
    public void onLoaderReset(Loader<List<File>> loader){  
        Log.e("Will","Clearing adapter");
        mAdapter.clear();
    }
}
