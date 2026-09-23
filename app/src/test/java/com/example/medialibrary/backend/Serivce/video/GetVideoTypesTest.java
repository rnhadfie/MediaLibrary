package com.example.medialibrary.backend.Serivce.video;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.Serivce.VideoService;
import com.example.medialibrary.backend.models.video.Enums.VideoType;
import java.util.Map;
import org.junit.Test;

import kotlin.LazyKt;

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
