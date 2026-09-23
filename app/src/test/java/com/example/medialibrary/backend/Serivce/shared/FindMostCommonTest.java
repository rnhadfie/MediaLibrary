package com.example.medialibrary.backend.Serivce.shared;

import static org.junit.Assert.*;

import com.example.medialibrary.backend.Serivce.SharedService;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FindMostCommonTest {
    @Test
    public void testFindMostCommon_Integers() {
        SharedService service = new SharedService(null);
        List<Integer> list = Arrays.asList(1, 2, 2, 3, 3, 3);
        Integer result = service.FindMostCommon(list);
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    public void testFindMostCommon_EmptyList() {
        SharedService service = new SharedService(null);
        List<Integer> list = Arrays.asList();
        Integer result = service.FindMostCommon(list);
        assertNull(result);
    }
}
