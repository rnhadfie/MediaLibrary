package com.example.medialibrary.backend.Serivce;

import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.models.shared.MainSetup;
import com.example.medialibrary.backend.models.shared.MediaItem;
import com.example.medialibrary.backend.models.shared.Tag;
import com.example.medialibrary.backend.repository.*;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Lazy;
import kotlin.LazyKt;

public class MainSerivce {
    private final MediaLibraryDbHelper dbHelper;

    public Lazy<BookRepository> bookRepository;
    public Lazy<VideoRepository> videoRepository;
    public Lazy<MusicRepository> musicRepository;
    public Lazy<OtherRepository> otherRepository;

    public Lazy<SharedService> sharedService;

    public MainSerivce(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.bookRepository = LazyKt.lazy(() -> new BookRepository(dbHelper));
        this.videoRepository = LazyKt.lazy(() -> new VideoRepository(dbHelper));
        this.musicRepository = LazyKt.lazy(() -> new MusicRepository(dbHelper));
        this.otherRepository = LazyKt.lazy(() -> new OtherRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));
    }

    public List<DisplayMediaItem> GetDisplayList(Filter filter) {
        List<DisplayMediaItem> allItems = new ArrayList<>();

        var sharedService = this.sharedService.getValue();

        allItems.addAll(sharedService.mapToDisplayItems(bookRepository.getValue().GetBooks(filter)));
        allItems.addAll(sharedService.mapToDisplayItems(videoRepository.getValue().GetVideos(filter)));
        allItems.addAll(sharedService.mapToDisplayItems(musicRepository.getValue().GetMusic(filter)));
        allItems.addAll(sharedService.mapToDisplayItems(otherRepository.getValue().GetOtherCollections(filter)));

        return allItems;
    }

    public List<MediaItem> GetAllItems(Filter filter) {
        List<MediaItem> allItems = new ArrayList<>();
        var sharedService = this.sharedService.getValue();

        allItems.addAll(sharedService.mapToMediaItems(bookRepository.getValue().GetBooks(filter)));
        allItems.addAll(sharedService.mapToMediaItems(videoRepository.getValue().GetVideos(null)));
        allItems.addAll(sharedService.mapToMediaItems(musicRepository.getValue().GetMusic(null)));
        allItems.addAll(sharedService.mapToMediaItems(otherRepository.getValue().GetOtherCollections(null)));

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
        return this.sharedService.getValue().GetTags();
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

    public Map<Integer,String> GetGenres() {
        var sharedService = this.sharedService.getValue();
        com.example.medialibrary.backend.models.shared.Enums.Genre[] genres = com.example.medialibrary.backend.models.shared.Enums.Genre.values();
        Map<Integer,String> genreMap = new HashMap<>();
        for (com.example.medialibrary.backend.models.shared.Enums.Genre genre : genres) {
            genreMap.put(genre.ordinal(), sharedService.GetSeperatedString(genre.toString()));
        }
        return genreMap;
    };

    public Map<Integer,String> GetMediaTypes() {
        var sharedService = this.sharedService.getValue();
        Enums.MediaType[] types = Enums.MediaType.values();
        Map<Integer,String> typeMap = new HashMap<>();
        for (Enums.MediaType type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    };




}
