package vandy.mooc.model.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * VideoDatabaseHelper is used by VideoProvider for creating and opening
 * SQLite database
 */
public class VideoDatabaseHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "VideoDatabase";

    private static int DB_VERSION = 1;

    final String SQL_CREATE_VIDEO_TABLE =
            "CREATE TABLE "
                    + VideoContract.VideoEntry.TABLE_NAME + " ("
                    + VideoContract.VideoEntry._ID + " INTEGER PRIMARY KEY, "
                    + VideoContract.VideoEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                    + VideoContract.VideoEntry.COLUMN_DURATION + " TEXT NOT NULL, "
                    + VideoContract.VideoEntry.COLUMN_CONTENT_TYPE+ " TEXT NOT NULL, "
                    + VideoContract.VideoEntry.COLUMN_DATA_URL + " TEXT NOT NULL, "
                    + VideoContract.VideoEntry.COLUMN_STAR_RATING+ " INTEGER NOT NULL "
                    + " );";

    public VideoDatabaseHelper(Context context) {
        super(context,
                context.getCacheDir() + File.separator + DB_NAME,
                null,
                DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEO_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        // Delete the existing tables.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "
                + VideoContract.VideoEntry.TABLE_NAME);
        // Create the new tables.
        onCreate(sqLiteDatabase);
    }
}
