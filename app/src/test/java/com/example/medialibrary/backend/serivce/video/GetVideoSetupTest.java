package com.example.medialibrary.backend.serivce.video;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import models.book.BookFilter;
import models.shared.Tag;
import models.video.Enums;
import models.video.VideoSetup;
import repository.VideoRepository;
import serivce.SharedService;
import serivce.VideoService;

@ExtendWith(MockitoExtension.class)
public class GetVideoSetupTest {
    @Mock
    private VideoRepository mockVideoRepository;

    @Mock
    private SharedService sharedService;
    private VideoService videoService;

    @BeforeEach
    public void setUp() {
        videoService = new VideoService(mockVideoRepository, sharedService);
    }

    @Test
    public void GetBookDisplayLists_emptyList_returnsEmptyList() {
        BookFilter filter = new BookFilter();
        filter.AnyOwned = true;


        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("1", "Test Tag"));

        when(sharedService.GetTags()).thenReturn(tags);


        VideoSetup result = videoService.GetVideoSetup();

        //Type
        assertNotNull(result.Types);
        Assert.assertFalse(result.Types.isEmpty());
        Assert.assertTrue(result.Types.containsKey(Enums.VideoType.Movie.ordinal()));

        //Category
        assertNotNull(result.VideoTags);
        Assert.assertFalse(result.VideoTags.isEmpty());
        Assert.assertTrue(result.VideoTags.containsKey(Enums.VideoTag.Anime.ordinal()));

        //Tag
        assertNotNull(result.Tag);
        Assert.assertFalse(result.Tag.isEmpty());
        Assert.assertTrue(result.Tag.stream().anyMatch(p -> p.Id.equals("1")));
    }
}
