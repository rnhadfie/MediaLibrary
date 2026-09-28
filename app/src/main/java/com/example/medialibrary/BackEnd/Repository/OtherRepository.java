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
import com.example.medialibrary.backend.utils.DatabaseMappings;

import android.content.ContentValues;
import android.util.LruCache;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                    DatabaseMappings.MapMediaItem(cursor, other);
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
    public Other GetOtherCollection(String id) {
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
                    DatabaseMappings.MapMediaItem(cursor, other);
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
            String id = UUID.randomUUID().toString();
            other.Id = id;
            ContentValues values = DatabaseMappings.MapOtherContentValues(other.Tag, other);



            var result = db.insert(TABLE_OTHERS, null, values);
            if (other.Items != null) {
                for (OtherItem item : other.Items) {
                    ContentValues itemValues = DatabaseMappings.MapOtherItemContentValues(id, item);
                    db.insert(TABLE_OTHER_ITEMS, null, itemValues);
                }
            }
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
    public boolean UpdateOtherCollection(OtherSaveObj otherObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        var other = otherObj.Other;
        try {

            String tagId = AddNewTag(otherObj.Other.Tag, otherObj.NewTag, db);

            ContentValues values = DatabaseMappings.MapOtherContentValues(tagId, other);

            var result = db.update(TABLE_OTHERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(other.Id)});

            db.delete(TABLE_OTHER_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(other.Id)});

            if (other.Items != null) {
                for (OtherItem item : other.Items) {
                    ContentValues itemValues = DatabaseMappings.MapOtherItemContentValues(other.Id, item);
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
    public boolean DeleteOtherCollection(String id) {
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

    private List<OtherItem> GetOtherItems(String id) {
        List<OtherItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(id));
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
                DatabaseMappings.MapOtherItem(cursor, item);
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }
}
