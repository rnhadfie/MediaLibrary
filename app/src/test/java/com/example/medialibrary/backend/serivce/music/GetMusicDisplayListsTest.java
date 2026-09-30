package com.example.medialibrary.backend.serivce.music;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import serivce.MusicService;
import serivce.SharedService;
import models.book.BookItem;
import models.music.Music;
import models.music.MusicFilter;
import models.shared.DisplayMediaItem;
import repository.MusicRepository;
import repository.database.MediaLibraryDbHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetMusicDisplayListsTest {
    @Mock
    private MusicRepository mockBookRepository;

    private SharedService sharedService;
    private MusicService musicService;

    @BeforeEach
    public void setUp() {
        sharedService = new SharedService((MediaLibraryDbHelper) null);
        musicService = new MusicService(mockBookRepository, sharedService);
    }

    @Test
    public void GetMusicDisplayLists_emptyList_returnsEmptyList() {
        List<Music> cds = new ArrayList<>();
        MusicFilter filter = new MusicFilter();
        filter.AnyOwned = true;

        when(mockBookRepository.GetMusic(any(), any())).thenReturn(cds);

        List<DisplayMediaItem> result = musicService.GetMusicDisplayLists(filter);
        assertTrue(result.isEmpty());
    }

    @Test
    public void GetMusicDisplayLists_filterIsActive_returnsFilteredList() {
        List<Music> cds = new ArrayList<>();
        MusicFilter filter = new MusicFilter();
        filter.AnyOwned = true;

        Music cd1 = new Music();
        cd1.Id = "1";
        cd1.Title = "Test Book";
        BookItem item1 = new BookItem();
        item1.Owned = true;

        cds.add(cd1);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());

        when(mockBookRepository.GetMusic(eq(whereClause), any())).thenReturn(cds);

        List<DisplayMediaItem> result = musicService.GetMusicDisplayLists(filter);

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).Id);
    }

    @Test
    public void GetMusicDisplayLists_sortIsApplied_returnsSortedList() {
        List<Music> cds = new ArrayList<>();
        MusicFilter filter = new MusicFilter();
        filter.Collecting = true;

        Music cd1 = new Music();
        cd1.Id = "1";
        cd1.Title = "Test Book";
        BookItem item1 = new BookItem();
        item1.Owned = true;

        Music cd2 = new Music();
        cd2.Id = "2";
        cd2.Title = "Filtered Book";
        cds.add(cd1);
        cds.add(cd2);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());

        when(mockBookRepository.GetMusic(eq(whereClause), any())).thenReturn(cds);

        List<DisplayMediaItem> result = musicService.GetMusicDisplayLists(filter);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).Id);
    }
}
