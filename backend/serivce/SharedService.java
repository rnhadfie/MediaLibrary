package serivce;

import static utils.DatabaseKeyNames.*;
import static utils.DyanmicSort.buildDynamicComparator;
import static java.util.stream.Collectors.toList;

import models.book.Book;
import models.book.BookFilter;
import models.music.Music;
import models.music.MusicFilter;
import models.other.Other;
import models.shared.DeleteConfirmationResult;
import models.shared.DisplayMediaItem;
import models.shared.Enums;
import models.shared.Filter;
import models.shared.GenreObject;
import models.shared.MediaItem;
import models.shared.Tag;
import models.video.Video;
import models.video.VideoFilter;
import repository.SharedRepository;
import repository.database.MediaLibraryDbHelper;
import utils.DyanmicSort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kotlin.Lazy;
import kotlin.LazyKt;

public class SharedService {

    public Lazy<SharedRepository> sharedRepository;

    public SharedService(MediaLibraryDbHelper dbHelper) {
        this.sharedRepository = LazyKt.lazy(() -> new SharedRepository(dbHelper));
    }

    public SharedService(SharedRepository sharedRepository) {
        this.sharedRepository = LazyKt.lazy(() -> sharedRepository);
    }

    public <T extends MediaItem> List<MediaItem> mapToMediaItems(List<T> items) {
        if (items == null) return new ArrayList<>();
        return items.stream()
                .map(this::mapToMediaItem)
                .collect(toList());
    }

    public <T extends MediaItem> List<DisplayMediaItem> mapToDisplayItems(List<T> items, boolean showType) {
        if (items == null) return new ArrayList<>();
        return items.stream().map(item -> mapToDisplayItem(item, showType)).collect(toList());
    }

    public <T extends MediaItem> DisplayMediaItem mapToDisplayItem(T item, boolean showType) {
        DisplayMediaItem displayItem = new DisplayMediaItem();
        displayItem.Id = item.Id;
        displayItem.Title = item.Title;
        displayItem.Cover = item.Cover;
        displayItem.CollectingPriority = item.CollectingPriority;

        StringBuilder statusString = new StringBuilder();
        if(item.HasCollectedAllItems != null && item.HasCollectedAllItems)
        {
            statusString.append("Collected");
        }
        else if(item.CurrentOwnAny != null && !item.CurrentOwnAny)
        {
            statusString.append("To Collect");
        }
        else if(item.Collecting != null && item.Collecting)
        {
            statusString.append("Collecting");
        }
        else
        {
            statusString.append("Not Collecting");
        }


        Boolean standalone = null;
        var alternativeStatus = "";
        if (item instanceof Book) {
            var tempBook = (Book) item;
            displayItem.MediaType = Enums.MediaType.Book;
            displayItem.MediaTypeText = "Type: Book";
            alternativeStatus = "Type: " + (tempBook.Type != null ? GetSeperatedString(tempBook.Type.name()) : "");

            displayItem.ItemCount = Math.toIntExact(tempBook.Items != null ? tempBook.Items.stream().filter(videoItem -> videoItem.Owned).count() : 0);
            if(displayItem.ItemCount > 0)
            {
                standalone = Objects.equals(tempBook.Items.get(0).VolumeNumber, "-1");
            }
        } else if (item instanceof Video) {
            var tempVideo = (Video) item;
            displayItem.MediaType = Enums.MediaType.Video;
            displayItem.MediaTypeText = "Type: Movie/TV Show";
            displayItem.ItemCount = Math.toIntExact(tempVideo.Items != null ? tempVideo.Items.stream().filter(videoItem -> videoItem.Owned).count() : 0);
            alternativeStatus = "Category: " + (tempVideo.VideoTag != null ? GetSeperatedString(tempVideo.VideoTag.name()) : "");
            if(displayItem.ItemCount > 0)
            {
                standalone = Objects.equals(tempVideo.Items.get(0).Season, -1);
            }
        } else if (item instanceof Music) {
            displayItem.MediaType = Enums.MediaType.Music;
            displayItem.MediaTypeText = "Type: CD";
            displayItem.ItemCount = (item.HasCollectedAllItems != null && item.HasCollectedAllItems) ? 1 : 0;
            alternativeStatus =  ((Music)item).Artist;

        } else if (item instanceof Other) {
            var tempOther = (Other) item;
            displayItem.MediaType = Enums.MediaType.Other;
            displayItem.MediaTypeText = "Type: Other Collection";
            displayItem.ItemCount = Math.toIntExact(tempOther.Items != null ? tempOther.Items.stream().filter(videoItem -> videoItem.Owned).count() : 0);
        } else {
            displayItem.MediaType = Enums.MediaType.NoneSelected;
        }
        if(!showType) {
            displayItem.MediaTypeText = alternativeStatus;
        }

        if(standalone != null)
        {
            statusString.append(" | ").append(standalone ? "Standalone" : "Series");
        }
        displayItem.Status = statusString.toString();

        return displayItem;
    }

