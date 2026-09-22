package com.example.medialibrary.backend.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.medialibrary.backend.models.video.Enums;
import com.example.medialibrary.backend.models.shared.Enums.MediaType;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.models.video.*;
import com.example.medialibrary.backend.repository.Interface.Interface.IVideoRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import static com.example.medialibrary.backend.repository.database.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class VideoRepository extends BaseRepository implements IVideoRepository {
    public VideoRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public <T extends Filter> List<Video> GetVideos(T filter) {
        List<Video> videos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();

        String whereClause = buildWhereClause(filter, selectionArgs);

        Cursor cursor = db.query(
                TABLE_VIDEOS,
                null,
                whereClause,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Video video = new Video();
                mapMediaItem(cursor, video);
                video.MediaType = MediaType.Video;
                video.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                video.Items = GetVideoItems(video.Id);
                video.HasSeriesEnded = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HAS_ENDED)) == 1;
                video.HasCollectedAllItems = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COLLECTING)) == 1;
                video.Collecting = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
                video.Cover = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_COVER));
                video.Genre = deserializeGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                video.Tag = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG));
                video.VideoTag = Enums.VideoTag.values()[cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_TAG))];


                int typeValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                if (typeValue >= 0 && typeValue < Enums.VideoType.values().length) {
                    video.Type = Enums.VideoType.values()[typeValue];
                }

                videos.add(video);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return videos;
    }

    @Override
    public Video GetVideo(int id) {

        Video video = new Video();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(id));

        String whereClause = COLUMN_ID + " = ?";

        //region Video

        Cursor videoCursor = db.query(
                TABLE_VIDEOS,
                null,
                whereClause,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );

        if (videoCursor != null && videoCursor.moveToFirst()) {
            do {
                mapMediaItem(videoCursor, video);
                video.MediaType = MediaType.Video;
                video.Id = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_ID));
                video.Items = GetVideoItems(video.Id);
                video.HasSeriesEnded = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_HAS_ENDED)) == 1;
                video.HasCollectedAllItems = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COLLECTING)) == 1;
                video.Collecting = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
                video.Cover = decompressBitmap(videoCursor.getBlob(videoCursor.getColumnIndexOrThrow(COLUMN_COVER)));
                video.Genre = deserializeGenre(videoCursor.getString(videoCursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                video.Tag = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_TAG));
                video.VideoTag = Enums.VideoTag.values()[videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_VIDEO_TAG))];


                int typeValue = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_TYPE));
                if (typeValue >= 0 && typeValue < Enums.VideoType.values().length) {
                    video.Type = Enums.VideoType.values()[typeValue];
                }

            } while (videoCursor.moveToNext());
            videoCursor.close();
        }

        //endregion


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

            ContentValues videoValues = new ContentValues();
            videoValues.put(COLUMN_TITLE, videoObj.video.Title);
            videoValues.put(COLUMN_COLLECTING, (videoObj.video.Collecting != null && videoObj.video.Collecting) ? 1 : 0);
            videoValues.put(COLUMN_HAS_ENDED, (videoObj.video.HasSeriesEnded != null && videoObj.video.HasSeriesEnded) ? 1 : 0);
            videoValues.put(COLUMN_COMPLETED_COLLECTING, (videoObj.video.HasCollectedAllItems != null && videoObj.video.HasCollectedAllItems) ? 1 : 0);
            videoValues.put(COLUMN_TAG, tagId);
            videoValues.put(COLUMN_COVER, compressBitmap(videoObj.video.Cover));
            videoValues.put(COLUMN_GENRE, serializeGenre(videoObj.video.Genre));
            videoValues.put(COLUMN_TYPE, videoObj.video.Type != null ? videoObj.video.Type.ordinal() : 0);
            videoValues.put(COLUMN_VIDEO_TAG, videoObj.video.VideoTag != null ? videoObj.video.VideoTag.ordinal() : 0);

            long videoId = db.insert(TABLE_VIDEOS, null, videoValues);

            if (videoObj.video.Items != null) {
                for (VideoItem item : videoObj.video.Items) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(COLUMN_SERIES, videoObj.video.Id);
                    itemValues.put(COLUMN_DISC_NUMBER, item.DiscNumber);
                    itemValues.put(COLUMN_DISC_TITLE, item.DiscTitle);
                    itemValues.put(COLUMN_WATCHED, item.Watched ? 1 : 0);
                    itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
                    itemValues.put(COLUMN_FORMAT, item.Format != null ? item.Format.ordinal() : 0);
                    itemValues.put(COLUMN_ITEM_COVER, compressBitmap(item.ItemCover));
                    db.insert(TABLE_VIDEO_ITEMS, null, itemValues);
                }
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
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

            ContentValues videoValues = new ContentValues();
            videoValues.put(COLUMN_TITLE, videoObj.video.Title);
            videoValues.put(COLUMN_COLLECTING, (videoObj.video.Collecting != null && videoObj.video.Collecting) ? 1 : 0);
            videoValues.put(COLUMN_HAS_ENDED, (videoObj.video.HasSeriesEnded != null && videoObj.video.HasSeriesEnded) ? 1 : 0);
            videoValues.put(COLUMN_COMPLETED_COLLECTING, (videoObj.video.HasCollectedAllItems != null && videoObj.video.HasCollectedAllItems) ? 1 : 0);
            videoValues.put(COLUMN_TAG, tagId);
            videoValues.put(COLUMN_COVER, compressBitmap(videoObj.video.Cover));
            videoValues.put(COLUMN_GENRE, serializeGenre(videoObj.video.Genre));
            videoValues.put(COLUMN_TYPE, videoObj.video.Type != null ? videoObj.video.Type.ordinal() : 0);
            videoValues.put(COLUMN_VIDEO_TAG, videoObj.video.VideoTag != null ? videoObj.video.VideoTag.ordinal() : 0);

            db.update(TABLE_VIDEOS, videoValues, COLUMN_ID + " = ?", new String[]{String.valueOf(videoObj.video.Id)});

            // Delete old items and insert new ones to keep it simple and consistent with AddBook's structure
            db.delete(TABLE_VIDEO_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(videoObj.video.Id)});

            if (videoObj.video.Items != null) {
                for (VideoItem item : videoObj.video.Items) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(COLUMN_SERIES, videoObj.video.Id);
                    itemValues.put(COLUMN_DISC_NUMBER, item.DiscNumber);
                    itemValues.put(COLUMN_DISC_TITLE, item.DiscTitle);
                    itemValues.put(COLUMN_WATCHED, item.Watched ? 1 : 0);
                    itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
                    itemValues.put(COLUMN_FORMAT, item.Format != null ? item.Format.ordinal() : 0);
                    itemValues.put(COLUMN_ITEM_COVER, compressBitmap(item.ItemCover));
                    db.insert(TABLE_VIDEO_ITEMS, null, itemValues);
                }
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.getLogger(VideoRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean DeleteVideo(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete all associated book items first
            db.delete(TABLE_VIDEO_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(id)});

            // Delete the book itself
            int deletedRows = db.delete(TABLE_VIDEOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

            db.setTransactionSuccessful();
            return deletedRows > 0;
        } catch (Exception e) {
            Logger.getLogger(VideoRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }


    private List<VideoItem> GetVideoItems(Integer id)
    {
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
        if (cursor != null && cursor.moveToFirst()) {
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
}
