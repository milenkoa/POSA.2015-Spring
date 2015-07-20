package vandy.mooc.model.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import vandy.mooc.R;
import vandy.mooc.model.provider.VideoController;

/**
 *
 */
public class DownloadVideoService extends IntentService {

    private final String TAG = getClass().getSimpleName();

    /**
     * Custom Action that will be used to send Broadcast to the
     * ShowVideoActivity.
     */
    public static final String ACTION_DOWNLOAD_SERVICE_RESPONSE =
            "vandy.mooc.services.DownloadVideoService.RESPONSE";

    /**
     * Key, used to store the videoId as an EXTRA in Intent.
     */
    private static final String KEY_DOWNLOAD_VIDEO_ID =
            "download_videoId";

    /**
     * Default Id , if no Id is present in Extras of the received
     * Intent.
     */
    private static final long DEFAULT_VIDEO_ID = 0;

    private static final String KEY_DOWNLOAD_VIDEO_NAME =
            "download_videoName";
    /**
     * VideoController mediates the communication between Server and
     * Android Storage.
     */
    private VideoController mController;

    /**
     * Manages the Notification displayed in System UI.
     */
    private NotificationManager mNotifyManager;

    /**
     * Builder used to build the Notification.
     */
    private NotificationCompat.Builder mBuilder;


    /**
     * It is used by Notification Manager to send Notifications.
     */
    private static final int NOTIFICATION_ID = 1;


    public DownloadVideoService() {
        super("DownloadVideoService");
    }


    /**
     * Factory method that creates Intent to start DownloadVideoService
     *
     * @param context
     * @param videoId
     * @param videoName
     * @return
     */
    public static Intent makeIntent(Context context,
                                    long videoId, String videoName){
        return new Intent(context,
                DownloadVideoService.class)
                .putExtra(KEY_DOWNLOAD_VIDEO_ID,
                        videoId)
                .putExtra(KEY_DOWNLOAD_VIDEO_NAME, videoName);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent() in");
        // Starts the Notification to show the progress of video
        // download.
        startNotification();

        // Create VideoController that will mediates the communication
        // between Server and Android Storage.
        mController =
                new VideoController(getApplicationContext());

        // Get the videoId from the Extras of Intent.
        long videoId =
                intent.getLongExtra(KEY_DOWNLOAD_VIDEO_ID,
                        DEFAULT_VIDEO_ID);

        String videoName =
                intent.getStringExtra(KEY_DOWNLOAD_VIDEO_NAME);

        Log.d(TAG, "onHandleIntent() - videoID: " + videoId + ", videoName: " + videoName);

        // Check if Video Download is successful.
        if (mController.downloadVideo(videoId, videoName))
            finishNotification("Download complete");
        else
            finishNotification("Download failed");

        // Send the Broadcast to Activity that the Video Download is
        // completed.
        sendBroadcast();
    }

    /**
     * Send the Broadcast to Activity that the Video
     * download is completed.
     */
    private void sendBroadcast(){
        sendBroadcast(new Intent(ACTION_DOWNLOAD_SERVICE_RESPONSE)
                .addCategory(Intent.CATEGORY_DEFAULT));
    }

    /**
     * Finish the Notification after the Video is downloaded.
     *
     * @param status
     */
    private void finishNotification(String status) {
        // When the loop is finished, updates the notification.
        mBuilder.setContentText(status) ;

        // Removes the progress bar.
        mBuilder.setProgress (0,
                0,
                false);
        mNotifyManager.notify(NOTIFICATION_ID,
                mBuilder.build());
    }

    /**
     * Starts the Notification to show the progress of video download.
     */
    private void startNotification() {
        //Gets the access to System Notification Services.
        mNotifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the Notification and sets an activity indicator
        // for an operation of indeterminate length.
        mBuilder = new NotificationCompat
                .Builder(this)
                .setContentTitle("Video Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_file_download_black_48dp)
                .setProgress(0,
                        0,
                        true);

        // Issues the notification
        mNotifyManager.notify(NOTIFICATION_ID,
                mBuilder.build());
    }
}
