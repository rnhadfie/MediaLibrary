package com.example.medialibrary.backend.Serivce.shared;

import static org.junit.Assert.*;

import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

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
}
