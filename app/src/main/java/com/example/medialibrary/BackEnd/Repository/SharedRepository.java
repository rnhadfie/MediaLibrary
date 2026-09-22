package com.example.medialibrary.backend.repository;

import static com.example.medialibrary.backend.repository.database.DatabaseKeyNames.COLUMN_ID;
import static com.example.medialibrary.backend.repository.database.DatabaseKeyNames.COLUMN_NAME;
import static com.example.medialibrary.backend.repository.database.DatabaseKeyNames.TABLE_PUBLISHERS;
import static com.example.medialibrary.backend.repository.database.DatabaseKeyNames.TABLE_TAGS;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.medialibrary.backend.models.book.Publisher;
import com.example.medialibrary.backend.models.shared.Tag;
import com.example.medialibrary.backend.repository.Interface.Interface.IBookRepository;
import com.example.medialibrary.backend.repository.Interface.Interface.ISharedRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SharedRepository extends BaseRepository implements ISharedRepository {
    public SharedRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

        public List<Tag> GetTags() {
            List<Tag> tags = new ArrayList<>();

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<String> selectionArgs = new ArrayList<>();

            Cursor cursor = db.query(
                    TABLE_TAGS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    tags.add(new Tag(id, name));
                } while (cursor.moveToNext());
                cursor.close();
            }
            return tags;
        }

        @Override
        public Tag GetTag(int id) {

            Tag tag = new Tag();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<String> selectionArgs = new ArrayList<>();
            selectionArgs.add(String.valueOf(id));

            String whereClause = COLUMN_ID + " = ?";

            Cursor bookCursor = db.query(
                    TABLE_TAGS,
                    null,
                    whereClause,
                    selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (bookCursor != null && bookCursor.moveToFirst()) {
                do {
                    tag.Id = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_ID));
                    tag.Name = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_NAME));

                } while (bookCursor.moveToNext());
                bookCursor.close();
            }

            return tag;

        }

        @Override
        public boolean AddTag(Tag tag) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                if (tag.Name != null && !tag.Name.isEmpty()) {
                    ContentValues pubValues = new ContentValues();
                    pubValues.put(COLUMN_NAME, tag.Name);
                    long id = db.insert(TABLE_TAGS, null, pubValues);
                    if (id == -1) return false;
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
        public boolean UpdateTag(Tag tag) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, tag.Name);

                int updatedRows = db.update(TABLE_TAGS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(tag.Id)});
                db.setTransactionSuccessful();
                return updatedRows > 0;
            } catch (Exception e) {
                Logger.getLogger(SharedRepository.class.getName()).severe(e.getMessage());
                return false;
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public boolean DeleteTag(int id) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {

                // Delete the book itself
                int deletedRows = db.delete(TABLE_TAGS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

                db.setTransactionSuccessful();
                return deletedRows > 0;
            } catch (Exception e) {
                Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
                return false;
            } finally {
                db.endTransaction();
            }
        }


    }

