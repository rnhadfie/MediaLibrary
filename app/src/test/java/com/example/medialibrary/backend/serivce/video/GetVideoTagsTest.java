package com.example.medialibrary.backend.serivce.video;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import serivce.SharedService;
import serivce.VideoService;
import models.video.Enums.VideoTag;
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
