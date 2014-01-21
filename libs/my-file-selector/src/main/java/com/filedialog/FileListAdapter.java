package com.filedialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.content.Context;

import java.util.List;
import java.util.ArrayList;
import java.io.File;


public class FileListAdapter extends BaseAdapter implements ListAdapter {

    private final LayoutInflater mInflater;
    private List<File> mFiles =  new ArrayList<File>();

    public FileListAdapter(Context c){
        mInflater = LayoutInflater.from(c);
    }

    public void add(File f){
        mFiles.add(f);
        notifyDataSetChanged();
    }

    public void remove(File f){
        mFiles.remove(f);
        notifyDataSetChanged();
    }

    public void insert(File f, int index){
        mFiles.add(index,f);
        notifyDataSetChanged();
    }

    public void clear(){
        mFiles.clear();
        notifyDataSetChanged();
    }

    @Override
    public File getItem(int index){
        return mFiles.get(index);
    }

    @Override
    public long getItemId(int index){
        return index;
    }

    @Override
    public int getCount(){
        return mFiles.size();
    }

    public List<File> getListItems(){
        return mFiles;
    }


    public void setListItems(List<File> files){
        mFiles = files;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int index, View row, ViewGroup parent){
        View mRow = view;
        if(mRow == null) mRow = mInflater.inflate(R.layout.file_item, parent,false);

        File file = getItem(index); 
        
        setText(mRow,R.id.file_title,file.getName());
        setText(mRow,R.id.file_size,file.length());
        setText(mRow,R.id.file_extra,"EXTRA INFO");
        setIcon(mRow,R.id.file_icon, R.drawable.music_icon);
    }

    private void setIcon(View view, int icon_view, int icon){
        ImageView i = (ImageView) view.findViewById(icon_view);
        i.setImageResource(icon);
    }
    private void setText(View view,int view_id, String s){
        TextView text = (TextView) view.findViewById(view_id);
        text.setText(s);
    }

}
