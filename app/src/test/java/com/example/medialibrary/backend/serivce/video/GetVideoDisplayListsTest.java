package com.example.medialibrary.backend.serivce.video;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import models.book.BookItem;
import models.shared.DisplayMediaItem;
import models.video.Video;
import models.video.VideoFilter;
import repository.VideoRepository;
import repository.database.MediaLibraryDbHelper;
import serivce.SharedService;
import serivce.VideoService;

@ExtendWith(MockitoExtension.class)
public class GetVideoDisplayListsTest {
    @Mock
    private VideoRepository mockVideoRepository;

    private SharedService sharedService;
    private VideoService videoService;

    @BeforeEach
    public void setUp() {
        sharedService = new SharedService((MediaLibraryDbHelper) null);
        videoService = new VideoService(mockVideoRepository, sharedService);
    }

    @Test
    public void GetBookDisplayLists_emptyList_returnsEmptyList() {
        List<Video> videos = new ArrayList<>();
        VideoFilter filter = new VideoFilter();
        filter.AnyOwned = true;

        when(mockVideoRepository.GetVideos(any(), any())).thenReturn(videos);

        List<DisplayMediaItem> result = videoService.GetVideoDisplayLists(filter);
        assertTrue(result.isEmpty());
    }

    @Test
    public void GetBookDisplayLists_filterIsActive_returnsFilteredList() {
        List<Video> videos = new ArrayList<>();
        VideoFilter filter = new VideoFilter();
        filter.Collected = true;

        Video video = new Video();
        video.Id = "1";
        video.Title = "Test Book";
        BookItem item1 = new BookItem();
        item1.Owned = true;

        videos.add(video);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());

        when(mockVideoRepository.GetVideos(eq(whereClause), any())).thenReturn(videos);

        List<DisplayMediaItem> result = videoService.GetVideoDisplayLists(filter);

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).Id);
    }

    @Test
    public void GetBookDisplayLists_sortIsApplied_returnsSortedList() {
        List<Video> videos = new ArrayList<>();
        VideoFilter filter = new VideoFilter();
        filter.Collected = true;

        Video video1 = new Video();
        video1.Id = "1";
        video1.Title = "Test Book";
        BookItem item1 = new BookItem();
        item1.Owned = true;

        Video video2 = new Video();
        video2.Id = "2";
        video2.Title = "Filtered Book";
        videos.add(video1);
        videos.add(video2);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());

        when(mockVideoRepository.GetVideos(eq(whereClause), any())).thenReturn(videos);

        List<DisplayMediaItem> result = videoService.GetVideoDisplayLists(filter);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).Id);
    }
}
