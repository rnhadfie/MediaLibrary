package com.example.medialibrary.backend.Serivce.video;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import android.provider.MediaStore;

import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.Serivce.VideoService;
import com.example.medialibrary.backend.models.book.BookFilter;
import com.example.medialibrary.backend.models.shared.Tag;
import com.example.medialibrary.backend.models.video.Enums;
import com.example.medialibrary.backend.models.video.Video;
import com.example.medialibrary.backend.models.video.VideoSetup;
import com.example.medialibrary.backend.repository.VideoRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

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
        Assert.assertTrue(result.Types.size() > 0);
        Assert.assertTrue(result.Types.containsKey(Enums.VideoType.Movie.ordinal()));

        //Category
        assertNotNull(result.VideoTags);
        Assert.assertTrue(result.VideoTags.size() > 0);
        Assert.assertTrue(result.VideoTags.containsKey(Enums.VideoTag.Anime.ordinal()));

        //Tag
        assertNotNull(result.Tag);
        Assert.assertTrue(result.Tag.size() > 0);
        Assert.assertTrue(result.Tag.stream().filter(p -> p.Id.equals("1")).findFirst().isPresent());
    }
}