    public <T extends MediaItem> MediaItem mapToMediaItem(T item) {
        return item;
    }

    public List<Tag> GetTags() {
        var tags = sharedRepository.getValue().GetTags();
        tags.sort(Comparator.comparing(o -> o.Name));
        return tags;
    }

    public boolean AddTag(Tag tag) {
        return sharedRepository.getValue().AddTag(tag);
    }

    public boolean UpdateTag(Tag tag) {
        return sharedRepository.getValue().UpdateTag(tag);
    }

    public DeleteConfirmationResult DeleteTag(String id, boolean forceDelete) {
        DeleteConfirmationResult result = new DeleteConfirmationResult();
        if(!forceDelete) {
        if(sharedRepository.getValue().TagIsBeingUsed(id))
        {
            result.DeleteSuccessful = false;
            result.ConflictDetected = true;
            return result;
        }
        }
        result.DeleteSuccessful = sharedRepository.getValue().DeleteTag(id, forceDelete);
        result.ConflictDetected = false;
        return result;
    }

    public String GetSeperatedString(String inputString) {
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
            var frequency = frequencyMap.getOrDefault(element, 0);

            int count = frequency != null ? frequency + 1 : 0;
            frequencyMap.put(element, count);

            if (count > maxCount) {
                maxCount = count;
                mostCommon = element;
            }
        }

