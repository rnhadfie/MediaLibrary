package com.example.medialibrary.backend.serivce.video;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import serivce.SharedService;
import serivce.VideoService;
import models.video.Enums.VideoFormat;
import java.util.Map;
import org.junit.Test;

import kotlin.LazyKt;

public class GetVideoFormatsTest {
    @Test
    public void testGetVideoFormats() {
        SharedService sharedService = mock(SharedService.class);
        when(sharedService.GetSeperatedString(anyString())).thenAnswer(i -> i.getArguments()[0]);
        
        VideoService service = new VideoService(null);
        service.sharedService = LazyKt.lazy(() -> sharedService);
        
        Map<Integer, String> formats = service.GetFormats();
        assertNotNull(formats);
        assertTrue(formats.containsKey(VideoFormat.DVD.ordinal()));
    }
}
