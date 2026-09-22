package com.example.medialibrary.backend.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.music.MusicObj;
import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.repository.Interface.Interface.IMusicRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;
import android.content.ContentValues;

import static com.example.medialibrary.backend.repository.database.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository extends BaseRepository implements IMusicRepository {
    public MusicRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public  <T extends Filter> List<Music> GetMusic(T filter) {
        List<Music> musicList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = buildWhereClause(filter, selectionArgs);

        Cursor cursor = db.query(
                TABLE_MUSIC,
                null,
                whereClause,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Music music = new Music();
                mapMediaItem(cursor, music);
                music.MediaType = Enums.MediaType.Music;
                music.Artist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST));
                music.Year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR));
                music.Genre = deserializeGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                music.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                music.HasSeriesEnded = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HAS_ENDED)) == 1;
                music.HasCollectedAllItems = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COLLECTING)) == 1;
                music.Collecting = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
                music.Cover = decompressBitmap(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_COVER)));
                musicList.add(music);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return musicList;
    }

    @Override
    public Music GetMusic(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            List<String> selectionArgs = new ArrayList<>();
            selectionArgs.add(String.valueOf(id));
            String whereClause = COLUMN_ID + " = ?";
            Cursor cursor = db.query(
                    TABLE_MUSIC,
                    null,
                    whereClause,
                    selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );
            Music music = new Music();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    mapMediaItem(cursor, music);
                    music.MediaType = Enums.MediaType.Music;
                    music.Artist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST));
                    music.Year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR));
                    music.Genre = deserializeGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                    music.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    music.HasSeriesEnded = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HAS_ENDED)) == 1;
                    music.HasCollectedAllItems = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COLLECTING)) == 1;
                    music.Collecting = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
                    music.Cover = decompressBitmap(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_COVER)));
                } while (cursor.moveToNext());
                cursor.close();
            }
            db.setTransactionSuccessful();
            return music;
        }
        catch (Exception ex)
        {
            return null;
        }
        finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean AddMusic(MusicObj musicObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {

            long tagId = musicObj.Music.Tag;
            if (musicObj.NewTag != null && !musicObj.NewTag.isEmpty()) {
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_NAME, musicObj.NewTag);
                tagId = db.insert(TABLE_TAGS, null, tagValues);
            }
            var music = musicObj.Music;

            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, music.Title);
            values.put(COLUMN_COLLECTING, (music.Collecting != null && music.Collecting) ? 1 : 0);
            values.put(COLUMN_HAS_ENDED, (music.HasSeriesEnded != null && music.HasSeriesEnded) ? 1 : 0);
            values.put(COLUMN_COMPLETED_COLLECTING, (music.HasCollectedAllItems != null && music.HasCollectedAllItems) ? 1 : 0);
            values.put(COLUMN_TAG, music.Tag);
            values.put(COLUMN_COVER, music.Cover);
            values.put(COLUMN_GENRE, serializeGenre(music.Genre));
            values.put(COLUMN_ARTIST, music.Artist);
            values.put(COLUMN_YEAR, music.Year);
            values.put(COLUMN_COVER, compressBitmap(music.Cover));

            long id = db.insert(TABLE_MUSIC, null, values);
            db.setTransactionSuccessful();
            return id != -1;
        } catch (Exception e) {
            return false;

        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean UpdateMusic(MusicObj musicObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        var music = musicObj.Music;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, music.Title);
            values.put(COLUMN_COLLECTING, (music.Collecting != null && music.Collecting) ? 1 : 0);
            values.put(COLUMN_HAS_ENDED, (music.HasSeriesEnded != null && music.HasSeriesEnded) ? 1 : 0);
            values.put(COLUMN_COMPLETED_COLLECTING, (music.HasCollectedAllItems != null && music.HasCollectedAllItems) ? 1 : 0);
            values.put(COLUMN_TAG, music.Tag);
            values.put(COLUMN_COVER, music.Cover);
            values.put(COLUMN_GENRE, serializeGenre(music.Genre));
            values.put(COLUMN_ARTIST, music.Artist);
            values.put(COLUMN_YEAR, music.Year);
            values.put(COLUMN_COVER, compressBitmap(music.Cover));

            long reuslt = db.update(TABLE_MUSIC, values, COLUMN_ID + " = ?", new String[]{String.valueOf(music.Id)});
            db.setTransactionSuccessful();
            return reuslt != -1;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean DeleteMusic(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int rowsDeleted = db.delete(TABLE_MUSIC, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return rowsDeleted > 0;
        }
        catch (Exception e) {
            return false;
        }finally {
            db.endTransaction();

        }
    }
}
