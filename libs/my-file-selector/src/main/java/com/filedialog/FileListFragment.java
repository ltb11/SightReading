package com.filedialog;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import android.widget.ListView;
import android.view.View;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;


import java.io.File;
import java.util.List;

public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<File>>{

    public static final String PATH = path;
    private FileListAdapter mAdapter;
    private String mPath;

    public static FileListFragment newInstance(String path){
        FileListFragment frag = new FileListFragment();
        Bundle args = new Bundle();
        args.putString(this.PATH,path);
        frag.setArguments(args);
        return frag;
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
    }

    @Override
    public void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mAdapter = new FileListAdapter(getActivity());
        mPath = getArguments() != null ? getArguments().getString(this.PATH):
            Environment.getExternalStorageDirectory.getAbsolutePath();
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
            File f = (File) adapter.getItem(index);
            mPath = file.getAbsolutePath();
            mListener.onFileSelected(f);
        }
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id,Bundle args){
        return new FileLoader(getActivity(),mPath);
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
