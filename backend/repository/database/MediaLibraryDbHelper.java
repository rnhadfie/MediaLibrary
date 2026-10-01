package repository.database;

import static utils.DatabaseKeyNames.*;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MediaLibraryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MediaLibrary.db";
    private static final int DATABASE_VERSION = 1;

    public MediaLibraryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_OTHER_ITEMS, null, null);
            db.delete(TABLE_OTHERS, null, null);
            db.delete(TABLE_VIDEO_ITEMS, null, null);
            db.delete(TABLE_VIDEOS, null, null);
            db.delete(TABLE_MUSIC, null, null);
            db.delete(TABLE_BOOK_ITEMS, null, null);
            db.delete(TABLE_BOOKS, null, null);
            db.delete(TABLE_PUBLISHERS, null, null);
            db.delete(TABLE_TAGS, null, null);

            db.execSQL("DELETE FROM sqlite_sequence WHERE name IN ('" +
                    TABLE_TAGS + "', '" + TABLE_PUBLISHERS + "', '" + TABLE_BOOKS + "', '" +
                    TABLE_BOOK_ITEMS + "', '" + TABLE_MUSIC + "', '" + TABLE_VIDEOS + "', '" +
                    TABLE_VIDEO_ITEMS + "', '" + TABLE_OTHERS + "', '" + TABLE_OTHER_ITEMS + "')");

            // Drops all tables
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_TAGS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_PUBLISHERS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_BOOKS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_BOOK_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_MUSIC);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_VIDEOS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_VIDEO_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_OTHERS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_OTHER_ITEMS);

            //Recreates them
            onCreate(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tag table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TAGS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY , " +
                COLUMN_NAME + " TEXT NOT NULL," +
                "UNIQUE (" + COLUMN_ID + ", " + COLUMN_NAME + "))"
        );

        // Publisher table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PUBLISHERS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY , " +
                COLUMN_NAME + " TEXT NOT NULL," +
                "UNIQUE (" + COLUMN_ID + ", " + COLUMN_NAME + "))"
        );

        // region Book table

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BOOKS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_COLLECTING + " INTEGER, " +
                COLUMN_ONGOING + " INTEGER, " +
                COLUMN_COLLECTED + " INTEGER, " +
                COLUMN_TAG + " INTEGER, " +
                COLUMN_COVER + " BLOB, " +
                COLUMN_GENRE + " TEXT, " +
                COLUMN_AUTHOR + " TEXT, " +
                COLUMN_ARTIST + " TEXT, " +
                COLUMN_TYPE + " INTEGER, " +
                COLUMN_PUBLISHER + " INTEGER, " +
                COLUMN_COLLECTING_PRIORITY + " INTEGER, " +
                "UNIQUE (" + COLUMN_ID + ", " + COLUMN_TITLE +", " + COLUMN_AUTHOR + ", " + COLUMN_TYPE + ")," +
                "FOREIGN KEY(" + COLUMN_TAG + ") REFERENCES " + TABLE_TAGS + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY(" + COLUMN_PUBLISHER + ") REFERENCES " + TABLE_PUBLISHERS + "(" + COLUMN_ID + "))");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BOOK_ITEMS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_SERIES + " INTEGER, " +
                COLUMN_VOLUME_NUMBER + " TEXT, " +
                COLUMN_VOLUME_TITLE + " TEXT, " +
                COLUMN_READ + " INTEGER, " +
                COLUMN_OWNED + " INTEGER, " +
                COLUMN_FORMAT + " INTEGER, " +
                COLUMN_ITEM_COVER + " BLOB, " +
                "FOREIGN KEY(" + COLUMN_SERIES + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_ID + "))");

        //endregion

        //region Music table

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MUSIC + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_COLLECTING + " INTEGER, " +
                COLUMN_ONGOING + " INTEGER, " +
                COLUMN_COLLECTED + " INTEGER, " +
                COLUMN_TAG + " INTEGER, " +
                COLUMN_COVER + " BLOB, " +
                COLUMN_GENRE + " TEXT, " +
                COLUMN_ARTIST + " TEXT, " +
                COLUMN_COLLECTING_PRIORITY + " INTEGER, " +
                "Year INTEGER, " +
                COLUMN_PUBLISHER + " INTEGER, " +
                "UNIQUE (" + COLUMN_ID + ", " + COLUMN_TITLE +", " + COLUMN_ARTIST + ")," +
                "FOREIGN KEY(" + COLUMN_TAG + ") REFERENCES " + TABLE_TAGS + "(" + COLUMN_ID + "))");

        //endregion

        // region Video table

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_VIDEOS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY , " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_COLLECTING + " INTEGER, " +
                COLUMN_ONGOING + " INTEGER, " +
                COLUMN_COLLECTED + " INTEGER, " +
                COLUMN_TAG + " INTEGER, " +
                COLUMN_COVER + " BLOB, " +
                COLUMN_GENRE + " TEXT, " +
                COLUMN_TYPE + " INTEGER, " +
                COLUMN_VIDEO_TAG + " INTEGER, " +
                COLUMN_COLLECTING_PRIORITY + " INTEGER, " +
                "UNIQUE (" + COLUMN_ID + ", " + COLUMN_TITLE +", " + COLUMN_VIDEO_TAG + ", " + COLUMN_TYPE + ")," +
                "FOREIGN KEY(" + COLUMN_TAG + ") REFERENCES " + TABLE_TAGS + "(" + COLUMN_ID + "))");

        // VideoItem table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_VIDEO_ITEMS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY , " +
                COLUMN_SERIES + " INTEGER, " +
                COLUMN_DISC_NUMBER + " INTEGER, " +
                COLUMN_DISC_TITLE + " TEXT, " +
                COLUMN_WATCHED + " INTEGER, " +
                COLUMN_OWNED + " INTEGER, " +
                COLUMN_FORMAT + " INTEGER, " +
                COLUMN_ITEM_COVER + " BLOB, " +
                "FOREIGN KEY(" + COLUMN_SERIES + ") REFERENCES " + TABLE_VIDEOS + "(" + COLUMN_ID + "))");

        //endregion

        //region Other table

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_OTHERS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY , " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_COLLECTING + " INTEGER, " +
                COLUMN_ONGOING + " INTEGER, " +
                COLUMN_COLLECTED + " INTEGER, " +
                COLUMN_TAG + " INTEGER, " +
                COLUMN_COVER + " BLOB, " +
                COLUMN_GENRE + " TEXT, " +
                COLUMN_PUBLISHER + " INTEGER, " +
                COLUMN_COLLECTING_PRIORITY + " INTEGER, " +
                "UNIQUE (" + COLUMN_ID + ", " + COLUMN_TITLE + ")," +
                "FOREIGN KEY(" + COLUMN_TAG + ") REFERENCES " + TABLE_TAGS + "(" + COLUMN_ID + "))");

        // OtherItem table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_OTHER_ITEMS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_SERIES + " INTEGER, " +
                COLUMN_ITEM_COVER + " BLOB, " +
                "FOREIGN KEY(" + COLUMN_SERIES + ") REFERENCES " + TABLE_OTHERS + "(" + COLUMN_ID + "))");

        //endregion
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        if(newVersion > oldVersion) {

            //Create temporary tables
            db.execSQL("ALTER TABLE " + TABLE_OTHER_ITEMS + " RENAME TO " + TABLE_OTHER_ITEMS+ "_TEMP ;");
            db.execSQL("ALTER TABLE " + TABLE_OTHERS + " RENAME TO " + TABLE_OTHERS+ "_TEMP ;");
            db.execSQL("ALTER TABLE " + TABLE_VIDEO_ITEMS + " RENAME TO " + TABLE_VIDEO_ITEMS+ "_TEMP ;");
            db.execSQL("ALTER TABLE " + TABLE_VIDEOS + " RENAME TO " + TABLE_VIDEOS+ "_TEMP ;");
            db.execSQL("ALTER TABLE " + TABLE_MUSIC + " RENAME TO " + TABLE_MUSIC+ "_TEMP ;");
            db.execSQL("ALTER TABLE " + TABLE_BOOK_ITEMS + " RENAME TO " + TABLE_BOOK_ITEMS+ "_TEMP ;");
            db.execSQL("ALTER TABLE " + TABLE_BOOKS + " RENAME TO " + TABLE_BOOKS+ "_TEMP ;");
            db.execSQL("ALTER TABLE " + TABLE_PUBLISHERS + " RENAME TO " + TABLE_PUBLISHERS+ "_TEMP ;");
            db.execSQL("ALTER TABLE " + TABLE_TAGS + " RENAME TO " + TABLE_TAGS + "_TEMP ;");

            //Recreate tables
            onCreate(db);

            //Copy data from temporary tables to original tables
            db.execSQL("INSERT INTO " + TABLE_TAGS + " SELECT * FROM " + TABLE_TAGS + "_TEMP ;");
            db.execSQL("INSERT INTO " + TABLE_PUBLISHERS + " SELECT * FROM " + TABLE_PUBLISHERS + "_TEMP ;");
            db.execSQL("INSERT INTO " + TABLE_BOOKS + " SELECT * FROM " + TABLE_BOOKS + "_TEMP ;");
            db.execSQL("INSERT INTO " + TABLE_BOOK_ITEMS + " SELECT * FROM " + TABLE_BOOK_ITEMS + "_TEMP ;");
            db.execSQL("INSERT INTO " + TABLE_MUSIC + " SELECT * FROM " + TABLE_MUSIC + "_TEMP ;");
            db.execSQL("INSERT INTO " + TABLE_VIDEOS + " SELECT * FROM " + TABLE_VIDEOS + "_TEMP ;");
            db.execSQL("INSERT INTO " + TABLE_VIDEO_ITEMS + " SELECT * FROM " + TABLE_VIDEO_ITEMS + "_TEMP ;");
            db.execSQL("INSERT INTO " + TABLE_OTHERS + " SELECT * FROM " + TABLE_OTHERS + "_TEMP ;");
            db.execSQL("INSERT INTO " + TABLE_OTHER_ITEMS + " SELECT * FROM " + TABLE_OTHER_ITEMS + "_TEMP ;");

            //Drop temporary tables
            db.execSQL("DROP TABLE " + TABLE_OTHER_ITEMS + "_TEMP ;");
            db.execSQL("DROP TABLE " + TABLE_OTHERS + "_TEMP ;");
            db.execSQL("DROP TABLE " + TABLE_VIDEO_ITEMS + "_TEMP ;");
            db.execSQL("DROP TABLE " + TABLE_VIDEOS + "_TEMP ;");
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_MUSIC);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_BOOK_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_BOOKS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_TAGS);
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_PUBLISHERS);
        }
    }
}
