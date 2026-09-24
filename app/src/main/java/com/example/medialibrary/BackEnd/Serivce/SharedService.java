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
import com.example.medialibrary.backend.models.music.MusicFilter;
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

    public Lazy<SharedRepository> sharedRepository;

    public SharedService(MediaLibraryDbHelper dbHelper) {
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
        displayItem.CollectedOrOnGoing = Boolean.TRUE.equals(item.HasCollectedAllItems);


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

    private void appendInClause(List<String> conditions, List<String> selectionArgs, String columnName, List<String> values) {
        if (values != null && !values.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(columnName).append(" IN (");
            for (int i = 0; i < values.size(); i++) {
                sb.append(i == 0 ? "?" : ", ?");
                selectionArgs.add(values.get(i));
            }
            sb.append(")");
            conditions.add(sb.toString());
        }
    }

    private void appendNotInClause(List<String> conditions, List<String> selectionArgs, String columnName, List<String> values) {
        if (values != null && !values.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(columnName).append(" NOT IN (");
            for (int i = 0; i < values.size(); i++) {
                sb.append(i == 0 ? "?" : ", ?");
                selectionArgs.add(values.get(i));
            }
            sb.append(")");
            conditions.add(sb.toString());
        }
    }

    private void appendLikeOrClause(List<String> conditions, List<String> selectionArgs, String columnName, List<String> values) {
        if (values != null && !values.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) sb.append(" OR ");
                sb.append(columnName).append(" LIKE ?");
                selectionArgs.add("%" + values.get(i) + "%");
            }
            sb.append(")");
            conditions.add(sb.toString());
        }
    }

    private void appendNotLikeAndClause(List<String> conditions, List<String> selectionArgs, String columnName, List<String> values) {
        if (values != null && !values.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) sb.append(" AND ");
                sb.append(columnName).append(" NOT LIKE ?");
                selectionArgs.add("%" + values.get(i) + "%");
            }
            sb.append(")");
            conditions.add(sb.toString());
        }
    }

    public <T extends Filter> String BuildWhereClause(T filter, List<String> selectionArgs) {
        if (filter == null) {
            return "";
        }

        List<String> conditions = new ArrayList<>();

        // region common filters

        if (filter.Collecting != null && filter.Collecting) {
            conditions.add(COLUMN_COLLECTING + " = ?");
            selectionArgs.add("1");
        }

        if (filter.Collected != null && filter.Collected) {
            conditions.add(COLUMN_COMPLETED_COLLECTING + " = ?");
            selectionArgs.add("1");
        }

        // Tags
        List<String> incTags = new ArrayList<>();
        if (filter.IncludedTags != null && !filter.IncludedTags.isEmpty()) {
            for (Integer tagId : filter.IncludedTags) {
                incTags.add(String.valueOf(tagId));
            }
        } else if (filter.Tag > 0) {
            incTags.add(String.valueOf(filter.Tag));
        }
        appendInClause(conditions, selectionArgs, COLUMN_TAG, incTags);

        List<String> excTags = new ArrayList<>();
        if (filter.ExcludedTags != null && !filter.ExcludedTags.isEmpty()) {
            for (Integer tagId : filter.ExcludedTags) {
                excTags.add(String.valueOf(tagId));
            }
        }
        appendNotInClause(conditions, selectionArgs, COLUMN_TAG, excTags);

        // Search
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

        // Genres
        List<String> incGenres = new ArrayList<>();
        if (filter.IncludedGenres != null && !filter.IncludedGenres.isEmpty()) {
            for (Integer gId : filter.IncludedGenres) {
                incGenres.add(String.valueOf(gId));
            }
        } else if (filter.Genre > 0) {
            incGenres.add(String.valueOf(filter.Genre));
        }

        List<String> excGenres = new ArrayList<>();
        if (filter.ExcludedGenres != null && !filter.ExcludedGenres.isEmpty()) {
            for (Integer gId : filter.ExcludedGenres) {
                excGenres.add(String.valueOf(gId));
            }
        }

        if (filter instanceof MusicFilter) {
            var mFilter = (MusicFilter) filter;
            if (mFilter.IncludedMusicGenres != null && !mFilter.IncludedMusicGenres.isEmpty()) {
                for (Integer mgId : mFilter.IncludedMusicGenres) {
                    if (!incGenres.contains(String.valueOf(mgId))) incGenres.add(String.valueOf(mgId));
                }
            } else if (mFilter.MusicGenre > 0) {
                incGenres.add(String.valueOf(mFilter.MusicGenre));
            }
            if (mFilter.ExcludedMusicGenres != null && !mFilter.ExcludedMusicGenres.isEmpty()) {
                for (Integer mgId : mFilter.ExcludedMusicGenres) {
                    if (!excGenres.contains(String.valueOf(mgId))) excGenres.add(String.valueOf(mgId));
                }
            }
        }

        appendLikeOrClause(conditions, selectionArgs, COLUMN_GENRE, incGenres);
        appendNotLikeAndClause(conditions, selectionArgs, COLUMN_GENRE, excGenres);

        //endregion

        //region Books

        if (filter instanceof BookFilter) {
            var bFilter = (BookFilter) filter;

            // Book Types
            List<String> incTypes = new ArrayList<>();
            if (bFilter.IncludedTypes != null && !bFilter.IncludedTypes.isEmpty()) {
                for (var t : bFilter.IncludedTypes) {
                    if (t != com.example.medialibrary.backend.models.book.Enums.BookType.NoneSelected) {
                        incTypes.add(String.valueOf(t.ordinal()));
                    }
                }
            } else if (bFilter.Type != null && bFilter.Type != com.example.medialibrary.backend.models.book.Enums.BookType.NoneSelected) {
                incTypes.add(String.valueOf(bFilter.Type.ordinal()));
            }
            appendInClause(conditions, selectionArgs, COLUMN_TYPE, incTypes);

            List<String> excTypes = new ArrayList<>();
            if (bFilter.ExcludedTypes != null && !bFilter.ExcludedTypes.isEmpty()) {
                for (var t : bFilter.ExcludedTypes) {
                    if (t != com.example.medialibrary.backend.models.book.Enums.BookType.NoneSelected) {
                        excTypes.add(String.valueOf(t.ordinal()));
                    }
                }
            }
            appendNotInClause(conditions, selectionArgs, COLUMN_TYPE, excTypes);

            //Publishers
            List<String> incPubs = new ArrayList<>();
            if (bFilter.IncludedPublishers != null && !bFilter.IncludedPublishers.isEmpty()) {
                for (Integer pId : bFilter.IncludedPublishers) {
                    incPubs.add(String.valueOf(pId));
                }
            } else if (bFilter.Publisher > 0) {
                incPubs.add(String.valueOf(bFilter.Publisher));
            }
            appendInClause(conditions, selectionArgs, COLUMN_PUBLISHER, incPubs);

            List<String> excPubs = new ArrayList<>();
            if (bFilter.ExcludedPublishers != null && !bFilter.ExcludedPublishers.isEmpty()) {
                for (Integer pId : bFilter.ExcludedPublishers) {
                    excPubs.add(String.valueOf(pId));
                }
            }
            appendNotInClause(conditions, selectionArgs, COLUMN_PUBLISHER, excPubs);

        }

        //endregion

        //region video
        if (filter instanceof VideoFilter) {
            var vFilter = (VideoFilter) filter;

            List<String> incTypes = new ArrayList<>();
            if (vFilter.IncludedTypes != null && !vFilter.IncludedTypes.isEmpty()) {
                for (var t : vFilter.IncludedTypes) {
                    if (t != com.example.medialibrary.backend.models.video.Enums.VideoType.NoneSelected) {
                        incTypes.add(String.valueOf(t.ordinal()));
                    }
                }
            } else if (vFilter.Type != null && vFilter.Type != com.example.medialibrary.backend.models.video.Enums.VideoType.NoneSelected) {
                incTypes.add(String.valueOf(vFilter.Type.ordinal()));
            }
            appendInClause(conditions, selectionArgs, COLUMN_TYPE, incTypes);

            List<String> excTypes = new ArrayList<>();
            if (vFilter.ExcludedTypes != null && !vFilter.ExcludedTypes.isEmpty()) {
                for (var t : vFilter.ExcludedTypes) {
                    if (t != com.example.medialibrary.backend.models.video.Enums.VideoType.NoneSelected) {
                        excTypes.add(String.valueOf(t.ordinal()));
                    }
                }
            }
            appendNotInClause(conditions, selectionArgs, COLUMN_TYPE, excTypes);

            List<String> incVideoTags = new ArrayList<>();
            if (vFilter.IncludedVideoTags != null && !vFilter.IncludedVideoTags.isEmpty()) {
                for (var vt : vFilter.IncludedVideoTags) {
                    if (vt != com.example.medialibrary.backend.models.video.Enums.VideoTag.None) {
                        incVideoTags.add(String.valueOf(vt.ordinal()));
                    }
                }
            } else if (vFilter.VideoTag != null && vFilter.VideoTag != com.example.medialibrary.backend.models.video.Enums.VideoTag.None) {
                incVideoTags.add(String.valueOf(vFilter.VideoTag.ordinal()));
            }
            appendInClause(conditions, selectionArgs, COLUMN_VIDEO_TAG, incVideoTags);

            List<String> excVideoTags = new ArrayList<>();
            if (vFilter.ExcludedVideoTags != null && !vFilter.ExcludedVideoTags.isEmpty()) {
                for (var vt : vFilter.ExcludedVideoTags) {
                    if (vt != com.example.medialibrary.backend.models.video.Enums.VideoTag.None) {
                        excVideoTags.add(String.valueOf(vt.ordinal()));
                    }
                }
            }
            appendNotInClause(conditions, selectionArgs, COLUMN_VIDEO_TAG, excVideoTags);
        }

        //endregion

        return conditions.isEmpty() ? null : String.join(" AND ", conditions);
    }


}
