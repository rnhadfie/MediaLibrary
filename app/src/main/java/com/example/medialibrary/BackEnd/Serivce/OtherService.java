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

    public OtherService(MediaLibraryDbHelper dbHelper) {
        this.otherRepository = LazyKt.lazy(() -> new OtherRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));
    }

    public List<Other> GetOtherCollections(Filter filter) {
        var repo = this.otherRepository.getValue();
        var sharedService = this.sharedService.getValue();

        List<String> selectionArgs = new ArrayList<>();
        String whereClause = sharedService.BuildWhereClause(filter, selectionArgs);

        var otherCollections =repo.GetOtherCollections(whereClause, selectionArgs);
        return OtherItemBasedFilters(otherCollections, filter);
    }

    public List<DisplayMediaItem> GetOtherDisplayLists(Filter filter) {
        var repo = this.otherRepository.getValue();
        var sharedService = this.sharedService.getValue();
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = sharedService.BuildWhereClause(filter, selectionArgs);

        var otherCollections =repo.GetOtherCollections(whereClause, selectionArgs);


        return sharedService.mapToDisplayItems(OtherItemBasedFilters(otherCollections, filter), false);
    }


    public Other GetOtherCollection(int id) {
        var repo = this.otherRepository.getValue();
        return repo.GetOtherCollection(id);

    }

    public boolean AddOtherCollection(OtherSaveObj other) {
        var repo = this.otherRepository.getValue();
        return repo.AddOtherCollection(other);
    }

    public boolean EditOtherCollection(OtherSaveObj other) {
        var repo = this.otherRepository.getValue();
        return repo.UpdateOtherCollection(other);
    }

    public boolean DeleteOtherCollection(int id) {
        var repo = this.otherRepository.getValue();
        return repo.DeleteOtherCollection(id);
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
