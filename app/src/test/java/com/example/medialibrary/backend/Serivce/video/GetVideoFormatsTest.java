package com.example.medialibrary.backend.Serivce.video;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.Serivce.VideoService;
import com.example.medialibrary.backend.models.video.Enums.VideoFormat;
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
