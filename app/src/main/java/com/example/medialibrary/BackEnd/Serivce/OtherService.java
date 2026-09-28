package com.example.medialibrary.backend.Serivce;

import com.example.medialibrary.backend.models.other.*;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.repository.OtherRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.Lazy;
import kotlin.LazyKt;

public class OtherService {

    public Lazy<OtherRepository> otherRepository;
    public Lazy<SharedService> sharedService;

    private final OtherRepository _OtherRepo;
    private final SharedService _SharedService;

    public OtherService(MediaLibraryDbHelper dbHelper) {
        this.otherRepository = LazyKt.lazy(() -> new OtherRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));

        _OtherRepo = otherRepository.getValue();
        _SharedService = sharedService.getValue();
    }

    public List<Other> GetOtherCollections(Filter filter) {
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = _SharedService.BuildWhereClause(filter, selectionArgs);

        var otherCollections = _OtherRepo.GetOtherCollections(whereClause, selectionArgs);
        otherCollections = _SharedService.Sort(filter, otherCollections);

        return OtherItemBasedFilters(otherCollections, filter);
    }

    public List<DisplayMediaItem> GetOtherDisplayLists(Filter filter) {

        List<String> selectionArgs = new ArrayList<>();
        String whereClause = _SharedService.BuildWhereClause(filter, selectionArgs);

        var otherCollections = _OtherRepo.GetOtherCollections(whereClause, selectionArgs);
        otherCollections = _SharedService.Sort(filter, otherCollections);

        return _SharedService.mapToDisplayItems(OtherItemBasedFilters(otherCollections, filter), false);
    }


    public Other GetOtherCollection(String id) {
        return _OtherRepo.GetOtherCollection(id);
    }

    public boolean AddOtherCollection(OtherSaveObj other) {
        return _OtherRepo.AddOtherCollection(other);
    }

    public boolean EditOtherCollection(OtherSaveObj other) {
        return _OtherRepo.UpdateOtherCollection(other);
    }

    public boolean DeleteOtherCollection(String id) {
        return _OtherRepo.DeleteOtherCollection(id);
    }

    public List<Other> OtherItemBasedFilters(List<Other> listOfOtherCollections, Filter filter)
    {
        List<Other> filteredList = new ArrayList<>();
        if((long) listOfOtherCollections.size() > 0) {
            for (Other otherCollection : listOfOtherCollections) {
                if(otherCollection.Items != null) {
                    otherCollection.CurrentOwnAny = (long) otherCollection.Items.size() <= 0 || otherCollection.Items.stream().anyMatch(x -> x.Owned);
                }
                else {
                    otherCollection.CurrentOwnAny = false;
                }
                filteredList.add(otherCollection);
            }

            if (filter != null) {
                if (filter.AnyOwned != null) {
                    filteredList = filteredList.stream().filter(book -> book.CurrentOwnAny == filter.AnyOwned).collect(Collectors.toList());
                }
            }
        }

        return filteredList;
    }

}
