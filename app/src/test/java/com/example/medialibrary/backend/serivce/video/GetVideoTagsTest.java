package com.example.medialibrary.backend.serivce.video;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.Map;

import kotlin.LazyKt;
import models.video.Enums.VideoTag;
import serivce.SharedService;
import serivce.VideoService;

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
