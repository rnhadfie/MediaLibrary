package com.example.medialibrary.backend.serivce.music;


import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import serivce.MusicService;
import serivce.SharedService;
import models.book.BookFilter;
import models.music.Enums;
import models.music.MusicSetup;
import models.shared.Tag;
import repository.MusicRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;


@ExtendWith(MockitoExtension.class)
public class GetMusicSetupTest {
    @Mock
    private MusicRepository mockMusicRepository;

    @Mock
    private SharedService sharedService;
    private MusicService musicService;

    @BeforeEach
    public void setUp() {
        musicService = new MusicService(mockMusicRepository, sharedService);
    }

    @Test
    public void GetBookDisplayLists_emptyList_returnsEmptyList() {
        BookFilter filter = new BookFilter();
        filter.AnyOwned = true;


        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("1", "Test Tag"));

        when(sharedService.GetTags()).thenReturn(tags);


        MusicSetup result = musicService.GetSetup();

        //Book Format
        assertNotNull(result.MusicGenre);
        Assert.assertTrue(result.MusicGenre.containsKey(Enums.MusicGenre.Pop.ordinal()));

        //Tag
        assertNotNull(result.Tags);
        Assert.assertFalse(result.Tags.isEmpty());
        Assert.assertTrue(result.Tags.stream().anyMatch(p -> p.Id.equals("1")));
    }
}
