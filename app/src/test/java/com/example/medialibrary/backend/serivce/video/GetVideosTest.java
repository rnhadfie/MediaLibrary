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

import models.video.Video;
import models.video.VideoFilter;
import repository.VideoRepository;
import repository.database.MediaLibraryDbHelper;
import serivce.SharedService;
import serivce.VideoService;

@ExtendWith(MockitoExtension.class)
public class GetVideosTest {

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
        List<Video> cds = new ArrayList<>();
        VideoFilter filter = new VideoFilter();
        filter.AnyOwned = true;

        when(mockVideoRepository.GetVideos(any(), any())).thenReturn(cds);

        List<Video> result = videoService.GetVideos(filter);
        assertTrue(result.isEmpty());
    }

    @Test
    public void GetBookDisplayLists_filterIsActive_returnsFilteredList() {
        List<Video> cds = new ArrayList<>();
        VideoFilter filter = new VideoFilter();
        filter.Collected = true;

        Video cd1 = new Video();
        cd1.Id = "1";
        cd1.Title = "Test Book";


        cds.add(cd1);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());


        when(mockVideoRepository.GetVideos(eq(whereClause), any())).thenReturn(cds);

        List<Video> result = videoService.GetVideos(filter);

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).Id);
    }

    @Test
    public void GetBookDisplayLists_sortIsApplied_returnsSortedList() {
        List<Video> cds = new ArrayList<>();
        VideoFilter filter = new VideoFilter();
        filter.SortAlphabetical = true;

        Video cd1 = new Video();
        cd1.Id = "1";
        cd1.Title = "Test Book";

        Video cd2 = new Video();
        cd2.Id = "2";
        cd2.Title = "Filtered Book";
        cds.add(cd1);
        cds.add(cd2);

        when(mockVideoRepository.GetVideos(any(), any())).thenReturn(cds);

        List<Video> result = videoService.GetVideos(filter);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).Id);
    }
}
