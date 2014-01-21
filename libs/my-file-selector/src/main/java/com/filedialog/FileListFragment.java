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
    public static final String EXTENSION = "extension";
    private static final String DEFAULT_EXTENSION = "midi";
    private FileListAdapter mAdapter;
    private String mPath;
    private String mExtension;
    private CallBacks mListener;
    private String midi;

    public interface CallBacks {
        public void onFileSelected(File file);
    }

    public static FileListFragment newInstance(String path, String extensionToShow){
        FileListFragment frag = new FileListFragment();
        Bundle args = new Bundle();
        args.putString(PATH,path);
        args.putString(EXTENSION,extensionToShow);
        frag.setArguments(args);
        Log.e("Will", "Trying to make fragment for " + path);
        return frag;
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        Log.e("Will", "Trying to load fragment for " + mPath);

        try
        {
            mListener = (CallBacks) activity;
        }
        catch(ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement Callbacks");
        }
    }

    @Override
    public void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mAdapter = new FileListAdapter(getActivity());
        mPath = getArguments() != null ? getArguments().getString(PATH):
            Environment.getExternalStorageDirectory().getAbsolutePath();
        mExtension = getArguments() != null ? getArguments().getString(EXTENSION):
                DEFAULT_EXTENSION;
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

    public Loader<List<File>> onCreateLoader(int id, Bundle args){
        return new FileLoader(getActivity(),mPath,mExtension);
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data){
        mAdapter.setListItems(data);
        if(isResumed()){
            setListShown(true);
        } else{
            setListShownNoAnimation(true);
        }
    }
    @Override
    public void onLoaderReset(Loader<List<File>> loader){
        mAdapter.clear();
    }
}
