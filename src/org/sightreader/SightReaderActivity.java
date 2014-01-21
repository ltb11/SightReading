package org.sightreader;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;


import com.filedialog.FileListFragment;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;

import utils.OurUtils;

public class SightReaderActivity extends FragmentActivity implements FileListFragment.CallBacks {
	public static final String TAG = "SightReaderActivity";
    private Button scan;
	private Button play;
    private static final int CAMERA_REQUEST = 1888;
    private static final int FILE_DIALOG_REQUEST = 1066;
	public final static long startTime = System.currentTimeMillis();
    private ActionBar actionBar;
    private DrawerLayout mDrawerLayout;
    private String[] mOptions;
    private ListView mDrawerListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private FragmentManager mFragmentManager;
    private FrameLayout mContent;

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};


    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sight_reading_surface_view);

        actionBar = getActionBar();
		initialiseDrawer();


        mFragmentManager = getSupportFragmentManager();
        mContent = (FrameLayout) findViewById(R.id.content_frame);

        if(mContent != null){
            if(savedInstanceState == null){
                FileListFragment fileList = FileListFragment.newInstance(OurUtils.getPath("midi/"),"midi");
                mFragmentManager.beginTransaction().add(R.id.content_frame,fileList).commit();
            }
        }

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this,
				mOpenCVCallBack)) {
			Log.e("TEST", "Cannot connect to OpenCV Manager");
		}


		// Make sure the necessary folders exist
		(new File(OurUtils.getPath("") + File.separator + "input")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "midi")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "output")).mkdirs();
		(new File(OurUtils.getPath("") + File.separator + "assets")).mkdirs();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            Intent i = new Intent(SightReaderActivity.this,
                    ProcessingActivity.class);
            startActivity(i);
        }
    }

	private void initialiseDrawer() {
        mTitle = mDrawerTitle = getTitle();
        mOptions = getResources().getStringArray(R.array.drawer_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView= (ListView) findViewById(R.id.left_drawer);
        mDrawerListView.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item,mOptions));
        mDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle =new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(mDrawerTitle);
                invalidateOptionsMenu();

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
	}

    @Override
    public void onFileSelected(File file) {
        Intent i = new Intent(SightReaderActivity.this,PlaybackActivity.class);
        i.putExtra(PlaybackActivity.FILE_PATH,file.toURI().toString());
        startActivity(i);
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectItem(i);
        }
    }

    private void selectItem(int i){
        Log.i(TAG," selected " + i);
        switch(i){
            case 0:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = new File(OurUtils.getPath("temp/tmp.png"));
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                break;
            case 2:

                break;
            default:
                break;
        }
        mDrawerListView.setItemChecked(i, true);
        setTitle(mOptions[i]);
        mDrawerLayout.closeDrawer(mDrawerListView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen= mDrawerLayout.isDrawerOpen(mDrawerListView);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title){
        actionBar.setTitle(title);
    }


}
