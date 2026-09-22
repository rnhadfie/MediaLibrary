package com.example.medialibrary.backend.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.other.OtherItem;
import com.example.medialibrary.backend.models.other.OtherSaveObj;
import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.repository.Interface.Interface.IOtherRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;
import android.content.ContentValues;

import static com.example.medialibrary.backend.repository.database.DatabaseKeyNames.*;

import java.util.ArrayList;
import java.util.List;

public class OtherRepository extends BaseRepository implements IOtherRepository {
    public OtherRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public List<Other> GetOtherCollections(Filter filter) {
        List<Other> others = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = buildWhereClause(filter, selectionArgs);

        Cursor cursor = db.query(
                TABLE_OTHERS,
                null,
                whereClause,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Other other = new Other();
                mapMediaItem(cursor, other);
                other.MediaType = Enums.MediaType.Other;
                others.add(other);
            } while (cursor.moveToNext());
            cursor.close();
        }

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
                    selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    mapMediaItem(cursor, other);
                    other.MediaType = Enums.MediaType.Other;
                    other.Items = GetOtherItems(id);
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        catch (Exception e) {
            return null;
        }
        finally {
            db.endTransaction();
            return other;
        }
    }

    @Override
    public boolean AddOtherCollection(OtherSaveObj otherObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        var other = otherObj.Other;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, other.Title);
            values.put(COLUMN_COLLECTING, (other.Collecting != null && other.Collecting) ? 1 : 0);
            values.put(COLUMN_HAS_ENDED, (other.HasSeriesEnded != null && other.HasSeriesEnded) ? 1 : 0);
            values.put(COLUMN_COMPLETED_COLLECTING, (other.HasCollectedAllItems != null && other.HasCollectedAllItems) ? 1 : 0);
            values.put(COLUMN_TAG, other.Tag);
            values.put(COLUMN_COVER, compressBitmap(other.Cover));
            values.put(COLUMN_GENRE, serializeGenre(other.Genre));


            long id = db.insert(TABLE_OTHERS, null, values);
            db.setTransactionSuccessful();
            return id != -1;
        } catch (Exception e) {
            return false;
        } finally {
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

            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, other.Title);
            values.put(COLUMN_COLLECTING, (other.Collecting != null && other.Collecting) ? 1 : 0);
            values.put(COLUMN_HAS_ENDED, (other.HasSeriesEnded != null && other.HasSeriesEnded) ? 1 : 0);
            values.put(COLUMN_COMPLETED_COLLECTING, (other.HasCollectedAllItems != null && other.HasCollectedAllItems) ? 1 : 0);
            values.put(COLUMN_TAG, other.Tag);
            values.put(COLUMN_COVER, compressBitmap(other.Cover));
            values.put(COLUMN_GENRE, serializeGenre(other.Genre));

            var result = db.update(TABLE_OTHERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(other.Id)});
            db.setTransactionSuccessful();
            return result > -1;

        }
        catch (Exception e) {
            return false;
        }
        finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean DeleteOtherCollection(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(TABLE_OTHERS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return true;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            db.endTransaction();
        }
    }

    private List<OtherItem> GetOtherItems(int bookId)
    {
        List<OtherItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(bookId));
        String whereClause = COLUMN_SERIES + " = ?";
        Cursor cursor = db.query(
                TABLE_BOOK_ITEMS,
                null,
                whereClause,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
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
}
