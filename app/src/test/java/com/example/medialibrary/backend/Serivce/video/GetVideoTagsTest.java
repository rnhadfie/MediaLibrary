package com.example.medialibrary.backend.Serivce.video;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.Serivce.VideoService;
import com.example.medialibrary.backend.models.video.Enums.VideoTag;
import java.util.Map;
import org.junit.Test;

import kotlin.LazyKt;

public class GetVideoTagsTest {
    @Test
    public void testGetVideoTags() {
        SharedService sharedService = mock(SharedService.class);
        when(sharedService.GetSeperatedString(anyString())).thenAnswer(i -> i.getArguments()[0]);
        
        VideoService service = new VideoService(null);
        service.sharedService = LazyKt.lazy(() -> sharedService);
        
        Map<Integer, String> tags = service.GetVideoTags();
        assertNotNull(tags);
        assertTrue(tags.containsKey(VideoTag.Anime.ordinal()));
    }
}
