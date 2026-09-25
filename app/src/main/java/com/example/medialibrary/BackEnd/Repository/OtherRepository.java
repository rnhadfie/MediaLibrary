package com.example.medialibrary.backend.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.other.OtherItem;
import com.example.medialibrary.backend.models.other.OtherSaveObj;
import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.repository.Interface.Interface.IOtherRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;
import android.content.ContentValues;
import android.util.LruCache;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;

public class OtherRepository extends BaseRepository implements IOtherRepository {

    private static final LruCache<String, List<Other>> listCache = new LruCache<>(30);

    public OtherRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

    public static void clearCache() {
        listCache.evictAll();
    }

    @Override
    public List<Other> GetOtherCollections() {
        return GetOtherCollections("", new ArrayList<>());
    }

    @Override
    public List<Other> GetOtherCollections(String whereClause, List<String> selectionArgs) {
        List<Other> others = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if (whereClause == null) {
            whereClause = "";
        }

        String argsKey = (selectionArgs != null && !selectionArgs.isEmpty()) ? String.join(",", selectionArgs) : "";
        String cacheKey = "Other_" + whereClause + "_" + argsKey;

        List<Other> cachedList = listCache.get(cacheKey);
        if (cachedList != null) {
            return cachedList; // Cache hit!
        }

        try {
            Cursor cursor = db.query(
                    TABLE_OTHERS,
                    null,
                    whereClause,
                    (selectionArgs == null || selectionArgs.isEmpty()) ? null : selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    Other other = new Other();
                    mapMediaItem(cursor, other);
                    other.Items = GetOtherItems(other.Id);
                    other.MediaType = Enums.MediaType.Other;
                    others.add(other);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            return null;
        }
        listCache.put(cacheKey, others);
        return others;
    }

    @Override
    public Other GetOtherCollection(int id) {
        Other other = new Other();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();

        try {
            selectionArgs.add(String.valueOf(id));
            String whereClause = COLUMN_ID + " = ?";
            Cursor cursor = db.query(
                    TABLE_OTHERS,
                    null,
                    whereClause,
                    selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    mapMediaItem(cursor, other);
                    other.MediaType = Enums.MediaType.Other;
                    other.Items = GetOtherItems(id);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            return null;
        }
        return other;
    }

    @Override
    public boolean AddOtherCollection(OtherSaveObj otherObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        var other = otherObj.Other;
        try {
            ContentValues values = mapOtherContentValues(other.Tag, other);

            long id = db.insert(TABLE_OTHERS, null, values);
            if (other.Items != null) {
                for (OtherItem item : other.Items) {
                    ContentValues itemValues = mapOtherItemContentValues(id, item);
                    db.insert(TABLE_OTHER_ITEMS, null, itemValues);
                }
            }
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
    public boolean UpdateOtherCollection(OtherSaveObj otherObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        var other = otherObj.Other;
        try {
            long tagId = otherObj.Other.Tag;
            if (otherObj.NewTag != null && !otherObj.NewTag.isEmpty()) {
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_NAME, otherObj.NewTag);
                tagId = db.insert(TABLE_TAGS, null, tagValues);
            }

            ContentValues values = mapOtherContentValues(tagId, other);

            var result = db.update(TABLE_OTHERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(other.Id)});

            db.delete(TABLE_OTHER_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(other.Id)});

            if (other.Items != null) {
                for (OtherItem item : other.Items) {
                    ContentValues itemValues = mapOtherItemContentValues(other.Id, item);
                    db.insert(TABLE_OTHER_ITEMS, null, itemValues);
                }
            }

            db.setTransactionSuccessful();
            return result > -1;
        } catch (Exception e) {
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    @Override
    public boolean DeleteOtherCollection(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(TABLE_OTHER_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(id)});
            db.delete(TABLE_OTHERS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    private List<OtherItem> GetOtherItems(int bookId) {
        List<OtherItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(bookId));
        String whereClause = COLUMN_SERIES + " = ?";
        Cursor cursor = db.query(
                TABLE_OTHER_ITEMS,
                null,
                whereClause,
                selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            do {
                OtherItem item = new OtherItem();
                mapOtherItem(cursor, item);
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    private void mapOtherItem(Cursor cursor, OtherItem item) {
        item.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.Title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOLUME_TITLE));
        item.Series = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERIES));
        item.Owned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNED)) == 1;
        item.ItemCover = decompressBitmap(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ITEM_COVER)));
    }

    private ContentValues mapOtherItemContentValues(long seriesId, OtherItem item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_SERIES, seriesId);
        itemValues.put(COLUMN_VOLUME_TITLE, item.Title);
        itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
        itemValues.put(COLUMN_ITEM_COVER, compressBitmap(item.ItemCover));
        return itemValues;
    }

    private ContentValues mapOtherContentValues(long tagId, Other other) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, other.Title);
        values.put(COLUMN_COLLECTING, (other.Collecting != null && other.Collecting) ? 1 : 0);
        values.put(COLUMN_HAS_ENDED, (other.HasSeriesEnded != null && other.HasSeriesEnded) ? 1 : 0);
        values.put(COLUMN_COMPLETED_COLLECTING, (other.HasCollectedAllItems != null && other.HasCollectedAllItems) ? 1 : 0);
        values.put(COLUMN_TAG, tagId);
        values.put(COLUMN_COVER, compressBitmap(other.Cover));
        values.put(COLUMN_GENRE, serializeGenre(other.Genre));
        return values;
    }
}
