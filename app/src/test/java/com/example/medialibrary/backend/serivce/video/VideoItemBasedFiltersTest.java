package com.example.medialibrary.backend.serivce.video;

import static org.junit.Assert.*;

import serivce.VideoService;
import models.video.Video;
import models.video.VideoFilter;
import models.video.VideoItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class VideoItemBasedFiltersTest {
    @Test
    public void testVideoItemBasedFilters_AnyOwnedTrue() {
        VideoService service = new VideoService(null);
        List<Video> videos = new ArrayList<>();
        
        Video v1 = new Video();
        v1.Items = new ArrayList<>();
        VideoItem i1 = new VideoItem(); i1.Owned = true;
        v1.Items.add(i1);
        
        Video v2 = new Video();
        v2.Items = new ArrayList<>();
        VideoItem i2 = new VideoItem(); i2.Owned = false;
        v2.Items.add(i2);
        
        videos.add(v1);
        videos.add(v2);
        
        VideoFilter filter = new VideoFilter();
        filter.AnyOwned = true;
        
        List<Video> result = service.VideoItemBasedFilters(videos, filter);
        assertEquals(1, result.size());
        assertTrue(result.get(0).CurrentOwnAny);
    }
}
