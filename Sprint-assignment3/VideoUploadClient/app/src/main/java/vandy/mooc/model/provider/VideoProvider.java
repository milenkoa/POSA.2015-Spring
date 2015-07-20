package vandy.mooc.model.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

/**
 * VideoProvider is Content Provider
 * It implements all common patterns for content provider development
 *
 * Information stored into content provider are (see VideoContract)
 *
 * _ID (note this is not ID assigned to the video by the server)
 * TITLE
 * DURATION
 * CONTENT_TYPE
 * DATA_URL
 * RATING
 *
 */
public class VideoProvider extends ContentProvider {

    private VideoDatabaseHelper mOpenHelper;

    public VideoProvider() {
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new VideoDatabaseHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted = 0;

        switch (VideoContract.VideoEntry.sUriMatcher.match(uri)) {
            case VideoContract.VideoEntry.VIDEOS:
                rowsDeleted = db.delete(
                        VideoContract.VideoEntry.TABLE_NAME,
                        selection, selectionArgs
                );

            break;

            case VideoContract.VideoEntry.VIDEO:
                rowsDeleted = db.delete(
                        VideoContract.VideoEntry.TABLE_NAME,
                        addKeyIdCheckToWhereStatement(selection, ContentUris.parseId(uri)),
                        selectionArgs
                );

            break;
            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);

        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {

        switch (VideoContract.VideoEntry.sUriMatcher.match(uri)) {
            case VideoContract.VideoEntry.VIDEOS:
                return VideoContract.VideoEntry.CONTENT_ITEMS_TYPE;

            case VideoContract.VideoEntry.VIDEO:
                return VideoContract.VideoEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);

        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Uri returnUri;

        switch (VideoContract.VideoEntry.sUriMatcher.match(uri)) {
            case VideoContract.VideoEntry.VIDEOS:
                long id = db.insert(VideoContract.PATH_VIDEO, null, values);

                if (id > 0)
                    returnUri = VideoContract.VideoEntry.buildUri(id);
                else
                    throw new UnsupportedOperationException("Failed to insert");
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);

        }

        return returnUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        switch (VideoContract.VideoEntry.sUriMatcher.match(uri)) {
            case VideoContract.VideoEntry.VIDEOS:
                return db.query(
                        VideoContract.VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null, sortOrder);

            case VideoContract.VideoEntry.VIDEO:
                return db.query(
                        VideoContract.VideoEntry.TABLE_NAME,
                        projection,
                        addKeyIdCheckToWhereStatement(selection, ContentUris.parseId(uri)),
                        selectionArgs,
                        null, null, sortOrder);

            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);

        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (VideoContract.VideoEntry.sUriMatcher.match(uri)) {
            case VideoContract.VideoEntry.VIDEOS:
                rowsUpdated = db.update(
                        VideoContract.VideoEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
            );

            break;

            case VideoContract.VideoEntry.VIDEO:
                rowsUpdated = db.update(
                        VideoContract.VideoEntry.TABLE_NAME,
                        values,
                        addKeyIdCheckToWhereStatement(selection, ContentUris.parseId(uri)),
                        selectionArgs
                );

            break;
            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);

        }
        return rowsUpdated;
    }

    private static String addKeyIdCheckToWhereStatement(String whereStatement,
                                                        long id) {
        String newWhereStatement;
        if (TextUtils.isEmpty(whereStatement))
            newWhereStatement = "";
        else
            newWhereStatement = whereStatement + " AND ";

        // Append the key id to the end of the WHERE statement.
        return newWhereStatement
                + VideoContract.VideoEntry._ID
                + " = '"
                + id
                + "'";
    }
}
