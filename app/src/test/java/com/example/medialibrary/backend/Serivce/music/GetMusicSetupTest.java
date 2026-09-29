package com.example.medialibrary.backend.Serivce.music;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


import com.example.medialibrary.backend.Serivce.MusicService;
import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.book.BookFilter;
import com.example.medialibrary.backend.models.music.Enums;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.music.MusicSetup;
import com.example.medialibrary.backend.models.shared.Tag;
import com.example.medialibrary.backend.repository.MusicRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        List<Music> cds = new ArrayList<>();
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
        Assert.assertTrue(result.Tags.size() > 0);
        Assert.assertTrue(result.Tags.stream().filter(p -> p.Id.equals("1")).findFirst().isPresent());
    }
}
