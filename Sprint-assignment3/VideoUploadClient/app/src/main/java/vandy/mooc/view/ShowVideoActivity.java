package vandy.mooc.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import vandy.mooc.R;
import vandy.mooc.common.GenericActivity;
import vandy.mooc.common.LifecycleLoggingActivity;
import vandy.mooc.model.provider.Video;
import vandy.mooc.model.services.DownloadVideoService;
import vandy.mooc.presenter.ShowVideoOps;
import vandy.mooc.presenter.VideoOps;

/**
 *
 */
public class ShowVideoActivity extends GenericActivity<ShowVideoOps> {

    private final String TAG = getClass().getSimpleName();

    /* keys used to store video data into intent sent to this activity*/
    private static final String KEY_VIDEO_ID = "video_id";
    private static final String KEY_VIDEO_TITLE = "video_title";
    private static final String KEY_VIDEO_DURATION = "video_duration";
    private static final String KEY_VIDEO_RATING = "video_rating";
    private static final String KEY_VIDEO_URL = "video_url";

    /* video information: activity gets this from Intent (received from VideoListActivity */
    long m_Id;
    String m_Title;
    long m_Duration;
    int m_Rating;
    String m_DataUrl;

    /* UI elements */
    RatingBar m_RatingView;
    TextView m_TitleView;
    ImageButton m_PlayButton;
    ImageButton m_DownloadButton;
    ImageView m_ThumbnailView;

    /**
     * Broadcast Receiver that is registered to receive result from VideoDownloadService
     */
    private DownloadReceiver m_DownloadResultReceiver;

    /**
     * Factory method that creates an intent used to start this activity
     *
     * @param video
     * @return
     */
    public static Intent makeIntent(Context context, Video video) {

        Intent intent = new Intent(context, ShowVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_VIDEO_ID, video.getId());
        intent.putExtra(KEY_VIDEO_TITLE, video.getTitle());
        intent.putExtra(KEY_VIDEO_DURATION, video.getDuration());
        intent.putExtra(KEY_VIDEO_RATING, video.getRating());
        intent.putExtra(KEY_VIDEO_URL, video.getDataUrl());

        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_video);

        // registers broadcast receiver that gets result from DownloadVideoService
        registerReceiver();

        // get references to relevant UI elements that should be initialize in the code
        m_RatingView = (RatingBar) findViewById(R.id.ratingbar);
        m_TitleView = (TextView) findViewById(R.id.videoTitle);
        m_PlayButton = (ImageButton) findViewById(R.id.playButton);
        m_DownloadButton = (ImageButton) findViewById(R.id.downloadButton);
        m_ThumbnailView = (ImageView) findViewById(R.id.videoThumbnail);

        //call to GenericActivity: GenericActivity initialize ShowVideoOps instance
        super.onCreate(savedInstanceState,
                ShowVideoOps.class);

        // retrieve video information from Intent
        Intent intent = getIntent();
        /*m_Id = intent.getLongExtra("id", 0);
        m_Title = intent.getStringExtra("title");
        m_Duration = intent.getLongExtra("duration", 0);
        m_Rating = intent.getIntExtra("rating", 0);
        m_DataUrl = intent.getStringExtra("url");*/
        m_Id = intent.getLongExtra(KEY_VIDEO_ID, 0);
        m_Title = intent.getStringExtra(KEY_VIDEO_TITLE);
        m_Duration = intent.getLongExtra(KEY_VIDEO_DURATION, 0);
        m_Rating = intent.getIntExtra(KEY_VIDEO_RATING, 0);
        m_DataUrl = intent.getStringExtra(KEY_VIDEO_URL);


        // set UI element values with Video information
        m_RatingView.setRating(m_Rating);
        m_TitleView.setText(m_Title);

        m_RatingView.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean fromUser) {
                if (fromUser == true) {
                    Toast.makeText(ShowVideoActivity.this, "Thank you for rating: " + Float.toString(v), Toast.LENGTH_SHORT).show();
                    getOps().rateVideo((int) m_Id, (int) v);
                }

            }
        });

        // check if video is already present in Video Media Store
        // if video is present, show PLAY button
        // if video is not present, show DOWNLOAD button
        boolean bInVideoStore = getOps().isVideoInMediaStore(m_Title);

        if (bInVideoStore) {
            m_DownloadButton.setEnabled(false);
            m_DownloadButton.setBackgroundColor(getResources().getColor(R.color.button_disabled));
            m_DownloadButton.setVisibility(View.GONE);
        }
        else {
            m_PlayButton.setEnabled(false);
            m_PlayButton.setBackgroundColor(getResources().getColor(R.color.button_disabled));
            m_PlayButton.setVisibility(View.GONE);
        }

        // retrieve thumbnail for a Video from Video Media Store
        // TODO: this should probably go under if (bInVideoStore)
        Bitmap thumbnail = getOps().getVideoThumbnail(m_Title);

        if (thumbnail != null) {
            m_ThumbnailView.setImageBitmap(thumbnail);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unregister receiver
        unregisterReceiver(m_DownloadResultReceiver);
    }

    /**
     * Method invoked when PLAY button is clicked. It only forwards call to ShowVideoOps
     *
     * @param v
     */
    public void playVideo(View v) {
        Log.d(TAG, "playVideo() in");
        getOps().playVideo(m_Title);
    }

    /**
     * Method invoked when DOWNLOAD button is clicked. It only forwards call to ShowVideoOps
     *
     * @param v
     */
    public void downloadVideo(View v) {
        Log.d(TAG, "downloadVideo() in");
        getOps().downloadVideo(m_Id, m_Title);
    }

    /**
     * Implementation of broadcast receiver: DownloadReceiver.
     * It received information when download is completed.
     * onReceive method should enable PLAY functionality
     * and retrieve thumbnail for a downloaded image
     */
    public class DownloadReceiver extends BroadcastReceiver {



        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "DownloadReceiver onReceive()");
            Toast.makeText(ShowVideoActivity.this, "Download completed", Toast.LENGTH_SHORT).show();

            m_DownloadButton.setEnabled(false);
            m_DownloadButton.setBackgroundColor(ShowVideoActivity.this.getResources().getColor(R.color.button_disabled));
            m_DownloadButton.setVisibility(View.GONE);
            m_PlayButton.setEnabled(true);
            m_PlayButton.setBackgroundColor(ShowVideoActivity.this.getResources().getColor(R.color.theme_primary));
            m_PlayButton.setVisibility(View.VISIBLE);

            Bitmap thumbnail = getOps().getVideoThumbnail(m_Title);
            if (thumbnail != null) {
                m_ThumbnailView.setImageBitmap(thumbnail);
            }

        }
    }

    /**
     *
     */
    private void registerReceiver() {
        m_DownloadResultReceiver = new DownloadReceiver();

        IntentFilter intentFilter =
                new IntentFilter(DownloadVideoService.ACTION_DOWNLOAD_SERVICE_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Register the BroadcastReceiver.
        registerReceiver(m_DownloadResultReceiver,
                intentFilter);

    }

    /**
     * Invoked from ShowVideoOps to update rating
     * TODO: maybe this could be refactored and placed into ShowVideoOps
     *
     * @param rating
     */
    public void updateRating(int rating) {
        m_RatingView.setRating(rating);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
