package com.example.medialibrary.backend.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.LruCache;

import com.example.medialibrary.backend.models.book.BookItem;
import com.example.medialibrary.backend.models.video.Enums;
import com.example.medialibrary.backend.models.shared.Enums.MediaType;
import com.example.medialibrary.backend.models.video.*;
import com.example.medialibrary.backend.repository.Interface.Interface.IVideoRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;
import com.example.medialibrary.backend.utils.DatabaseMappings;
import com.example.medialibrary.backend.utils.NaturalComparator;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class VideoRepository extends BaseRepository implements IVideoRepository {

    private static final LruCache<String, List<Video>> listCache = new LruCache<>(30);

    public VideoRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

    public static void clearCache() {
        listCache.evictAll();
    }

    @Override
    public List<Video> GetVideos() {
        return GetVideos("", new ArrayList<>());
    }

    @Override
    public List<Video> GetVideos(String whereClause, List<String> selectionArgs) {
        List<Video> videos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if (whereClause == null) {
            whereClause = "";
        }

        String argsKey = (selectionArgs != null && !selectionArgs.isEmpty()) ? String.join(",", selectionArgs) : "";
        String cacheKey = "Video_" + whereClause + "_" + argsKey;

        List<Video> cachedList = listCache.get(cacheKey);
        if (cachedList != null) {
            return cachedList; // Cache hit!
        }

        try {
            Cursor cursor = db.query(
                    TABLE_VIDEOS,
                    null,
                    whereClause,
                    (selectionArgs == null || selectionArgs.isEmpty()) ? null : selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    Video video = new Video();
                    var id = DatabaseMappings.MapVideo(cursor, video);
                    video.Items = GetVideoItems(id);
                    videos.add(video);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception ex) {
            return null;
        }
        listCache.put(cacheKey, videos);

        return videos;
    }

    @Override
    public Video GetVideo(String id) {
        Video video = new Video();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            List<String> selectionArgs = new ArrayList<>();
            selectionArgs.add(String.valueOf(id));

            String whereClause = COLUMN_ID + " = ?";

            Cursor videoCursor = db.query(
                    TABLE_VIDEOS,
                    null,
                    whereClause,
                    selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (videoCursor.moveToFirst()) {
                do {
                    DatabaseMappings.MapVideo(videoCursor, video);
                    video.Items = GetVideoItems(video.Id);
                } while (videoCursor.moveToNext());
                videoCursor.close();
            }
        } catch (Exception ex) {
            return null;
        }

        return video;
    }

    @Override
    public boolean AddVideo(VideoSaveObject videoObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String tagId = AddNewTag(videoObj.video.Tag, videoObj.NewTag, db);

            String videoId = UUID.randomUUID().toString();
            videoObj.video.Id = videoId;

            ContentValues videoValues = DatabaseMappings.MapVideoContentValues(tagId, videoObj.video);

            db.insert(TABLE_VIDEOS, null, videoValues);

            if (videoObj.video.Items != null) {
                for (VideoItem item : videoObj.video.Items) {
                    ContentValues itemValues = DatabaseMappings.MapVideoItemContentValues(videoId, item);
                    db.insert(TABLE_VIDEO_ITEMS, null, itemValues);
                }
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    @Override
    public boolean UpdateVideo(VideoSaveObject videoObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String tagId = AddNewTag(videoObj.video.Tag, videoObj.NewTag, db);

            ContentValues videoValues = DatabaseMappings.MapVideoContentValues(tagId, videoObj.video);

            db.update(TABLE_VIDEOS, videoValues, COLUMN_ID + " = ?", new String[]{videoObj.video.Id});

            db.delete(TABLE_VIDEO_ITEMS, COLUMN_SERIES + " = ?", new String[]{videoObj.video.Id});

            if (videoObj.video.Items != null) {
                for (VideoItem item : videoObj.video.Items) {
                    ContentValues itemValues = DatabaseMappings.MapVideoItemContentValues(videoObj.video.Id, item);
                    db.insert(TABLE_VIDEO_ITEMS, null, itemValues);
                }
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.getLogger(VideoRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    @Override
    public boolean DeleteVideo(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_VIDEO_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(id)});
            int deletedRows = db.delete(TABLE_VIDEOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return deletedRows > 0;
        } catch (Exception e) {
            Logger.getLogger(VideoRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    private List<VideoItem> GetVideoItems(String id) {
        List<VideoItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_VIDEO_ITEMS,
                null,
                COLUMN_SERIES + " = ?",
                new String[]{id},
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            do {
                VideoItem item = new VideoItem();
                DatabaseMappings.MapVideoItem(cursor, item);
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        items.sort(new NaturalComparator<>(VideoItem::GetSeason));
        return items;
    }


}
