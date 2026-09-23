package com.example.medialibrary.backend.Serivce;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_ARTIST;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_AUTHOR;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_COLLECTING;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_COMPLETED_COLLECTING;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_GENRE;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_PUBLISHER;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_TAG;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_TITLE;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_TYPE;
import static com.example.medialibrary.backend.utils.DatabaseKeyNames.COLUMN_VIDEO_TAG;
import static java.util.stream.Collectors.toList;

import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.BookFilter;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.models.shared.MediaItem;
import com.example.medialibrary.backend.models.shared.Tag;
import com.example.medialibrary.backend.models.video.Video;
import com.example.medialibrary.backend.models.video.VideoFilter;
import com.example.medialibrary.backend.repository.SharedRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Lazy;
import kotlin.LazyKt;

public class SharedService {
    private final MediaLibraryDbHelper dbHelper;

    public Lazy<SharedRepository> sharedRepository;

    public SharedService(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.sharedRepository = LazyKt.lazy(() -> new SharedRepository(dbHelper));
    }
    public <T extends MediaItem> List<MediaItem> mapToMediaItems(List<T> items) {
        if (items == null) return new ArrayList<>();
        return items.stream()
                .map(this::mapToMediaItem)
                .collect(toList());
    }

    public <T extends MediaItem> List<DisplayMediaItem> mapToDisplayItems(List<T> items) {
        if (items == null) return new ArrayList<>();
        return items.stream()
                .map(this::mapToDisplayItem)
                .collect(toList());
    }

    public <T extends MediaItem> DisplayMediaItem mapToDisplayItem(T item) {
        DisplayMediaItem displayItem = new DisplayMediaItem();
        displayItem.Id = item.Id;
        displayItem.Title = item.Title;
        displayItem.Cover = item.Cover;
        displayItem.Collecting = Boolean.TRUE.equals(item.Collecting);
        displayItem.ToCollect = !Boolean.TRUE.equals(item.CurrentOwnAny);

        if(item instanceof Book)
        {
            displayItem.MediaType = Enums.MediaType.Book;
        }
        else if(item instanceof Video)
        {
            displayItem.MediaType = Enums.MediaType.Video;
        }
        else if(item instanceof Music)
        {
            displayItem.MediaType = Enums.MediaType.Music;
        }
        else if(item instanceof Other)
        {
            displayItem.MediaType = Enums.MediaType.Other;
        }
        else {
            displayItem.MediaType = Enums.MediaType.None;
        }


        return displayItem;
    }

    public <T extends MediaItem> MediaItem mapToMediaItem(T item) {
        return item;
    }

    public List<Tag> GetTags() {
        return sharedRepository.getValue().GetTags();
    }

    public boolean AddTag(Tag tag) {
        return sharedRepository.getValue().AddTag(tag);
    }

    public boolean UpdateTag(Tag tag) {
        return sharedRepository.getValue().UpdateTag(tag);
    }

    public boolean DeleteTag(int id) {
        return sharedRepository.getValue().DeleteTag(id);
    }

    public String GetSeperatedString(String inputString)
    {
        return inputString.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z\\s])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z\\s])"
                ),
                " "
        );
    }

    public <T> T FindMostCommon(List<T> list) {
            if (list == null || list.isEmpty()) {
                return null;
            }

            Map<T, Integer> frequencyMap = new HashMap<>();
            T mostCommon = null;
            int maxCount = 0;

            for (T element : list) {
                int count = frequencyMap.getOrDefault(element, 0) + 1;
                frequencyMap.put(element, count);

                if (count > maxCount) {
                    maxCount = count;
                    mostCommon = element;
                }
            }

            return mostCommon;
        }

    public Map<Integer,String> GetGenres() {
        Enums.Genre[] genres = Enums.Genre.values();
        Map<Integer,String> genreMap = new HashMap<>();
        for (Enums.Genre genre : genres) {
            genreMap.put(genre.ordinal(), GetSeperatedString(genre.toString()));
        }
        return genreMap;
    }

    public <T extends Filter> String BuildWhereClause(T filter, List<String> selectionArgs) {
        if (filter == null) {
            return "";
        }

        List<String> conditions = new ArrayList<>();

        // region common filters

        if (filter.Collecting != null) {
            conditions.add(COLUMN_COLLECTING + " = ?");
            selectionArgs.add(filter.Collecting ? "1" : "0");
        }

        if (filter.CompletedCollecting != null) {
            conditions.add(COLUMN_COMPLETED_COLLECTING + " = ?");
            selectionArgs.add(filter.CompletedCollecting ? "1" : "0");
        }

        if (filter.Tag > 0) {
            conditions.add(COLUMN_TAG + " = ?");
            selectionArgs.add(String.valueOf(filter.Tag));
        }

        if (filter.Search != null && !filter.Search.isEmpty()) {
            if (filter.MediaType == Enums.MediaType.Book) {
                conditions.add("(" + COLUMN_TITLE + " LIKE ? OR " + COLUMN_AUTHOR + " LIKE ? OR " + COLUMN_ARTIST + " LIKE ?)");
                selectionArgs.add("%" + filter.Search + "%");
                selectionArgs.add("%" + filter.Search + "%");
                selectionArgs.add("%" + filter.Search + "%");
            } else if (filter.MediaType == Enums.MediaType.Music) {
                conditions.add("(" + COLUMN_TITLE + " LIKE ? OR " + COLUMN_ARTIST + " LIKE ?)");
                selectionArgs.add("%" + filter.Search + "%");
                selectionArgs.add("%" + filter.Search + "%");
            } else {
                conditions.add(COLUMN_TITLE + " LIKE ?");
                selectionArgs.add("%" + filter.Search + "%");
            }
        }

        if (filter.Genre > 0) {
            conditions.add(COLUMN_GENRE + " LIKE ?");
            selectionArgs.add("%" + filter.Genre + "%");
        }



        //endregion

        //region Books

        if(filter instanceof BookFilter) {
            var bFilter = (BookFilter) filter;
            if(bFilter.Type != null && bFilter.Type != com.example.medialibrary.backend.models.book.Enums.BookType.NoneSelected)
            {
                conditions.add(COLUMN_TYPE + " = ?");
                selectionArgs.add(String.valueOf(bFilter.Type.ordinal()));
            }
            if(bFilter.Publisher > 0)
            {
                conditions.add(COLUMN_PUBLISHER + " = ?");
                selectionArgs.add(String.valueOf(bFilter.Publisher));
            }

        }

        //endregion

        //region video
        if(filter instanceof VideoFilter) {
            var vFilter = (VideoFilter) filter;
            if (vFilter.Type != null && vFilter.Type != com.example.medialibrary.backend.models.video.Enums.VideoType.NoneSelected) {
                conditions.add(COLUMN_TYPE + " = ?");
                selectionArgs.add(String.valueOf(vFilter.Type.ordinal()));
            }

            if (vFilter.VideoTag != null && vFilter.VideoTag != com.example.medialibrary.backend.models.video.Enums.VideoTag.None) {
                conditions.add(COLUMN_VIDEO_TAG + " = ?");
                selectionArgs.add(String.valueOf(vFilter.VideoTag.ordinal()));
            }
        }

        //endregion

        return conditions.isEmpty() ? null : String.join(" AND ", conditions);
    }


}