        return mostCommon;
    }

    public List<GenreObject> GetGenres() {
        Enums.Genre[] genres = Enums.Genre.values();
        List<GenreObject> genreMap = new ArrayList<>();
        for (Enums.Genre genre : genres) {
            if(genre != Enums.Genre.NoneSelected) {
                genreMap.add(new GenreObject(genre.ordinal(), GetSeperatedString(genre.toString())));
            }
        }
        return genreMap.stream().sorted(Comparator.comparing(g -> g.genreName)).collect(toList());
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

        if (filter.Collecting != null) {
            conditions.add(COLUMN_COLLECTING + " = ?");
            selectionArgs.add(filter.Collecting ? "1" : "0");
        }

        if (filter.Collected != null) {
            conditions.add(COLUMN_COLLECTED + " = ?");
            selectionArgs.add(filter.Collected ? "1" : "0");
        }

        if (filter.Ongoing != null) {
            conditions.add(COLUMN_ONGOING + " = ?");
            selectionArgs.add(filter.Ongoing ? "1" : "0");
        }

        // Tags
        List<String> incTags = new ArrayList<>();
        if (filter.IncludedTags != null && !filter.IncludedTags.isEmpty()) {
            for (String tagId : filter.IncludedTags) {
                incTags.add(String.valueOf(tagId));
            }
        } else if (filter.Tag > 0) {
            incTags.add(String.valueOf(filter.Tag));
        }
        appendInClause(conditions, selectionArgs, COLUMN_TAG, incTags);

        List<String> excTags = new ArrayList<>();
        if (filter.ExcludedTags != null && !filter.ExcludedTags.isEmpty()) {
            for (String tagId : filter.ExcludedTags) {
                excTags.add(String.valueOf(tagId));
            }
        }
        appendNotInClause(conditions, selectionArgs, COLUMN_TAG, excTags);

        // Search
        if (filter.Search != null && !filter.Search.isEmpty()) {
            var searchText = filter.Search.trim();
            if (filter instanceof BookFilter) {
                conditions.add("(" + COLUMN_TITLE + " LIKE ? OR " + COLUMN_AUTHOR + " LIKE ? OR " + COLUMN_ARTIST + " LIKE ?)");
                selectionArgs.add("%" + searchText + "%");
                selectionArgs.add("%" + searchText + "%");
                selectionArgs.add("%" + searchText + "%");
            } else if (filter instanceof MusicFilter) {
                conditions.add("(" + COLUMN_TITLE + " LIKE ? OR " + COLUMN_ARTIST + " LIKE ?)");
                selectionArgs.add("%" + searchText + "%");
                selectionArgs.add("%" + searchText + "%");
            } else {
                conditions.add(COLUMN_TITLE + " LIKE ?");
                selectionArgs.add("%" + searchText + "%");
            }
        }

        // Genres
        List<String> incGenres = new ArrayList<>();
        if (filter.IncludedGenres != null && !filter.IncludedGenres.isEmpty()) {
            for (int gId : filter.IncludedGenres) {
                incGenres.add(String.valueOf(gId));
            }
        } else if (filter.Genre > 0) {
            incGenres.add(String.valueOf(filter.Genre));
        }

        List<String> excGenres = new ArrayList<>();
        if (filter.ExcludedGenres != null && !filter.ExcludedGenres.isEmpty()) {
            for (int gId : filter.ExcludedGenres) {
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
                    if (t != models.book.Enums.BookType.NoneSelected) {
                        incTypes.add(String.valueOf(t.ordinal()));
                    }
                }
            } else if (bFilter.Type != null && bFilter.Type != models.book.Enums.BookType.NoneSelected) {
                incTypes.add(String.valueOf(bFilter.Type.ordinal()));
            }
            appendInClause(conditions, selectionArgs, COLUMN_TYPE, incTypes);

            List<String> excTypes = new ArrayList<>();
            if (bFilter.ExcludedTypes != null && !bFilter.ExcludedTypes.isEmpty()) {
                for (var t : bFilter.ExcludedTypes) {
                    if (t != models.book.Enums.BookType.NoneSelected) {
                        excTypes.add(String.valueOf(t.ordinal()));
                    }
                }
            }
            appendNotInClause(conditions, selectionArgs, COLUMN_TYPE, excTypes);

            //Publishers
            List<String> incPubs = new ArrayList<>();
            if (bFilter.IncludedPublishers != null && !bFilter.IncludedPublishers.isEmpty()) {
                for (String pId : bFilter.IncludedPublishers) {
                    incPubs.add(String.valueOf(pId));
                }
            } else if (bFilter.Publisher > 0) {
                incPubs.add(String.valueOf(bFilter.Publisher));
            }
            appendInClause(conditions, selectionArgs, COLUMN_PUBLISHER, incPubs);

            List<String> excPubs = new ArrayList<>();
            if (bFilter.ExcludedPublishers != null && !bFilter.ExcludedPublishers.isEmpty()) {
                for (String pId : bFilter.ExcludedPublishers) {
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
                    if (t != models.video.Enums.VideoType.NoneSelected) {
                        incTypes.add(String.valueOf(t.ordinal()));
                    }
                }
            } else if (vFilter.Type != null && vFilter.Type != models.video.Enums.VideoType.NoneSelected) {
                incTypes.add(String.valueOf(vFilter.Type.ordinal()));
            }
            appendInClause(conditions, selectionArgs, COLUMN_TYPE, incTypes);

            List<String> excTypes = new ArrayList<>();
            if (vFilter.ExcludedTypes != null && !vFilter.ExcludedTypes.isEmpty()) {
                for (var t : vFilter.ExcludedTypes) {
                    if (t != models.video.Enums.VideoType.NoneSelected) {
                        excTypes.add(String.valueOf(t.ordinal()));
                    }
                }
            }
            appendNotInClause(conditions, selectionArgs, COLUMN_TYPE, excTypes);

            List<String> incVideoTags = new ArrayList<>();
            if (vFilter.IncludedVideoTags != null && !vFilter.IncludedVideoTags.isEmpty()) {
                for (var vt : vFilter.IncludedVideoTags) {
                    if (vt != models.video.Enums.VideoTag.None) {
                        incVideoTags.add(String.valueOf(vt.ordinal()));
                    }
                }
            } else if (vFilter.VideoTag != null && vFilter.VideoTag != models.video.Enums.VideoTag.None) {
                incVideoTags.add(String.valueOf(vFilter.VideoTag.ordinal()));
            }
            appendInClause(conditions, selectionArgs, COLUMN_VIDEO_TAG, incVideoTags);

            List<String> excVideoTags = new ArrayList<>();
            if (vFilter.ExcludedVideoTags != null && !vFilter.ExcludedVideoTags.isEmpty()) {
                for (var vt : vFilter.ExcludedVideoTags) {
                    if (vt != models.video.Enums.VideoTag.None) {
                        excVideoTags.add(String.valueOf(vt.ordinal()));
                    }
                }
            }
            appendNotInClause(conditions, selectionArgs, COLUMN_VIDEO_TAG, excVideoTags);
        }

        //endregion

        return conditions.isEmpty() ? null : String.join(" AND ", conditions);
    }

    public <T extends Filter, U extends MediaItem> List<U> Sort(T filter, List<U> items) {
        if (filter == null || items == null || items.isEmpty()) {
            return items != null ? items : new ArrayList<>();
        }

        List<U> sortedItems = new ArrayList<>(items);
        List<DyanmicSort.SortKey<U, ?>> sortRules = new ArrayList<>();

        List<String> order = (filter.SortOrder != null && !filter.SortOrder.isEmpty())
                ? filter.SortOrder
                : List.of("Priority", "Alphabetical", "ItemMediaType");

        for (String key : order) {
            if ("Priority".equalsIgnoreCase(key) && filter.SortPriority != null) {
                sortRules.add(new DyanmicSort.SortKey<>(u -> u.CollectingPriority != null ? u.CollectingPriority.ordinal() : 0, !filter.SortPriority));
            } else if ("Alphabetical".equalsIgnoreCase(key) && filter.SortAlphabetical != null) {
                sortRules.add(new DyanmicSort.SortKey<>(u -> u.Title != null ? u.Title : "", filter.SortAlphabetical));
            } else if ("ItemMediaType".equalsIgnoreCase(key) && filter.SortItemMediaType != null) {
                sortRules.add(new DyanmicSort.SortKey<>(u -> u.MediaType != null ? u.MediaType.name() : "", filter.SortItemMediaType));
            }
        }

        if (filter.SortPriority != null && !order.contains("Priority")) {
            sortRules.add(new DyanmicSort.SortKey<>(u -> u.CollectingPriority != null ? u.CollectingPriority.ordinal() : 0, !filter.SortPriority));
        }
        if (filter.SortAlphabetical != null && !order.contains("Alphabetical")) {
            sortRules.add(new DyanmicSort.SortKey<>(u -> u.Title != null ? u.Title : "", filter.SortAlphabetical));
        }
        if (filter.SortItemMediaType != null && !order.contains("ItemMediaType")) {
            sortRules.add(new DyanmicSort.SortKey<>(u -> u.MediaType != null ? u.MediaType.name() : "", filter.SortItemMediaType));
        }

        if (!sortRules.isEmpty()) {
            sortedItems.sort(buildDynamicComparator(sortRules));
        }
        return sortedItems;
    }

    public List<DisplayMediaItem> SortDisplayItem(Filter filter, List<DisplayMediaItem> items) {
        if (filter == null || items == null || items.isEmpty()) {
            return items != null ? items : new ArrayList<>();
        }

        List<DisplayMediaItem> sortedItems = new ArrayList<>(items);
        List<DyanmicSort.SortKey<DisplayMediaItem, ?>> sortRules = new ArrayList<>();

        List<String> order = (filter.SortOrder != null && !filter.SortOrder.isEmpty())
                ? filter.SortOrder
                : List.of("Priority", "Alphabetical", "ItemMediaType");

        for (String key : order) {
            if ("Priority".equalsIgnoreCase(key) && filter.SortPriority != null) {
                sortRules.add(new DyanmicSort.SortKey<>(u -> u.CollectingPriority != null ? u.CollectingPriority.ordinal() : 0, !filter.SortPriority));
            } else if ("Alphabetical".equalsIgnoreCase(key) && filter.SortAlphabetical != null) {
                sortRules.add(new DyanmicSort.SortKey<>(u -> u.Title != null ? u.Title : "", filter.SortAlphabetical));
            } else if ("ItemMediaType".equalsIgnoreCase(key) && filter.SortItemMediaType != null) {
                sortRules.add(new DyanmicSort.SortKey<>(u -> u.MediaType != null ? u.MediaType.name() : "", filter.SortItemMediaType));
            }
        }

        if (filter.SortPriority != null && !order.contains("Priority")) {
            sortRules.add(new DyanmicSort.SortKey<>(u -> u.CollectingPriority != null ? u.CollectingPriority.ordinal() : 0, !filter.SortPriority));
        }
        if (filter.SortAlphabetical != null && !order.contains("Alphabetical")) {
            sortRules.add(new DyanmicSort.SortKey<>(u -> u.Title != null ? u.Title : "", filter.SortAlphabetical));
        }
        if (filter.SortItemMediaType != null && !order.contains("ItemMediaType")) {
            sortRules.add(new DyanmicSort.SortKey<>(u -> u.MediaType != null ? u.MediaType.name() : "", filter.SortItemMediaType));
        }

        if (!sortRules.isEmpty()) {
            sortedItems.sort(buildDynamicComparator(sortRules));
        }
        return sortedItems;
    }
}
