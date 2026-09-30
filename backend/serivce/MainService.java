package serivce;

import models.shared.Enums;
import models.shared.DisplayMediaItem;
import models.shared.Filter;
import models.shared.GenreObject;
import models.shared.MainSetup;
import models.shared.MediaItem;
import models.shared.Tag;
import repository.*;
import repository.database.MediaLibraryDbHelper;

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

    private final SharedService _sharedService;

    public MainService(MediaLibraryDbHelper dbHelper) {
        this.bookRepository = LazyKt.lazy(() -> new BookRepository(dbHelper));
        this.videoRepository = LazyKt.lazy(() -> new VideoRepository(dbHelper));
        this.musicRepository = LazyKt.lazy(() -> new MusicRepository(dbHelper));
        this.otherRepository = LazyKt.lazy(() -> new OtherRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));

        _sharedService = this.sharedService.getValue();
    }

    public List<DisplayMediaItem> GetDisplayList(Filter filter) {
        List<DisplayMediaItem> allItems = new ArrayList<>();

        List<String> selectionArgs = new ArrayList<>();
        String whereClause = _sharedService.BuildWhereClause(filter, selectionArgs);
        boolean allMedia = filter.MediaType == null || filter.MediaType == Enums.MediaType.None;

        if(allMedia || filter.MediaType == Enums.MediaType.Book) {
            allItems.addAll(_sharedService.mapToDisplayItems(bookRepository.getValue().GetBooks(whereClause, selectionArgs), true));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Video) {
            allItems.addAll(_sharedService.mapToDisplayItems(videoRepository.getValue().GetVideos(whereClause, selectionArgs), true));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Music){
            allItems.addAll(_sharedService.mapToDisplayItems(musicRepository.getValue().GetMusic(whereClause, selectionArgs), true));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Other) {
            allItems.addAll(_sharedService.mapToDisplayItems(otherRepository.getValue().GetOtherCollections(whereClause, selectionArgs), true));
        }

        allItems = _sharedService.SortDisplayItem(filter, allItems);
        return allItems;
    }

    public List<MediaItem> GetAllItems(Filter filter) {
        List<MediaItem> allItems = new ArrayList<>();

        List<String> selectionArgs = new ArrayList<>();
        String whereClause = _sharedService.BuildWhereClause(filter, selectionArgs);
        boolean allMedia = filter.MediaType == null || filter.MediaType == Enums.MediaType.None;

        if(allMedia || filter.MediaType == Enums.MediaType.Book) {
            allItems.addAll(_sharedService.mapToMediaItems(bookRepository.getValue().GetBooks(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Video) {
            allItems.addAll(_sharedService.mapToMediaItems(videoRepository.getValue().GetVideos(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Music){
            allItems.addAll(_sharedService.mapToMediaItems(musicRepository.getValue().GetMusic(whereClause, selectionArgs)));
        }
        if(allMedia || filter.MediaType == Enums.MediaType.Other) {
            allItems.addAll(_sharedService.mapToMediaItems(otherRepository.getValue().GetOtherCollections(whereClause, selectionArgs)));
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
        var tags = _sharedService.GetTags();
         tags.sort(Comparator.comparing(o -> o.Name));
        return tags;
    }

    public boolean AddTag(Tag tag) {
        return _sharedService.AddTag(tag);
    }

    public boolean UpdateTag(Tag tag) {
        return _sharedService.UpdateTag(tag);
    }

    public boolean DeleteTag(String id) {
        return _sharedService.DeleteTag(id);
    }

    public List<GenreObject> GetGenres() {
        return  _sharedService.GetGenres();
    }

    public Map<Integer,String> GetMediaTypes() {
        Enums.MediaType[] types = Enums.MediaType.values();
        Map<Integer,String> typeMap = new HashMap<>();
        for (Enums.MediaType type : types) {
            typeMap.put(type.ordinal(), _sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    }




}
