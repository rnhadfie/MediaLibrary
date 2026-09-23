package com.example.medialibrary.backend.repository;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

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

import java.util.ArrayList;

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
    public void SaveAllData(DataContainer container) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete all existing data
            db.delete(TABLE_OTHER_ITEMS, null, null);
            db.delete(TABLE_OTHERS, null, null);
            db.delete(TABLE_VIDEO_ITEMS, null, null);
            db.delete(TABLE_VIDEOS, null, null);
            db.delete(TABLE_MUSIC, null, null);
            db.delete(TABLE_BOOK_ITEMS, null, null);
            db.delete(TABLE_BOOKS, null, null);
            db.delete(TABLE_PUBLISHERS, null, null);
            db.delete(TABLE_TAGS, null, null);

            // Reset auto-increment counters
            db.execSQL("DELETE FROM sqlite_sequence WHERE name IN ('" + 
                TABLE_TAGS + "', '" + TABLE_PUBLISHERS + "', '" + TABLE_BOOKS + "', '" + 
                TABLE_BOOK_ITEMS + "', '" + TABLE_MUSIC + "', '" + TABLE_VIDEOS + "', '" + 
                TABLE_VIDEO_ITEMS + "', '" + TABLE_OTHERS + "', '" + TABLE_OTHER_ITEMS + "')");

            // Insert new data
            if (container.Tags != null) {
                for (Tag t : container.Tags) {
                    sharedRepository.AddTag(t);
                }
            }
            if (container.Publishers != null) {
                for (Publisher p : container.Publishers) {
                    bookRepository.AddPublisher(p);
                }
            }
            if (container.Books != null) {
                for (Book b : container.Books) {
                    BookSaveObject bso = new BookSaveObject();
                    bso.book = b;
                    bso.book.Items = b.Items;
                    bookRepository.AddBook(bso);
                }
            }
            if (container.Videos != null) {
                for (Video v : container.Videos) {
                    VideoSaveObject vso = new VideoSaveObject();
                    vso.video = v;
                    vso.video.Items = v.Items;
                    videoRepository.AddVideo(vso);
                }
            }
            if (container.Music != null) {

                for (Music m : container.Music) {
                    MusicObj mo = new MusicObj();
                    mo.Music = m;
                    musicRepository.AddMusic(mo);
                }
            }
            if (container.Others != null) {
                for (Other o : container.Others) {
                    OtherSaveObj os = new OtherSaveObj();
                    os.Other = o;
                    otherRepository.AddOtherCollection(os);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
