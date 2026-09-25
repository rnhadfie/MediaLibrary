package com.example.medialibrary.backend.Serivce;

import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.models.shared.GenreObject;
import com.example.medialibrary.backend.models.shared.MainSetup;
import com.example.medialibrary.backend.models.shared.MediaItem;
import com.example.medialibrary.backend.models.shared.Tag;
import com.example.medialibrary.backend.repository.*;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Lazy;
import kotlin.LazyKt;

public class MainService {

    public Lazy<BookRepository> bookRepository;
    public Lazy<VideoRepository> videoRepository;
    public Lazy<MusicRepository> musicRepository;
    public Lazy<OtherRepository> otherRepository;

    public Lazy<SharedService> sharedService;

    public MainService(MediaLibraryDbHelper dbHelper) {
        this.bookRepository = LazyKt.lazy(() -> new BookRepository(dbHelper));
        this.videoRepository = LazyKt.lazy(() -> new VideoRepository(dbHelper));
        this.musicRepository = LazyKt.lazy(() -> new MusicRepository(dbHelper));
        this.otherRepository = LazyKt.lazy(() -> new OtherRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));
    }

    public List<DisplayMediaItem> GetDisplayList(Filter filter) {
        List<DisplayMediaItem> allItems = new ArrayList<>();

        var sharedService = this.sharedService.getValue();


        List<String> selectionArgs = new ArrayList<>();
        String whereClause = sharedService.BuildWhereClause(filter, selectionArgs);
        boolean allMedia = filter.MediaType == null || filter.MediaType == Enums.MediaType.None;

        if(allMedia || filter.MediaType == Enums.MediaType.Book) {
            allItems.addAll(sharedService.mapToDisplayItems(bookRepository.getValue().GetBooks(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Video) {
            allItems.addAll(sharedService.mapToDisplayItems(videoRepository.getValue().GetVideos(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Music){
            allItems.addAll(sharedService.mapToDisplayItems(musicRepository.getValue().GetMusic(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Other) {
            allItems.addAll(sharedService.mapToDisplayItems(otherRepository.getValue().GetOtherCollections(whereClause, selectionArgs)));
        }


        return allItems;
    }

    public List<MediaItem> GetAllItems(Filter filter) {
        List<MediaItem> allItems = new ArrayList<>();
        var sharedService = this.sharedService.getValue();

        List<String> selectionArgs = new ArrayList<>();
        String whereClause = sharedService.BuildWhereClause(filter, selectionArgs);
        boolean allMedia = filter.MediaType == null || filter.MediaType == Enums.MediaType.None;

        if(allMedia || filter.MediaType == Enums.MediaType.Book) {
            allItems.addAll(sharedService.mapToMediaItems(bookRepository.getValue().GetBooks(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Video) {
            allItems.addAll(sharedService.mapToMediaItems(videoRepository.getValue().GetVideos(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Music){
            allItems.addAll(sharedService.mapToMediaItems(musicRepository.getValue().GetMusic(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Other) {
            allItems.addAll(sharedService.mapToMediaItems(otherRepository.getValue().GetOtherCollections(whereClause, selectionArgs)));
        }

        return allItems;
    }

    public MainSetup GetSetup() {
        MainSetup setup = new MainSetup();
        setup.Genre = GetGenres();
        setup.MediaType = GetMediaTypes();
        setup.Tag = GetTags();
        return setup;
    }

    public List<Tag> GetTags() {
        var tags = this.sharedService.getValue().GetTags();
         tags.sort(Comparator.comparing(o -> o.Name));
        return tags;
    }

    public boolean AddTag(Tag tag) {
        return this.sharedService.getValue().AddTag(tag);
    }

    public boolean UpdateTag(Tag tag) {
        return this.sharedService.getValue().UpdateTag(tag);
    }

    public boolean DeleteTag(int id) {
        return this.sharedService.getValue().DeleteTag(id);
    }

    public List<GenreObject> GetGenres() {
        var sharedService = this.sharedService.getValue();
        return  sharedService.GetGenres();
    }

    public Map<Integer,String> GetMediaTypes() {
        var sharedService = this.sharedService.getValue();
        Enums.MediaType[] types = Enums.MediaType.values();
        Map<Integer,String> typeMap = new HashMap<>();
        for (Enums.MediaType type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    }




}
