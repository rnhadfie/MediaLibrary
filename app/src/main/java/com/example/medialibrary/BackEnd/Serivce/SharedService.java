package com.example.medialibrary.backend.Serivce;

import static java.util.stream.Collectors.toList;

import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.models.shared.MediaItem;
import com.example.medialibrary.backend.models.shared.Tag;
import com.example.medialibrary.backend.models.video.Video;
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
        return (MediaItem) item;
    }

    public List<Tag> GetTags() {
        return sharedRepository.getValue().GetTags();
    }

    public Tag GetTag(int id) {
        return sharedRepository.getValue().GetTag(id);
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
        String result = inputString.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
        return result;
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
    };


}
