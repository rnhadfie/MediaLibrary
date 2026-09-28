package com.example.medialibrary.backend.repository;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;
import static com.example.medialibrary.backend.utils.DatabaseMappings.*;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.example.medialibrary.backend.models.book.*;
import com.example.medialibrary.backend.models.music.*;
import com.example.medialibrary.backend.models.other.*;
import com.example.medialibrary.backend.models.shared.DataContainer;
import com.example.medialibrary.backend.models.shared.Tag;
import com.example.medialibrary.backend.models.video.*;
import com.example.medialibrary.backend.repository.Interface.Interface.IXmlRepository;
import com.example.medialibrary.backend.repository.database.BaseRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;
import com.example.medialibrary.backend.utils.DatabaseMappings;
import com.example.medialibrary.backend.utils.UUIDValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class XmlRepository extends BaseRepository implements IXmlRepository {

    private final BookRepository bookRepository;
    private final VideoRepository videoRepository;
    private final MusicRepository musicRepository;
    private final OtherRepository otherRepository;
    private final SharedRepository sharedRepository;

    public XmlRepository(MediaLibraryDbHelper dbHelper) {
        super(dbHelper);
        this.bookRepository = new BookRepository(dbHelper);
        this.videoRepository = new VideoRepository(dbHelper);
        this.musicRepository = new MusicRepository(dbHelper);
        this.otherRepository = new OtherRepository(dbHelper);
        this.sharedRepository = new SharedRepository(dbHelper);
    }

    // MOVE TO SERVICE
    @Override
    public DataContainer GetAllData() {
        DataContainer container = new DataContainer();
        container.Tags = sharedRepository.GetTags();
        container.Publishers = bookRepository.GetPublishers();
        
        container.Books = new ArrayList<>();
        for (Book b : bookRepository.GetBooks()) {
            container.Books.add(bookRepository.GetBook(b.Id));
        }
        
        container.Videos = new ArrayList<>();
        for (Video v : videoRepository.GetVideos()) {
            container.Videos.add(videoRepository.GetVideo(v.Id));
        }
        
        container.Music = musicRepository.GetMusic();
        container.Others = otherRepository.GetOtherCollections();
        
        return container;
    }

    @Override
    public String SaveAllData(DataContainer container) {
        StringBuilder importResult = new StringBuilder();
        importResult.append("Importing Results").append(System.lineSeparator()).append(System.lineSeparator());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Insert new data
            if (container.Tags != null) {
                importResult.append("Importing Tags").append(System.lineSeparator());
                var counts =  ImportTags(db, container);
                importResult.append("Added: ").append(counts.get("Added")).append(System.lineSeparator());
                importResult.append("Updated: ").append(counts.get("Updated")).append(System.lineSeparator());
                importResult.append(System.lineSeparator());
            }
            if (container.Publishers != null) {
                importResult.append("Importing Publishers").append(System.lineSeparator());
                var counts = ImportPublishers(db,container);
                importResult.append("Added: ").append(counts.get("Added")).append(System.lineSeparator());
                importResult.append("Updated: ").append(counts.get("Updated")).append(System.lineSeparator());
                importResult.append(System.lineSeparator());

            }
            if (container.Books != null) {
                importResult.append("Importing Books").append(System.lineSeparator());
                var counts = ImportBooks(db, container);
                importResult.append("Added: ").append(counts.get("Added")).append(System.lineSeparator());
                importResult.append("Updated: ").append(counts.get("Updated")).append(System.lineSeparator());
                importResult.append(System.lineSeparator());

            }
            if (container.Videos != null) {
                importResult.append("Importing Movies and TV Shows").append(System.lineSeparator());
                var counts = ImportVideos(db, container);
                importResult.append("Added: ").append(counts.get("Added")).append(System.lineSeparator());
                importResult.append("Updated: ").append(counts.get("Updated")).append(System.lineSeparator());
                importResult.append(System.lineSeparator());
            }
            if (container.Music != null) {
                importResult.append("Importing Cds").append(System.lineSeparator());
                var counts = ImportMusicCollections(db, container.Music);
                importResult.append("Added: ").append(counts.get("Added")).append(System.lineSeparator());
                importResult.append("Updated: ").append(counts.get("Updated")).append(System.lineSeparator());
                importResult.append(System.lineSeparator());
            }
            if (container.Others != null) {
                importResult.append("Importing Other Collections").append(System.lineSeparator());
                var counts = ImportOtherCollections(db, container.Others);
                importResult.append("Added: ").append(counts.get("Added")).append(System.lineSeparator());
                importResult.append("Updated: ").append(counts.get("Updated")).append(System.lineSeparator());
                importResult.append(System.lineSeparator());
            }
            db.setTransactionSuccessful();
            return importResult.toString();
        }
        catch (Exception e) {
            Logger.getLogger(XmlRepository.class.getName()).severe(e.getMessage());
            db.endTransaction();
            return "Fail to Import Data";
        }
        finally {
            clearAllCaches();
            db.endTransaction();

        }
    }

    public Map<String, Integer> ImportTags(SQLiteDatabase db, DataContainer importObj)
    {
        List<Tag> tags = importObj.Tags;
        int addedCounter = 0;
        int updateCounter = 0;
        try {

            for (Tag tag : tags) {
                String tagId = tag.Id;
                if(!UUIDValidator.isValidUUID(tag.Id))
                {
                    tagId = UUID.randomUUID().toString();
                    String finalTagId = tagId;
                    importObj.Books.stream().filter(book -> Objects.equals(book.Publisher, tag.Id)).forEach(book->book.Tag = finalTagId);
                    importObj.Videos.stream().filter(video -> Objects.equals(video.Tag, tag.Id)).forEach(video->video.Tag = finalTagId);
                    importObj.Music.stream().filter(music -> Objects.equals(music.Tag, tag.Id)).forEach(music->music.Tag = finalTagId);
                    importObj.Others.stream().filter(other -> Objects.equals(other.Tag, tag.Id)).forEach(other->other.Tag = finalTagId);
                }
                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, tagId);
                values.put(COLUMN_NAME, tag.Name);
                long id = db.insertWithOnConflict(TABLE_PUBLISHERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {
                    addedCounter++;
                } else {
                    values = new ContentValues();
                    values.put(COLUMN_NAME, tag.Name);

                    int updatedRows = db.update(TABLE_PUBLISHERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(tag.Id)});
                    if(updatedRows > 0) {
                        updateCounter++;
                    }
                }

            }

            return Map.of("Added", addedCounter, "Updated", updateCounter);
        } catch (Exception e) {
            Logger.getLogger(SharedRepository.class.getName()).severe(e.getMessage());
            return Map.of("Added", 0, "Updated", 0);
        }
    }

    public Map<String, Integer> ImportPublishers( SQLiteDatabase db, DataContainer importObj)
    {
        List<Publisher> publishers = importObj.Publishers;
        int addedCounter = 0;
        int updateCounter = 0;
        try {

            for (Publisher publisher : publishers) {
                String pubId = publisher.Id;
                if(!UUIDValidator.isValidUUID(publisher.Id))
                {
                    pubId = UUID.randomUUID().toString();
                    String finalPubId = pubId;
                    importObj.Books.stream().filter(book -> Objects.equals(book.Publisher, publisher.Id)).forEach(book->book.Publisher = finalPubId);
                }
                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, pubId);
                values.put(COLUMN_NAME, publisher.Name);
                long id = db.insertWithOnConflict(TABLE_PUBLISHERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {
                    addedCounter++;
                } else {
                    values = new ContentValues();
                    values.put(COLUMN_NAME, publisher.Name);

                    int updatedRows = db.update(TABLE_PUBLISHERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(publisher.Id)});
                    if(updatedRows > 0) {
                        updateCounter++;
                    }
                }
            }
            return Map.of("Added", addedCounter, "Updated", updateCounter);
        } catch (Exception e) {
            Logger.getLogger(BookRepository.class.getName()).severe(e.getMessage());
            return Map.of("Added", 0, "Updated", 0);
        }
    }

    public Map<String, Integer> ImportBooks(SQLiteDatabase db, DataContainer importObj)
    {
        List<Book> bookToImport = importObj.Books;
        int addedCounter = 0;
        int updateCounter = 0;

            for (Book importBook : bookToImport) {
                String newBookId = importBook.Id;
                if(!UUIDValidator.isValidUUID(importBook.Id))
                {
                    newBookId = UUID.randomUUID().toString();
                }
                importBook.Id = newBookId;

                ContentValues values = DatabaseMappings.MapBookContentValues(importBook.Publisher, importBook.Tag, importBook);
                long id = db.insertWithOnConflict(TABLE_BOOKS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {

                    addedCounter++;
                    if (importBook.Items != null) {
                        for (BookItem item : importBook.Items) {
                            ContentValues itemValues = MapBookItemContentValues(importBook.Id, item);
                            db.insert(TABLE_BOOK_ITEMS, null, itemValues);
                        }
                    }
                }
                else {
                    db.update(TABLE_BOOKS, values, COLUMN_ID + " = ?", new String[]{importBook.Id});

                    db.delete(TABLE_BOOK_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(importBook.Id)});

                    if (importBook.Items != null) {
                        for (BookItem item : importBook.Items) {
                            ContentValues itemValues = MapBookItemContentValues(importBook.Id, item);
                            db.insert(TABLE_BOOK_ITEMS, null, itemValues);
                        }
                    }
                }

            }

            return Map.of("Added", addedCounter, "Updated", updateCounter);

    }
    public Map<String, Integer> ImportVideos(SQLiteDatabase db, DataContainer importObj)
    {
        List<Video> videosToImport = importObj.Videos;
        int addedCounter = 0;
        int updateCounter = 0;

            for (Video importVideo : videosToImport) {
                String newVideoId = importVideo.Id;
                if(!UUIDValidator.isValidUUID(importVideo.Id))
                {
                    newVideoId = UUID.randomUUID().toString();
                }
                importVideo.Id = newVideoId;

                ContentValues values = DatabaseMappings.MapVideoContentValues(importVideo.Tag, importVideo);
                long id = db.insertWithOnConflict(TABLE_VIDEOS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {

                    addedCounter++;
                    if (importVideo.Items != null) {
                        for (VideoItem item : importVideo.Items) {
                            ContentValues itemValues = DatabaseMappings.MapVideoItemContentValues(importVideo.Id, item);
                            db.insert(TABLE_VIDEO_ITEMS, null, itemValues);
                        }
                    }
                }
                else {
                    db.update(TABLE_VIDEOS, values, COLUMN_ID + " = ?", new String[]{importVideo.Id});

                    db.delete(TABLE_VIDEO_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(importVideo.Id)});

                    if (importVideo.Items != null) {
                        for (VideoItem item : importVideo.Items) {
                            ContentValues itemValues = MapVideoItemContentValues(importVideo.Id, item);
                            db.insert(TABLE_VIDEO_ITEMS, null, itemValues);
                        }
                    }
                }

            }

            return Map.of("Added", addedCounter, "Updated", updateCounter);

    }

    public Map<String, Integer> ImportMusicCollections(SQLiteDatabase db, List<Music> musicToImport)
    {
        int addedCounter = 0;
        int updateCounter = 0;

            for (Music importMusic : musicToImport) {
                String newCDId = importMusic.Id;
                if(!UUIDValidator.isValidUUID(importMusic.Id))
                {
                    newCDId = UUID.randomUUID().toString();
                }
                importMusic.Id = newCDId;

                ContentValues values = DatabaseMappings.MapMusicContentValues(importMusic.Tag, importMusic);
                long id = db.insertWithOnConflict(TABLE_MUSIC, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {
                    addedCounter++;
                }
                else {
                    db.update(TABLE_MUSIC, values, COLUMN_ID + " = ?", new String[]{importMusic.Id});
                }

            }
            return Map.of("Added", addedCounter, "Updated", updateCounter);

    }

    public Map<String, Integer> ImportOtherCollections(SQLiteDatabase db, List<Other> otherToImport)
    {
        int addedCounter = 0;
        int updateCounter = 0;

            for (Other importOther : otherToImport) {
                String newOtherId = importOther.Id;
                if(!UUIDValidator.isValidUUID(importOther.Id))
                {
                    newOtherId = UUID.randomUUID().toString();
                }
                importOther.Id = newOtherId;

                ContentValues values = DatabaseMappings.MapOtherContentValues(importOther.Tag, importOther);
                long id = db.insertWithOnConflict(TABLE_OTHERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {

                    addedCounter++;
                    if (importOther.Items != null) {
                        for (OtherItem item : importOther.Items) {
                            ContentValues itemValues = MapOtherItemContentValues(importOther.Id, item);
                            db.insert(TABLE_OTHER_ITEMS, null, itemValues);
                        }
                    }
                }
                else {
                    db.update(TABLE_OTHERS, values, COLUMN_ID + " = ?", new String[]{importOther.Id});

                    db.delete(TABLE_OTHER_ITEMS, COLUMN_SERIES + " = ?", new String[]{String.valueOf(importOther.Id)});

                    if (importOther.Items != null) {
                        for (OtherItem item : importOther.Items) {
                            ContentValues itemValues = MapOtherItemContentValues(importOther.Id, item);
                            db.insert(TABLE_OTHER_ITEMS, null, itemValues);
                        }
                    }
                }

            }

            return Map.of("Added", addedCounter, "Updated", updateCounter);

    }

}
