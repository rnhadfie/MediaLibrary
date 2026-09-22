package com.example.medialibrary.backend.repository;

import static com.example.medialibrary.backend.repository.database.DatabaseKeyNames.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.medialibrary.backend.models.book.*;
import com.example.medialibrary.backend.models.book.Enums;
import com.example.medialibrary.backend.models.shared.*;
import com.example.medialibrary.backend.repository.Interface.Interface.IBookRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BookRepository extends BaseRepository implements IBookRepository {

    public BookRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

    //region Books
    @Override
    public <T extends Filter> List<Book> GetBooks(T filter) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = buildWhereClause(filter, selectionArgs);

        Cursor bookCursor = db.query(
                TABLE_BOOKS,
                null,
                whereClause,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );



        if (bookCursor != null && bookCursor.moveToFirst()) {
            do {
                Book book = new Book();
                mapMediaItem(bookCursor, book);
                book.MediaType = com.example.medialibrary.backend.models.shared.Enums.MediaType.Book;
                book.Id = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_ID));
                book.Author = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
                book.Artist = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_ARTIST));
                book.Publisher = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_PUBLISHER));
                book.Tag = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_TAG));

                byte[] bookCover = bookCursor.getBlob(bookCursor.getColumnIndexOrThrow(COLUMN_COVER));
                book.Cover = decompressBitmap(bookCover);
                book.Genre = deserializeGenre(bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                book.Items = GetBookItems(book.Id);
                book.HasSeriesEnded = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_HAS_ENDED)) == 1;
                book.HasCollectedAllItems = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COLLECTING)) == 1;

                book.Collecting = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
                int typeValue = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_TYPE));
                if (typeValue >= 0 && typeValue < Enums.BookType.values().length) {
                    book.Type = Enums.BookType.values()[typeValue];
                }


                books.add(book);
            } while (bookCursor.moveToNext());
            bookCursor.close();
        }

        return books;
    }

    @Override
    public Book GetBook(int id) {

        Book book = new Book();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(id));

        String whereClause = COLUMN_ID + " = ?";

        //region Book

        Cursor bookCursor = db.query(
                TABLE_BOOKS,
                null,
                whereClause,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );

        if (bookCursor != null && bookCursor.moveToFirst()) {
            do {
                mapMediaItem(bookCursor, book);
                book.MediaType = com.example.medialibrary.backend.models.shared.Enums.MediaType.Book;

                book.Author = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
                book.Artist = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_ARTIST));
                book.Publisher = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_PUBLISHER));
                book.Tag = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_TAG));
                book.Cover = bookCursor.getBlob(bookCursor.getColumnIndexOrThrow(COLUMN_COVER));
                book.Genre = deserializeGenre(bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                book.HasSeriesEnded = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_HAS_ENDED)) == 1;
                book.HasCollectedAllItems = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COLLECTING)) == 1;
                book.Items = GetBookItems(id);
                book.Collecting = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
                int typeValue = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_TYPE));
                if (typeValue >= 0 && typeValue < Enums.BookType.values().length) {
                    book.Type = Enums.BookType.values()[typeValue];
                }
            } while (bookCursor.moveToNext());
            bookCursor.close();
        }

        //endregion

        return book;
    }

    @Override
    public boolean AddBook(BookSaveObject bookObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long publisherId = bookObj.book.Publisher;
            if (bookObj.NewPubliser != null && !bookObj.NewPubliser.isEmpty()) {
                ContentValues pubValues = new ContentValues();
                pubValues.put(COLUMN_NAME, bookObj.NewPubliser);
                publisherId = db.insert(TABLE_PUBLISHERS, null, pubValues);
            }

            long tagId = bookObj.book.Tag;
            if (bookObj.NewTag != null && !bookObj.NewTag.isEmpty()) {
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_NAME, bookObj.NewTag);
                tagId = db.insert(TABLE_TAGS, null, tagValues);
            }

            ContentValues bookValues = new ContentValues();
            bookValues.put(COLUMN_TITLE, bookObj.book.Title);
            bookValues.put(COLUMN_COLLECTING, (bookObj.book.Collecting != null && bookObj.book.Collecting) ? 1 : 0);
            bookValues.put(COLUMN_HAS_ENDED, (bookObj.book.HasSeriesEnded != null && bookObj.book.HasSeriesEnded) ? 1 : 0);
            bookValues.put(COLUMN_COMPLETED_COLLECTING, (bookObj.book.HasCollectedAllItems != null && bookObj.book.HasCollectedAllItems) ? 1 : 0);
            bookValues.put(COLUMN_TAG, tagId);
            bookValues.put(COLUMN_COVER, compressBitmap(bookObj.book.Cover));
            bookValues.put(COLUMN_GENRE, serializeGenre(bookObj.book.Genre));
            bookValues.put(COLUMN_AUTHOR, bookObj.book.Author);
            bookValues.put(COLUMN_ARTIST, bookObj.book.Artist);
            bookValues.put(COLUMN_TYPE, bookObj.book.Type != null ? bookObj.book.Type.ordinal() : 0);
            bookValues.put(COLUMN_PUBLISHER, publisherId);

            long bookId = db.insert(TABLE_BOOKS, null, bookValues);

            if (bookObj.book.Items != null) {
                for (BookItem item : bookObj.book.Items) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(COLUMN_SERIES, bookId);
                    itemValues.put(COLUMN_VOLUME_NUMBER, item.VolumeNumber);
                    itemValues.put(COLUMN_VOLUME_TITLE, item.VolumeTitle);
                    itemValues.put(COLUMN_READ, item.Read ? 1 : 0);
                    itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
                    itemValues.put(COLUMN_FORMAT, item.Format != null ? item.Format.ordinal() : 0);
                    itemValues.put(COLUMN_ITEM_COVER,  compressBitmap(item.ItemCover));
                    db.insert(TABLE_BOOK_ITEMS, null, itemValues);
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
    public boolean UpdateBook(BookSaveObject bookObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long publisherId = bookObj.book.Publisher;
            if (bookObj.NewPubliser != null && !bookObj.NewPubliser.isEmpty()) {
                ContentValues pubValues = new ContentValues();
                pubValues.put(COLUMN_NAME, bookObj.NewPubliser);
                publisherId = db.insert(TABLE_PUBLISHERS, null, pubValues);
            }

            long tagId = bookObj.book.Tag;
            if (bookObj.NewTag != null && !bookObj.NewTag.isEmpty()) {
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_NAME, bookObj.NewTag);
                tagId = db.insert(TABLE_TAGS, null, tagValues);
            }

            ContentValues bookValues = new ContentValues();
            bookValues.put(COLUMN_TITLE, bookObj.book.Title);
            bookValues.put(COLUMN_COLLECTING, (bookObj.book.Collecting != null && bookObj.book.Collecting) ? 1 : 0);
            bookValues.put(COLUMN_HAS_ENDED, (bookObj.book.HasSeriesEnded != null && bookObj.book.HasSeriesEnded) ? 1 : 0);
            bookValues.put(COLUMN_COMPLETED_COLLECTING, (bookObj.book.HasCollectedAllItems != null && bookObj.book.HasCollectedAllItems) ? 1 : 0);
            bookValues.put(COLUMN_TAG, tagId);
            bookValues.put(COLUMN_COVER,  compressBitmap(bookObj.book.Cover));
            bookValues.put(COLUMN_GENRE, serializeGenre(bookObj.book.Genre));
            bookValues.put(COLUMN_AUTHOR, bookObj.book.Author);
            bookValues.put(COLUMN_ARTIST, bookObj.book.Artist);
            bookValues.put(COLUMN_TYPE, bookObj.book.Type != null ? bookObj.book.Type.ordinal() : 0);
            bookValues.put(COLUMN_PUBLISHER, publisherId);

            db.update(TABLE_BOOKS, bookValues, COLUMN_ID + " = ?", new String[]{String.valueOf(bookObj.book.Id)});

            // Delete old items and insert new ones to keep it simple and consistent with AddBook's structure
            db.delete(TABLE_BOOK_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(bookObj.book.Id)});

            if (bookObj.book.Items != null) {
                for (BookItem item : bookObj.book.Items) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(COLUMN_SERIES, bookObj.book.Id);
                    itemValues.put(COLUMN_VOLUME_NUMBER, item.VolumeNumber);
                    itemValues.put(COLUMN_VOLUME_TITLE, item.VolumeTitle);
                    itemValues.put(COLUMN_READ, item.Read ? 1 : 0);
                    itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
                    itemValues.put(COLUMN_FORMAT, item.Format != null ? item.Format.ordinal() : 0);
                    itemValues.put(COLUMN_ITEM_COVER,  compressBitmap(item.ItemCover));
                    db.insert(TABLE_BOOK_ITEMS, null, itemValues);
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
    public boolean DeleteBook(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete all associated book items first
            db.delete(TABLE_BOOK_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(id)});
            
            // Delete the book itself
            int deletedRows = db.delete(TABLE_BOOKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            
            db.setTransactionSuccessful();
            return deletedRows > 0;
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    //endregion

    //region publisher

    public List<Publisher> GetPublishers() {
        List<Publisher> publishers = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();

        Cursor cursor = db.query(
                TABLE_PUBLISHERS,
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
                publishers.add(new Publisher(id, name));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return publishers;
    }

    @Override
    public Publisher GetPublisher(int id) {

        Publisher pub = new Publisher();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(id));

        String whereClause = COLUMN_ID + " = ?";

        Cursor bookCursor = db.query(
                TABLE_PUBLISHERS,
                null,
                whereClause,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );

        if (bookCursor != null && bookCursor.moveToFirst()) {
            do {
               pub.Id = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_ID));
               pub.Name = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_NAME));

            } while (bookCursor.moveToNext());
            bookCursor.close();
        }

        return pub;

    }

    @Override
    public boolean AddPublisher(Publisher publisher) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (publisher.Name != null && !publisher.Name.isEmpty()) {
                ContentValues pubValues = new ContentValues();
                pubValues.put(COLUMN_NAME, publisher.Name);
                long id = db.insert(TABLE_PUBLISHERS, null, pubValues);
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
    public boolean UpdatePublisher(Publisher publisher) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, publisher.Name);

            int updatedRows = db.update(TABLE_PUBLISHERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(publisher.Id)});
            db.setTransactionSuccessful();
            return updatedRows > 0;
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean DeletePublisher(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {

            // Delete the book itself
            int deletedRows = db.delete(TABLE_PUBLISHERS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

            db.setTransactionSuccessful();
            return deletedRows > 0;
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    //endregion

    private List<BookItem> GetBookItems(int bookId)
    {
        List<BookItem> items = new ArrayList<>();
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
                BookItem item = new BookItem();
                mapBookItem(cursor, item);
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    private void mapBookItem(Cursor cursor, BookItem item) {
        item.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.VolumeNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOLUME_NUMBER));
        item.VolumeTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOLUME_TITLE));
        item.Series = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERIES));
        item.Read = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_READ)) == 1;
        item.Format = Enums.BookFormat.values()[cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FORMAT))];
        item.Owned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNED)) == 1;
        var itemCover = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ITEM_COVER));
        item.ItemCover = decompressBitmap(itemCover);
    }
}
