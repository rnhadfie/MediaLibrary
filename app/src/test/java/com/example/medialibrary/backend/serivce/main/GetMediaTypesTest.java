package com.example.medialibrary.backend.serivce.main;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import serivce.MainService;
import serivce.SharedService;
import models.shared.Enums.MediaType;
import java.util.Map;
import org.junit.Test;

import kotlin.LazyKt;

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
