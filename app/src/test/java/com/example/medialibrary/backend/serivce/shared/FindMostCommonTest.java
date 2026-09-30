package com.example.medialibrary.backend.serivce.shared;

import static org.junit.Assert.*;

import serivce.SharedService;
import repository.database.MediaLibraryDbHelper;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FindMostCommonTest {
    @Test
    public void testFindMostCommon_Integers() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        List<Integer> list = Arrays.asList(1, 2, 2, 3, 3, 3);
        Integer result = service.FindMostCommon(list);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    public void testFindMostCommon_EmptyList() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        List<Integer> list = List.of();
        Integer result = service.FindMostCommon(list);
        assertNull(result);
    }
}
