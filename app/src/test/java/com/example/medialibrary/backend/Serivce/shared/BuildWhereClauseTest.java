package com.example.medialibrary.backend.Serivce.shared;

import static org.junit.Assert.*;

import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.shared.Filter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class BuildWhereClauseTest {
    @Test
    public void testBuildWhereClause_CollectingTrue() {
        SharedService service = new SharedService(null);
        Filter filter = new Filter();
        filter.Collecting = true;
        List<String> selectionArgs = new ArrayList<>();
        
        String result = service.BuildWhereClause(filter, selectionArgs);
        
        assertTrue(result.contains("collecting = ?"));
        assertEquals(1, selectionArgs.size());
        assertEquals("1", selectionArgs.get(0));
    }

    @Test
    public void testBuildWhereClause_Search() {
        SharedService service = new SharedService(null);
        Filter filter = new Filter();
        filter.Search = "Harry Potter";
        List<String> selectionArgs = new ArrayList<>();
        
        String result = service.BuildWhereClause(filter, selectionArgs);
        
        assertTrue(result.contains("Title LIKE ?"));
        assertEquals(1, selectionArgs.size());
        assertEquals("%Harry Potter%", selectionArgs.get(0));
    }
}
