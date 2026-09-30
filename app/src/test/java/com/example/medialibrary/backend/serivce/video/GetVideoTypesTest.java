package com.example.medialibrary.backend.serivce.video;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.Map;

import kotlin.LazyKt;
import models.video.Enums.VideoType;
import serivce.SharedService;
import serivce.VideoService;

public class GetVideoTypesTest {
    @Test
    public void testGetVideoTypes() {
        SharedService sharedService = mock(SharedService.class);
        when(sharedService.GetSeperatedString(anyString())).thenAnswer(i -> i.getArguments()[0]);
        
        VideoService service = new VideoService(null);
        service.sharedService = LazyKt.lazy(() -> sharedService);
        
        Map<Integer, String> types = service.GetTypes();
        assertNotNull(types);
        assertTrue(types.containsKey(VideoType.Movie.ordinal()));
    }
}
