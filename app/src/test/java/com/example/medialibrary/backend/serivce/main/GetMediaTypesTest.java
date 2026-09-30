package com.example.medialibrary.backend.serivce.main;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.Map;

import kotlin.LazyKt;
import models.shared.Enums.MediaType;
import serivce.MainService;
import serivce.SharedService;

public class GetMediaTypesTest {
    @Test
    public void testMainGetMediaTypes() {
        SharedService sharedService = mock(SharedService.class);
        when(sharedService.GetSeperatedString(anyString())).thenAnswer(i -> i.getArguments()[0]);
        
        MainService service = new MainService(null);
        service.sharedService = LazyKt.lazy(() -> sharedService);
        
        Map<Integer, String> types = service.GetMediaTypes();
        assertNotNull(types);
        assertTrue(types.containsKey(MediaType.Book.ordinal()));
    }
}
