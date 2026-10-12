package com.example.medialibrary.backend.serivce.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import models.book.BookFilter;
import models.shared.Filter;
import repository.database.MediaLibraryDbHelper;
import serivce.SharedService;

public class BuildWhereClauseTest {
    @Test
    public void testBuildWhereClause_CollectingTrue() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        Filter filter = new Filter();
        filter.Collecting = true;
        List<String> selectionArgs = new ArrayList<>();
        
        String result = service.BuildWhereClause(filter, selectionArgs);
        
        assertTrue(result.toLowerCase().contains("collecting = ?"));
        assertEquals(1, selectionArgs.size());
        assertEquals("1", selectionArgs.get(0));
    }

    @Test
    public void testBuildWhereClause_Search() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        Filter filter = new Filter();
        filter.Search = "Harry Potter";
        List<String> selectionArgs = new ArrayList<>();
        
        String result = service.BuildWhereClause(filter, selectionArgs);
        
        assertTrue(result.contains("Title LIKE ?"));
        assertEquals(1, selectionArgs.size());
        assertEquals("%Harry Potter%", selectionArgs.get(0));
    }

    @Test
    public void testBuildWhereClause_MultipleIncludedAndExcludedTags() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        Filter filter = new Filter();
        filter.IncludedTags.add("1");
        filter.IncludedTags.add("3");
        filter.ExcludedTags.add("2");
        List<String> selectionArgs = new ArrayList<>();
        
        String result = service.BuildWhereClause(filter, selectionArgs);
        
        assertTrue(result.contains("IN (?, ?)"));
        assertTrue(result.contains("NOT IN (?)"));
        assertEquals(3, selectionArgs.size());
        assertEquals("1", selectionArgs.get(0));
        assertEquals("3", selectionArgs.get(1));
        assertEquals("2", selectionArgs.get(2));
    }

    @Test
    public void testBuildWhereClause_MultipleIncludedAndExcludedGenres() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        Filter filter = new Filter();
        filter.IncludedGenres.add(10);
        filter.IncludedGenres.add(20);
        filter.ExcludedGenres.add(30);
        List<String> selectionArgs = new ArrayList<>();
        
        String result = service.BuildWhereClause(filter, selectionArgs);
        
        assertTrue(result.contains("LIKE ? OR"));
        assertTrue(result.contains("NOT LIKE ?"));
        assertEquals(3, selectionArgs.size());
        assertEquals("%10%", selectionArgs.get(0));
        assertEquals("%20%", selectionArgs.get(1));
        assertEquals("%30%", selectionArgs.get(2));
    }

    @Test
    public void testBuildWhereClause_NoTagIncludedAndExcluded() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        Filter filter = new Filter();
        filter.IncludedTags.add("");
        List<String> selectionArgs = new ArrayList<>();

        String result = service.BuildWhereClause(filter, selectionArgs);

        assertTrue(result.contains("Tag IS NULL OR Tag = ''"));

        Filter filterEx = new Filter();
        filterEx.ExcludedTags.add("");
        List<String> selectionArgsEx = new ArrayList<>();

        String resultEx = service.BuildWhereClause(filterEx, selectionArgsEx);

        assertTrue(resultEx.contains("Tag IS NOT NULL AND Tag != ''"));
    }

    @Test
    public void testBuildWhereClause_NoPublisherIncludedAndExcluded() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        BookFilter filter = new BookFilter();
        filter.IncludedPublishers.add("");
        List<String> selectionArgs = new ArrayList<>();

        String result = service.BuildWhereClause(filter, selectionArgs);

        assertTrue(result.contains("Publisher IS NULL OR Publisher = ''"));
    }
}
