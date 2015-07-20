package vandy.mooc.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
//import android.widget.Button;
//import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

//import vandy.mooc.R;
import vandy.mooc.common.ConfigurableOps;
import vandy.mooc.common.GenericAsyncTask;
import vandy.mooc.common.GenericAsyncTaskOps;
//import vandy.mooc.model.provider.Video;
import vandy.mooc.model.provider.VideoController;
import vandy.mooc.model.services.DownloadVideoService;
import vandy.mooc.view.ShowVideoActivity;

/**
 * This class implements all the Video-related operations.  It
 * implements ConfigurableOps so it can be created/managed by the
 * GenericActivity framework. It extends GenericAsyncTaskOps so its
 * doInBackground() method runs in a background task.
 */

public class ShowVideoOps implements ConfigurableOps, GenericAsyncTaskOps<Integer, Void, Integer> {

    private final String TAG = getClass().getSimpleName();

    private WeakReference<ShowVideoActivity> mActivity;
    //private WeakReference<ImageView> mVideoThumbnailView;

    private Context mApplicationContext;
    VideoController mVideoController;

    private GenericAsyncTask<Integer, Void, Integer, ShowVideoOps> mAsyncTask;

    private RatingBar mRatingView;

    public ShowVideoOps() {

    }

    public VideoController getVideoController() {
        return mVideoController;
    }

    @Override
    public void onConfiguration(Activity activity, boolean firstTimeIn) {
        final String time =
                firstTimeIn
                        ? "first time"
                        : "second+ time";

        Log.d(TAG,
                "onConfiguration() called the "
                        + time
                        + " with activity = "
                        + activity);

        mActivity = new WeakReference<ShowVideoActivity>((ShowVideoActivity) activity);
        //mVideoThumbnailView = new WeakReference<ImageView> ((ImageView)) activity.findViewById(R.id.videoThumbnail);


        if (firstTimeIn) {
            mApplicationContext =
                    activity.getApplicationContext();
            mVideoController =
                    new VideoController(mApplicationContext);
        }
    }

    /**
     * Checks Video Media Store if video with given title is already stored locally
     *
     * @param title
     * @return
     */
    public boolean isVideoInMediaStore(String title) {
        Log.d(TAG, "isVideoInMediaStore() in, title: " + title);

        Cursor c = mApplicationContext.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Video.Media.DISPLAY_NAME},
                MediaStore.Video.Media.DISPLAY_NAME + "='" + title +"'",null, null
        );

        if (c.getCount() == 0) {
            Log.d(TAG, "isVideoInMediaStore(), video not found");
            return false;
        }
        Log.d(TAG, "isVideoInMediaStore(), found video");
        return true;
    }

    /**
     * Retrieves thumbnail from Video Media Store if video with given title is already
     * stored locally
     *
     * @param title
     * @return
     */
    public Bitmap getVideoThumbnail(String title) {
        Log.d(TAG, "getVideoThumbnail() in, title: " + title);

        Cursor c = mApplicationContext.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DISPLAY_NAME + "='" + title + "'", null, null
        );

        if (c.getCount() == 0) {
            Log.d(TAG, "getVideoThumbnail(), video not found");
            return null;
        }
        c.moveToFirst();
        int id = c.getInt(0);
        Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(mApplicationContext.getContentResolver(),
            id, MediaStore.Video.Thumbnails.MINI_KIND, null);
        return thumbnail;
    }

    /**
     * Creates an instance of RateVideoCommand and executes it in worker thread
     * RateVideoCommand makes a Retrofit call to the server
     *
     * @param id
     * @param rating
     */
    public void rateVideo(int id, int rating) {
        Log.d(TAG, "rateVideo() in - id: " + id + ", rating: " + rating);

        new RateVideoCommand(this, (long) id, rating).run();

    }

    /**
     * Updates UI with rating received back from server
     *
     * @param rating
     */
    public void updateRating(int rating) {
        Log.d(TAG, "updateRating() in, rating: " + rating);

        Toast.makeText(mApplicationContext, "Average rating returned from server: " + rating, Toast.LENGTH_LONG).show();
        mActivity.get().updateRating(rating);
    }

    /**
     * Starts DownloadVideoService and passes video ID and video name in intent
     * Intent is created using DowbloadVideoService().makeIntent factory method
     *
     * @param videoId
     * @param videoName
     */
    public void downloadVideo(long videoId, String videoName) {

        Log.d(TAG, "downloadVideo() in - videoId: " + videoId + ", videoName: " + videoName);
        mApplicationContext.startService
                (DownloadVideoService.makeIntent
                        (mApplicationContext,
                                videoId,
                                videoName));

    }

    /**
     * Plays a video using implicit intent
     *
     * @param videoName
     */
    public void playVideo(String videoName) {
        Log.d(TAG, "playVideo() in - videoName: " + videoName);
        File path =
                Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path,
                videoName);

        Log.d(TAG, "playVideo() on location: " + file.getAbsolutePath());
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.fromFile(file),"video/*");
        mApplicationContext.startActivity(i);
    }

    /**
     * From GenericAsyncTaskOps - not used!!!
     * @param params
     * @return
     */
    @Override
    public Integer doInBackground(Integer... params) {
        Log.d(TAG, "doInBackground() in");
        return 0;
    }

    /**
     * From GenericAsyncTaskOps - not used!!!
     * @param integer
     */
    @Override
    public void onPostExecute(Integer integer) {

    }
}
