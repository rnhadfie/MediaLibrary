package com.example.medialibrary.backend.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.LruCache;
import com.example.medialibrary.backend.models.video.Enums;
import com.example.medialibrary.backend.models.shared.Enums.MediaType;
import com.example.medialibrary.backend.models.video.*;
import com.example.medialibrary.backend.repository.Interface.Interface.IVideoRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;
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
                    mapVideo(cursor, video);
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
    public Video GetVideo(int id) {
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
                    mapVideo(videoCursor, video);
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
            long tagId = videoObj.video.Tag;
            if (videoObj.NewTag != null && !videoObj.NewTag.isEmpty()) {
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_NAME, videoObj.NewTag);
                tagId = db.insert(TABLE_TAGS, null, tagValues);
            }

            ContentValues videoValues = mapVideoContentValues(tagId, videoObj.video);

            long videoId = db.insert(TABLE_VIDEOS, null, videoValues);

            if (videoObj.video.Items != null) {
                for (VideoItem item : videoObj.video.Items) {
                    ContentValues itemValues = mapVideoItemContentValues(videoId, item);
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
            long tagId = videoObj.video.Tag;
            if (videoObj.NewTag != null && !videoObj.NewTag.isEmpty()) {
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_NAME, videoObj.NewTag);
                tagId = db.insert(TABLE_TAGS, null, tagValues);
            }

            ContentValues videoValues = mapVideoContentValues(tagId, videoObj.video);

            db.update(TABLE_VIDEOS, videoValues, COLUMN_ID + " = ?", new String[]{String.valueOf(videoObj.video.Id)});

            db.delete(TABLE_VIDEO_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(videoObj.video.Id)});

            if (videoObj.video.Items != null) {
                for (VideoItem item : videoObj.video.Items) {
                    ContentValues itemValues = mapVideoItemContentValues(videoObj.video.Id, item);
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
    public boolean DeleteVideo(int id) {
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

    private List<VideoItem> GetVideoItems(Integer id) {
        List<VideoItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_VIDEO_ITEMS,
                null,
                COLUMN_SERIES + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            do {
                VideoItem item = new VideoItem();
                mapVideoItem(cursor, item);
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    private void mapVideoItem(Cursor cursor, VideoItem item) {
        item.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.DiscNumber = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISC_NUMBER));
        item.DiscTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_TITLE));
        item.Series = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERIES));
        item.Watched = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WATCHED)) == 1;
        item.Format = Enums.VideoFormat.values()[cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FORMAT))];
        item.Owned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNED)) == 1;
        item.ItemCover = decompressBitmap(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ITEM_COVER)));
    }

    private void mapVideo(Cursor videoCursor, Video video) {
        mapMediaItem(videoCursor, video);
        video.MediaType = MediaType.Video;
        video.Items = GetVideoItems(video.Id);
        video.Tag = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_TAG));
        video.VideoTag = Enums.VideoTag.values()[videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_VIDEO_TAG))];

        int typeValue = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_TYPE));
        if (typeValue >= 0 && typeValue < Enums.VideoType.values().length) {
            video.Type = Enums.VideoType.values()[typeValue];
        }
    }

    private ContentValues mapVideoContentValues(long tagId, Video video) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(COLUMN_TITLE, video.Title);
        videoValues.put(COLUMN_COLLECTING, (video.Collecting != null && video.Collecting) ? 1 : 0);
        videoValues.put(COLUMN_HAS_ENDED, (video.HasSeriesEnded != null && video.HasSeriesEnded) ? 1 : 0);
        videoValues.put(COLUMN_COMPLETED_COLLECTING, (video.HasCollectedAllItems != null && video.HasCollectedAllItems) ? 1 : 0);
        videoValues.put(COLUMN_TAG, tagId);
        videoValues.put(COLUMN_COVER, compressBitmap(video.Cover));
        videoValues.put(COLUMN_GENRE, serializeGenre(video.Genre));
        videoValues.put(COLUMN_TYPE, video.Type != null ? video.Type.ordinal() : 0);
        videoValues.put(COLUMN_VIDEO_TAG, video.VideoTag != null ? video.VideoTag.ordinal() : 0);
        return videoValues;
    }

    private ContentValues mapVideoItemContentValues(long videoId, VideoItem item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_SERIES, videoId);
        itemValues.put(COLUMN_DISC_NUMBER, item.DiscNumber);
        itemValues.put(COLUMN_DISC_TITLE, item.DiscTitle);
        itemValues.put(COLUMN_WATCHED, item.Watched ? 1 : 0);
        itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
        itemValues.put(COLUMN_FORMAT, item.Format != null ? item.Format.ordinal() : 0);
        itemValues.put(COLUMN_ITEM_COVER, compressBitmap(item.ItemCover));
        return itemValues;
    }
}
