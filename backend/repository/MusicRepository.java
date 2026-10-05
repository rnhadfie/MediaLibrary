package repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import models.music.*;
import repository.Interface.Interface.IMusicRepository;
import repository.database.BaseRepository;
import repository.database.MediaLibraryDbHelper;
import utils.DatabaseMappings;

import android.content.ContentValues;
import android.util.LruCache;

import static utils.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MusicRepository extends BaseRepository implements IMusicRepository {

    private static final LruCache<String, List<Music>> listCache = new LruCache<>(30);

    public MusicRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

    public static void clearCache() {
        listCache.evictAll();
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

        String argsKey = (selectionArgs != null && !selectionArgs.isEmpty()) ? String.join(",", selectionArgs) : "";
        String cacheKey = "Music_" + whereClause + "_" + argsKey;

        List<Music> cachedList = listCache.get(cacheKey);
        if (cachedList != null) {
            return cachedList; // Cache hit!
        }

        try {
            Cursor cursor = db.query(
                    TABLE_MUSIC,
                    null,
                    whereClause,
                    (selectionArgs == null || selectionArgs.isEmpty()) ? null : selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    Music music = new Music();
                    DatabaseMappings.MapMusicItems(cursor, music);
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
    public Music GetMusic(String id) {
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
                    DatabaseMappings.MapMusicItems(cursor, music);
                } while (cursor.moveToNext());
                cursor.close();
            }
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
            String tagId = AddNewTag(musicObj.Music.Tag, musicObj.NewTag, db);

            var music = musicObj.Music;

            music.Id = UUID.randomUUID().toString();
            ContentValues values = DatabaseMappings.MapMusicContentValues(tagId, music);

            long id = db.insert(TABLE_MUSIC, null, values);
            db.setTransactionSuccessful();
            return id != -1;
        } catch (Exception e) {
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    @Override
    public boolean UpdateMusic(MusicObj musicObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        var music = musicObj.Music;
        try {
            ContentValues values = DatabaseMappings.MapMusicContentValues(music.Tag, music);

            long result = db.update(TABLE_MUSIC, values, COLUMN_ID + " = ?", new String[]{music.Id});
            db.setTransactionSuccessful();
            return result != -1;
        } catch (Exception e) {
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    @Override
    public boolean DeleteMusic(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int rowsDeleted = db.delete(TABLE_MUSIC, COLUMN_ID + "=?", new String[]{id});
            db.setTransactionSuccessful();
            return rowsDeleted > 0;
        } catch (Exception e) {
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }




}
