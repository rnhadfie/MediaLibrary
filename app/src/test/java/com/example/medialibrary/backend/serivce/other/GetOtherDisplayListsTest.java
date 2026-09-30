package com.example.medialibrary.backend.serivce.other;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import serivce.OtherService;
import serivce.SharedService;
import models.book.BookItem;
import models.other.Other;
import models.other.OtherFilter;
import models.shared.DisplayMediaItem;
import repository.OtherRepository;
import repository.database.MediaLibraryDbHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetOtherDisplayListsTest {
    @Mock
    private OtherRepository mockOtherRepository;

    private SharedService sharedService;
    private OtherService otherService;

    @BeforeEach
    public void setUp() {
        sharedService = new SharedService((MediaLibraryDbHelper) null);
        otherService = new OtherService(mockOtherRepository, sharedService);
    }

    @Test
    public void GetBookDisplayLists_emptyList_returnsEmptyList() {
        List<Other> otherItems = new ArrayList<>();
        OtherFilter filter = new OtherFilter();
        filter.Collecting = true;

        when(mockOtherRepository.GetOtherCollections(any(), any())).thenReturn(otherItems);

        List<DisplayMediaItem> result = otherService.GetOtherDisplayLists(filter);
        assertTrue(result.isEmpty());
    }

    @Test
    public void GetBookDisplayLists_filterIsActive_returnsFilteredList() {
        List<Other> otherItems = new ArrayList<>();
        OtherFilter filter = new OtherFilter();
        filter.Collected = true;

        Other otherItem1 = new Other();
        otherItem1.Id = "1";
        otherItem1.Title = "Test Book";
        BookItem item1 = new BookItem();
        item1.Owned = true;

        otherItems.add(otherItem1);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());

        when(mockOtherRepository.GetOtherCollections(eq(whereClause), any())).thenReturn(otherItems);

        List<DisplayMediaItem> result = otherService.GetOtherDisplayLists(filter);

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).Id);
    }

    @Test
    public void GetBookDisplayLists_sortIsApplied_returnsSortedList() {
        List<Other> otherItems = new ArrayList<>();
        OtherFilter filter = new OtherFilter();
        filter.Collected = true;

        Other otherItem1 = new Other();
        otherItem1.Id = "1";
        otherItem1.Title = "Test Book";
        BookItem item1 = new BookItem();
        item1.Owned = true;

        Other otherItem2 = new Other();
        otherItem2.Id = "2";
        otherItem2.Title = "Filtered Book";
        otherItems.add(otherItem1);
        otherItems.add(otherItem2);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());

        when(mockOtherRepository.GetOtherCollections(eq(whereClause), any())).thenReturn(otherItems);

        List<DisplayMediaItem> result = otherService.GetOtherDisplayLists(filter);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).Id);
    }
}
