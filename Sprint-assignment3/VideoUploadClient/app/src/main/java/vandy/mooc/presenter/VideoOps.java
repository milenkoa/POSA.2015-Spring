package vandy.mooc.presenter;

import java.lang.ref.WeakReference;
import java.util.List;

import vandy.mooc.R;
import vandy.mooc.common.ConfigurableOps;
import vandy.mooc.common.GenericAsyncTask;
import vandy.mooc.common.GenericAsyncTaskOps;
import vandy.mooc.common.Utils;
import vandy.mooc.model.provider.Video;
import vandy.mooc.model.provider.VideoContract;
import vandy.mooc.model.provider.VideoController;
import vandy.mooc.model.services.UploadVideoService;
import vandy.mooc.view.ShowVideoActivity;
import vandy.mooc.view.VideoListActivity;
import vandy.mooc.view.ui.VideoAdapter;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * This class implements all the Video-related operations.  It
 * implements ConfigurableOps so it can be created/managed by the
 * GenericActivity framework.  It implements Callback so it can serve
 * as the target of an asynchronous Retrofit RPC call.  It extends
 * GenericAsyncTaskOps so its doInBackground() method runs in a
 * background task.
 */
public class VideoOps
       implements ConfigurableOps,
                  GenericAsyncTaskOps<Void, Void, Cursor> {
    /**
     * Debugging tag used by the Android logger.
     */
    private static final String TAG =
        VideoOps.class.getSimpleName();
    
    /**
     * Used to enable garbage collection.
     */
    private WeakReference<VideoListActivity> mActivity;
    
    /**
     *  It allows access to application-specific resources.
     */
    private Context mApplicationContext;
    
    /**
     * The GenericAsyncTask used to expand an Video in a background
     * thread via the Video web service.
     */
    private GenericAsyncTask<Void, Void, Cursor, VideoOps> mAsyncTask;
    
    /**
     * VideoController mediates the communication between Server and
     * Android Storage.
     */
    VideoController mVideoController;
    
    /**
     * The ListView that contains a list of Videos available from
     * Server.
     */
    private ListView mVideosList;
    
    /**
     * The Adapter that is needed by ListView to show the list of
     * Videos.
     */
    //private VideoAdapter mAdapter;
    private SimpleCursorAdapter mAdapter;

    private List<Video> mCurrentVideoList;
    
    /**
     * Default constructor that's needed by the GenericActivity
     * framework.
     */
    public VideoOps() {
    }
    
    /**
     * Called after a runtime configuration change occurs to finish
     * the initialisation steps.
     */
    public void onConfiguration(Activity activity,
                                boolean firstTimeIn) {
        final String time =
            firstTimeIn 
            ? "first time" 
            : "second+ time";
        
        Log.d(TAG,
              "onConfiguration() called the "
              + time
              + " with activity = "
              + activity);

        // (Re)set the mActivity WeakReference.
        mActivity =
            new WeakReference<>((VideoListActivity) activity);
        
        // Get reference to the ListView for displaying the results
        // entered.
        mVideosList =
            (ListView) mActivity.get().findViewById(R.id.videoList);

        if (firstTimeIn) {
            // Get the Application Context.
            mApplicationContext =
                activity.getApplicationContext();
            
            // Create VideoController that will mediate the
            // communication between Server and Android Storage.
            mVideoController =
                new VideoController(mApplicationContext);
            
            // Create a local instance of our custom Adapter for our
            // ListView.
            //mAdapter = new VideoAdapter(mApplicationContext);
            mAdapter = makeCursorAdapter();
            //mVideosList.setAdapter(mAdapter);
            
            // Get the VideoList from Server. 
            getVideoList();
        }
        
       // Set the adapter to the ListView.
       mVideosList.setAdapter(mAdapter);
    }

    /**
     * Display the Videos in ListView
     * 
     * @param videos
     */
    public void displayVideoList(Cursor videos) {
        if (videos != null) {
            // Update the adapter with the List of Videos.
            mAdapter.changeCursor(videos);
            Utils.showToast(mActivity.get(),
                            "Video meta-data loaded from Video Service");
        } else {
            Utils.showToast(mActivity.get(),
                           "Please connect to the Video Service");
            
            mActivity.get().finish();
        }

    }
    
    /**
     * Gets the VideoList from Server by executing the AsyncTask to
     * expand the acronym without blocking the caller.
     */
    public void getVideoList(){
        mAsyncTask = new GenericAsyncTask<>(this);
        mAsyncTask.execute();
    }
    
    /**
     * Start a service that Uploads the Video having given Id.
     *   
     * @param videoId
     */
    public void uploadVideo(Long videoId, Uri videoUri){
        mApplicationContext.startService
            (UploadVideoService.makeIntent 
                 (mApplicationContext,
                  videoId,
                  videoUri));
    }


    /**
     * Starts ShowVideoActivity passing all Video data in Intent
     *
     * @param i
     */
    public void startShowVideoActivity(int i) {
        Video video = mCurrentVideoList.get(i);
        Intent intent = ShowVideoActivity.makeIntent(mApplicationContext, video);
        mApplicationContext.startActivity(intent);

    }
    
    /**
     * Retrieve the List of Videos by help of VideoController via a
     * synchronous two-way method call, which runs in a background
     * thread to avoid blocking the UI thread.
     */
    @Override
    public Cursor doInBackground(Void... params) {
        //return mVideoController.getVideoList();
        Log.d(TAG, "doInBackground() in");
        List<Video> videos = mVideoController.getVideoList();
        Log.d(TAG, "doInBackGround() - retrieved list of videos from server");
        ContentResolver cr = mApplicationContext.getContentResolver();

        //delete everything
        cr.delete(VideoContract.VideoEntry.CONTENT_URI, null, null);

        for (Video v : videos) {

            //check if video already exists
            Cursor c = cr.query(VideoContract.VideoEntry.CONTENT_URI,
                    new String[] {VideoContract.VideoEntry.COLUMN_TITLE},
                    VideoContract.VideoEntry.COLUMN_TITLE + "='" + v.getTitle() + "'",
                    null,
                    null);

            if (c.getCount() == 0) {
                Log.d(TAG, "doInBackground() - video " + v.getTitle() + " is not in VideoContentProvider");
                Log.d(TAG, "doInBackground() - storing video " + v.toString());
                ContentValues cvs = VideoContract.makeContentValuesFromVideo(v);
                cr.insert(VideoContract.VideoEntry.CONTENT_URI, cvs);
            }
            else {
                Log.d(TAG, "doInBackground() - video " + v.getTitle() + " is already in VideoContentProvider");
            }
        }
        mCurrentVideoList = videos;

        Cursor c = cr.query(VideoContract.VideoEntry.CONTENT_URI,
                new String[] {VideoContract.VideoEntry._ID, VideoContract.VideoEntry.COLUMN_TITLE},
                null, null, null);
        return c;
    }

    /**
     * Display the results in the UI Thread.
     */
    @Override
    public void onPostExecute(Cursor videos) {
        displayVideoList(videos);
    }

    public SimpleCursorAdapter makeCursorAdapter() {
        return new SimpleCursorAdapter(
                mApplicationContext,
                R.layout.video_list_item,
                null,
                new String[] {VideoContract.VideoEntry.COLUMN_TITLE},
                new int[] {R.id.tvVideoTitle},
                1);
    }

}
