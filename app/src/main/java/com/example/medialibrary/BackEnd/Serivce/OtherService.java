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
    private final MediaLibraryDbHelper dbHelper;

    public Lazy<OtherRepository> otherRepository;
    public Lazy<SharedService> sharedService;

    public OtherService(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.otherRepository = LazyKt.lazy(() -> new OtherRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));
    }

    public List<Other> GetBooks(Filter filter) {
        var repo = this.otherRepository.getValue();
        List<Other> books = repo.GetOtherCollections(filter);
        return OtherItemBasedFilters(books, filter);
    }

    public List<DisplayMediaItem> GetBookDisplayLists(Filter filter) {
        var repo = this.otherRepository.getValue();
        var books =repo.GetOtherCollections(filter);
        var sharedService = this.sharedService.getValue();

        return sharedService.mapToDisplayItems(OtherItemBasedFilters(books, filter));
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


    public List<Other> OtherItemBasedFilters(List<Other> listOfOtherCollections, Filter fitler)
    {
        List<Other> filteredList = new ArrayList<Other>();
        if(listOfOtherCollections.stream().count() > 0) {
            for (Other otherCollection : listOfOtherCollections) {
                if(otherCollection.Items != null) {
                    otherCollection.CurrentOwnAny = otherCollection.Items.stream().count() <= 0 || otherCollection.Items.stream().anyMatch(x -> x.Owned);
                }
                else {
                    otherCollection.CurrentOwnAny = false;
                }
                filteredList.add(otherCollection);
            }

            if (fitler != null) {
                if (fitler.AnyOwned != null) {
                    filteredList = filteredList.stream().filter(book -> book.CurrentOwnAny == fitler.AnyOwned).collect(Collectors.toList());
                }
            }
        }

        return filteredList;
    }

}
