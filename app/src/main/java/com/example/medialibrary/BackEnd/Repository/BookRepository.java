package com.example.medialibrary.backend.repository;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.medialibrary.backend.models.book.*;
import com.example.medialibrary.backend.models.book.Enums;
import com.example.medialibrary.backend.repository.Interface.Interface.IBookRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import android.util.LruCache;

public class BookRepository extends BaseRepository implements IBookRepository {

    private static final LruCache<String, List<Book>> listCache = new LruCache<>(30);
    private static final LruCache<String, List<Publisher>> listPubCache = new LruCache<>(5);

    public BookRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
    }

    public static void clearCache() {
        listCache.evictAll();
        listPubCache.evictAll();
    }

    //region Books

    @Override
    public List<Book> GetBooks() {
        return GetBooks("", new ArrayList<>());
    }

    @Override
    public List<Book> GetBooks(String whereClause, List<String> selectionArgs) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if (whereClause == null) {
            whereClause = "";
        }

        String argsKey = (selectionArgs != null && !selectionArgs.isEmpty()) ? String.join(",", selectionArgs) : "";
        String cacheKey = "Book_" + whereClause + "_" + argsKey;

        List<Book> cachedList = listCache.get(cacheKey);
        if (cachedList != null) {
            return cachedList; // Cache hit!
        }

        try {
            Cursor bookCursor = db.query(
                    TABLE_BOOKS,
                    null,
                    whereClause,
                    (selectionArgs == null || selectionArgs.isEmpty()) ? null : selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (bookCursor.moveToFirst()) {
                do {
                    Book book = new Book();
                    mapBook(bookCursor, book);
                    books.add(book);
                } while (bookCursor.moveToNext());
                bookCursor.close();
            }
        } catch (Exception ex) {
            return null;
        }
        listCache.put(cacheKey, books);
        return books;
    }

    @Override
    public Book GetBook(int id) {
        Book book = new Book();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            List<String> selectionArgs = new ArrayList<>();
            selectionArgs.add(String.valueOf(id));

            String whereClause = COLUMN_ID + " = ?";

            Cursor bookCursor = db.query(
                    TABLE_BOOKS,
                    null,
                    whereClause,
                    selectionArgs.toArray(new String[0]),
                    null,
                    null,
                    null
            );

            if (bookCursor.moveToFirst()) {
                do {
                    mapBook(bookCursor, book);
                } while (bookCursor.moveToNext());
                bookCursor.close();
            }
        } catch (Exception ex) {
            return null;
        }

        return book;
    }

    @Override
    public boolean AddBook(BookSaveObject bookObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long publisherId = bookObj.book.Publisher;
            if (bookObj.NewPublisher != null && !bookObj.NewPublisher.isEmpty()) {
                ContentValues pubValues = new ContentValues();
                pubValues.put(COLUMN_NAME, bookObj.NewPublisher);
                publisherId = db.insert(TABLE_PUBLISHERS, null, pubValues);
            }

            long tagId = bookObj.book.Tag;
            if (bookObj.NewTag != null && !bookObj.NewTag.isEmpty()) {
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_NAME, bookObj.NewTag);
                tagId = db.insert(TABLE_TAGS, null, tagValues);
            }

            ContentValues bookValues = mapBookContentValues(publisherId, tagId, bookObj.book);

            long bookId = db.insert(TABLE_BOOKS, null, bookValues);

            if (bookObj.book.Items != null) {
                for (BookItem item : bookObj.book.Items) {
                    ContentValues itemValues = mapBookItemContentValues(bookId, item);
                    db.insert(TABLE_BOOK_ITEMS, null, itemValues);
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
    public boolean UpdateBook(BookSaveObject bookObj) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long publisherId = bookObj.book.Publisher;
            if (bookObj.NewPublisher != null && !bookObj.NewPublisher.isEmpty()) {
                ContentValues pubValues = new ContentValues();
                pubValues.put(COLUMN_NAME, bookObj.NewPublisher);
                publisherId = db.insert(TABLE_PUBLISHERS, null, pubValues);
            }

            long tagId = bookObj.book.Tag;
            if (bookObj.NewTag != null && !bookObj.NewTag.isEmpty()) {
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_NAME, bookObj.NewTag);
                tagId = db.insert(TABLE_TAGS, null, tagValues);
            }

            ContentValues bookValues = mapBookContentValues(publisherId, tagId, bookObj.book);

            db.update(TABLE_BOOKS, bookValues, COLUMN_ID + " = ?", new String[]{String.valueOf(bookObj.book.Id)});

            db.delete(TABLE_BOOK_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(bookObj.book.Id)});

            if (bookObj.book.Items != null) {
                for (BookItem item : bookObj.book.Items) {
                    ContentValues itemValues = mapBookItemContentValues(bookObj.book.Id, item);
                    db.insert(TABLE_BOOK_ITEMS, null, itemValues);
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
    public boolean DeleteBook(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_BOOK_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(id)});
            int deletedRows = db.delete(TABLE_BOOKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return deletedRows > 0;
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    //endregion

    //region publisher

    public List<Publisher> GetPublishers() {
        List<Publisher> publishers = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String cacheKey = "Publisher";
        List<Publisher> cachedList = listPubCache.get(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        try {
            Cursor cursor = db.query(
                    TABLE_PUBLISHERS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    publishers.add(new Publisher(id, name));
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception ex) {
            return null;
        }
        listPubCache.put(cacheKey, publishers);
        return publishers;
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
            clearAllCaches();
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
            clearAllCaches();
            db.endTransaction();
        }
    }

    @Override
    public boolean DeletePublisher(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int deletedRows = db.delete(TABLE_PUBLISHERS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return deletedRows > 0;
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return false;
        } finally {
            clearAllCaches();
            db.endTransaction();
        }
    }

    //endregion

    private List<BookItem> GetBookItems(int bookId) {
        List<BookItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(bookId));
        String whereClause = COLUMN_SERIES + " = ?";
        Cursor cursor = db.query(
                TABLE_BOOK_ITEMS,
                null,
                whereClause,
                selectionArgs.toArray(new String[0]),
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            do {
                BookItem item = new BookItem();
                mapBookItem(cursor, item);
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        items.sort(new NaturalComparator<>(BookItem::GetVolumeNumber));
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

    private void mapBook(Cursor bookCursor, Book book) {
        mapMediaItem(bookCursor, book);
        book.MediaType = com.example.medialibrary.backend.models.shared.Enums.MediaType.Book;
        book.Author = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
        book.Artist = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_ARTIST));
        book.Publisher = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_PUBLISHER));

        book.Items = GetBookItems(book.Id);

        int typeValue = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_TYPE));
        if (typeValue >= 0 && typeValue < Enums.BookType.values().length) {
            book.Type = Enums.BookType.values()[typeValue];
        }
    }

    private ContentValues mapBookItemContentValues(long bookId, BookItem item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_SERIES, bookId);
        itemValues.put(COLUMN_VOLUME_NUMBER, item.VolumeNumber);
        itemValues.put(COLUMN_VOLUME_TITLE, item.VolumeTitle);
        itemValues.put(COLUMN_READ, item.Read ? 1 : 0);
        itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
        itemValues.put(COLUMN_FORMAT, item.Format != null ? item.Format.ordinal() : 0);
        itemValues.put(COLUMN_ITEM_COVER, compressBitmap(item.ItemCover));
        return itemValues;
    }

    private ContentValues mapBookContentValues(long publisherId, long tagId, Book book) {
        ContentValues bookValues = new ContentValues();
        bookValues.put(COLUMN_TITLE, book.Title);
        bookValues.put(COLUMN_COLLECTING, (book.Collecting != null && book.Collecting) ? 1 : 0);
        bookValues.put(COLUMN_HAS_ENDED, (book.Ongoing != null && book.Ongoing) ? 1 : 0);
        bookValues.put(COLUMN_COMPLETED_COLLECTING, (book.HasCollectedAllItems != null && book.HasCollectedAllItems) ? 1 : 0);
        bookValues.put(COLUMN_TAG, tagId);
        bookValues.put(COLUMN_COVER, compressBitmap(book.Cover));
        bookValues.put(COLUMN_GENRE, serializeGenre(book.Genre));
        bookValues.put(COLUMN_AUTHOR, book.Author);
        bookValues.put(COLUMN_ARTIST, book.Artist);
        bookValues.put(COLUMN_TYPE, book.Type != null ? book.Type.ordinal() : 0);
        bookValues.put(COLUMN_PUBLISHER, publisherId);
        return bookValues;
    }
}
