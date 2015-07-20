package vandy.mooc.model.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Video Contract class defines all constants that describe VideoProvider content provider
 */
public class VideoContract {

    public static final String CONTENT_AUTHORITY;

    static {
        CONTENT_AUTHORITY = "vandy.mooc.videoprovider";
    }

    public static final Uri BASE_CONTENT_URI =
            Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_VIDEO =
           VideoEntry.TABLE_NAME;

    public static final class VideoEntry implements BaseColumns {

        public static final String TABLE_NAME = "video";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_CONTENT_TYPE = "contentType";
        public static final String COLUMN_DATA_URL = "data_url";
        public static final String COLUMN_STAR_RATING = "star_rating";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().
                        appendPath(PATH_VIDEO).build();

        public static final String CONTENT_ITEMS_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static final int VIDEOS = 100;
        public static final int VIDEO = 101;

        public static final UriMatcher sUriMatcher =
                buildUriMatcher();

        protected static UriMatcher buildUriMatcher() {
            final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

            matcher.addURI(
                    VideoContract.CONTENT_AUTHORITY,
                    VideoContract.PATH_VIDEO,
                    VIDEOS
            );

            matcher.addURI(
                    VideoContract.CONTENT_AUTHORITY,
                    VideoContract.PATH_VIDEO + "/#",
                    VIDEO
            );
            return matcher;
        }

        public static Uri buildUri(Long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static ContentValues makeContentValuesFromVideo(Video video) {
        ContentValues cvs = new ContentValues();
        cvs.put(VideoEntry.COLUMN_TITLE, video.getTitle());
        cvs.put(VideoEntry.COLUMN_DURATION, video.getDuration());
        cvs.put(VideoEntry.COLUMN_CONTENT_TYPE, video.getContentType());
        cvs.put(VideoEntry.COLUMN_DATA_URL, video.getDataUrl());
        cvs.put(VideoEntry.COLUMN_STAR_RATING, video.getRating());
        return cvs;
    }
}
