package com.example.medialibrary.backend.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.music.MusicObj;
import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.models.music.Enums.*;
import com.example.medialibrary.backend.repository.Interface.Interface.IMusicRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;
import android.content.ContentValues;
import android.util.LruCache;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository extends BaseRepository implements IMusicRepository {

    private final LruCache<String, List<Music>> listCache;

    public MusicRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
        this.listCache = new LruCache<>(30);
    }

    @Override
    public List<Music> GetMusic() {
        return GetMusic("", new ArrayList<>());
    }


    @Override
    public List<Music> GetMusic(String whereClause, List<String> selectionArgs) {
        List<Music> musicList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (whereClause == null) {
            whereClause = "";
        }

        String cacheKey = "Music_" + whereClause;
        List<Music> cachedList = listCache.get(cacheKey);
        if (cachedList != null) {
            return cachedList; // Cache hit!
        }

        try {

            Cursor cursor = db.query(
                    TABLE_MUSIC,
                    null,
                    whereClause,
                    selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    Music music = new Music();
                    MapMusicItems(cursor, music);
                    musicList.add(music);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception ex) {
            return null;
        }
        listCache.put(cacheKey, musicList);
        return musicList;
    }

    @Override
    public Music GetMusic(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            List<String> selectionArgs = new ArrayList<>();
            selectionArgs.add(String.valueOf(id));
            String whereClause = COLUMN_ID + " = ?";
            Cursor cursor = db.query(
                    TABLE_MUSIC,
                    null,
                    whereClause,
                    selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );
            Music music = new Music();

            if (cursor.moveToFirst()) {
                do {
                    MapMusicItems(cursor, music);
                } while (cursor.moveToNext());
                cursor.close();
            }
            db.setTransactionSuccessful();
            return music;
        } catch (Exception ex) {
            return null;
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

            ContentValues values = MapMusicContentValues(tagId, music);

            long id = db.insert(TABLE_MUSIC, null, values);
            db.setTransactionSuccessful();
            return id != -1;
        } catch (Exception e) {
            return false;

        } finally {
            listCache.evictAll();
            db.endTransaction();
        }
    }

    @Override
    public boolean UpdateMusic(MusicObj musicObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        var music = musicObj.Music;
        try {
            ContentValues values = MapMusicContentValues(music.Tag, music);

            long result = db.update(TABLE_MUSIC, values, COLUMN_ID + " = ?", new String[]{String.valueOf(music.Id)});
            db.setTransactionSuccessful();
            return result != -1;
        } finally {
            listCache.evictAll();
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
        } catch (Exception e) {
            return false;
        } finally {
            listCache.evictAll();
            db.endTransaction();

        }
    }

    protected String serializeMusicGenre(MusicGenre musicGenre) {
        if (musicGenre == null || musicGenre == MusicGenre.NoneSelected) {
            return "";
        }

        return String.valueOf(musicGenre.ordinal());
    }

    protected MusicGenre deserializeMusicGenre(String genre) {
        if (genre == null || genre.isEmpty()) {
            return MusicGenre.NoneSelected;
        }
        int musicGenre = Integer.parseInt(genre);

        return MusicGenre.values()[musicGenre];
    }

    private void MapMusicItems(Cursor cursor, Music music) {

        mapMediaItem(cursor, music);
        music.MediaType = Enums.MediaType.Music;
        music.Artist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST));
        music.Year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR));
        music.MusicGenre = deserializeMusicGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE)));
    }

    private ContentValues MapMusicContentValues(long tagId, Music music)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, music.Title);
        values.put(COLUMN_COLLECTING, (music.Collecting != null && music.Collecting) ? 1 : 0);
        values.put(COLUMN_HAS_ENDED, (music.HasSeriesEnded != null && music.HasSeriesEnded) ? 1 : 0);
        values.put(COLUMN_COMPLETED_COLLECTING, (music.HasCollectedAllItems != null && music.HasCollectedAllItems) ? 1 : 0);
        values.put(COLUMN_TAG, tagId);
        values.put(COLUMN_COVER, music.Cover);
        values.put(COLUMN_GENRE, serializeMusicGenre(music.MusicGenre));
        values.put(COLUMN_ARTIST, music.Artist);
        values.put(COLUMN_YEAR, music.Year);
        values.put(COLUMN_COVER, compressBitmap(music.Cover));
        return values;
    }

}
