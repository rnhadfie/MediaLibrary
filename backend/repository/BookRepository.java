package repository;

import static utils.DatabaseKeyNames.*;
import static utils.DatabaseMappings.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import models.book.*;
import repository.Interface.Interface.IBookRepository;
import repository.database.BaseRepository;
import repository.database.MediaLibraryDbHelper;
import utils.NaturalComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
                    MapBook(bookCursor, book);
                    book.Items = GetBookItems(book.Id);
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
    public Book GetBook(String id) {
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
                    MapBook(bookCursor, book);
                    book.Items = GetBookItems(book.Id);

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
            String publisherId = bookObj.book.Publisher;
            if (bookObj.NewPublisher != null && !bookObj.NewPublisher.isEmpty()) {

                String primKey = UUID.randomUUID().toString();
                ContentValues pubValues = new ContentValues();
                pubValues.put(COLUMN_ID, primKey);
                pubValues.put(COLUMN_NAME, bookObj.NewPublisher);
                db.insert(TABLE_PUBLISHERS, null, pubValues);
                publisherId = primKey;
            }

            String tagId = AddNewTag(bookObj.book.Tag, bookObj.NewTag, db);

            String bookId = UUID.randomUUID().toString();
            bookObj.book.Id = bookId;

            ContentValues bookValues = MapBookContentValues(publisherId, tagId, bookObj.book);

            db.insert(TABLE_BOOKS, null, bookValues);

            if (bookObj.book.Items != null) {
                for (BookItem item : bookObj.book.Items) {
                    ContentValues itemValues = MapBookItemContentValues(bookId, item);
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
            String publisherId = bookObj.book.Publisher;

            if (bookObj.NewPublisher != null && !bookObj.NewPublisher.isEmpty()) {
                publisherId = UUID.randomUUID().toString();
                ContentValues pubValues = new ContentValues();
                pubValues.put(COLUMN_ID, publisherId);
                pubValues.put(COLUMN_NAME, bookObj.NewPublisher);
                db.insert(TABLE_PUBLISHERS, null, pubValues);
            }

            String tagId = bookObj.book.Tag;
            if (bookObj.NewTag != null && !bookObj.NewTag.isEmpty()) {
                tagId = UUID.randomUUID().toString();
                ContentValues tagValues = new ContentValues();
                tagValues.put(COLUMN_ID, tagId);
                tagValues.put(COLUMN_NAME, bookObj.NewTag);
                db.insert(TABLE_TAGS, null, tagValues);
            }

            ContentValues bookValues = MapBookContentValues(publisherId, tagId, bookObj.book);

            db.update(TABLE_BOOKS, bookValues, COLUMN_ID + " = ?", new String[]{String.valueOf(bookObj.book.Id)});

            db.delete(TABLE_BOOK_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(bookObj.book.Id)});

            if (bookObj.book.Items != null) {
                for (BookItem item : bookObj.book.Items) {
                    ContentValues itemValues = MapBookItemContentValues(bookObj.book.Id, item);
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
    public boolean DeleteBook(String id) {
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
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
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
                pubValues.put(COLUMN_ID, UUID.randomUUID().toString());
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
    public boolean DeletePublisher(String id, boolean forceDelete) {


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {

            if (!forceDelete) {
                ContentValues newContentValues = new ContentValues();
                newContentValues.put(COLUMN_PUBLISHER, "");

                db.update(TABLE_BOOKS, newContentValues, COLUMN_PUBLISHER + " = ?", new String[]{id});
            }

            int deletedRows = db.delete(TABLE_PUBLISHERS, COLUMN_ID + " = ?", new String[]{id});
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

    @Override
    public boolean PublisherIsBeingUsed(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE_BOOKS, null, COLUMN_PUBLISHER + " = ?", new String[]{id}, null, null, null);
            int result = cursor.getCount();
            cursor.close();
            return result > 0;
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return false;
        }
    }

    //endregion

    private List<BookItem> GetBookItems(String bookId) {
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
                MapBookItem(cursor, item);
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        items.sort(new NaturalComparator<>(BookItem::GetVolumeNumber));
        return items;
    }




}
