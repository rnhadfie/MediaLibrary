package com.example.medialibrary.backend.Serivce.music;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.medialibrary.backend.Serivce.MusicService;
import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.music.MusicFilter;
import com.example.medialibrary.backend.repository.MusicRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetMusicsTest {

    @Mock
    private MusicRepository mockMusicRepository;

    private SharedService sharedService;
    private MusicService bookService;

    @BeforeEach
    public void setUp() {
        sharedService = new SharedService((MediaLibraryDbHelper) null);
        bookService = new MusicService(mockMusicRepository, sharedService);
    }

    @Test
    public void GetBookDisplayLists_emptyList_returnsEmptyList() {
        List<Music> cds = new ArrayList<>();
        MusicFilter filter = new MusicFilter();
        filter.AnyOwned = true;

        when(mockMusicRepository.GetMusic(any(), any())).thenReturn(cds);

        List<Music> result = bookService.GetMusics(filter);
        assertTrue(result.isEmpty());
    }

    @Test
    public void GetBookDisplayLists_filterIsActive_returnsFilteredList() {
        List<Music> cds = new ArrayList<>();
        MusicFilter filter = new MusicFilter();
        filter.AnyOwned = true;

        Music cd1 = new Music();
        cd1.Id = "1";
        cd1.Title = "Test Book";


        cds.add(cd1);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());


        when(mockMusicRepository.GetMusic(eq(whereClause), any())).thenReturn(cds);

        List<Music> result = bookService.GetMusics(filter);

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).Id);
    }

    @Test
    public void GetBookDisplayLists_sortIsApplied_returnsSortedList() {
        List<Music> cds = new ArrayList<>();
        MusicFilter filter = new MusicFilter();
        filter.SortAlphabetical = true;

        Music cd1 = new Music();
        cd1.Id = "1";
        cd1.Title = "Test Book";

        Music cd2 = new Music();
        cd2.Id = "2";
        cd2.Title = "Filtered Book";
        cds.add(cd1);
        cds.add(cd2);

        when(mockMusicRepository.GetMusic(any(), any())).thenReturn(cds);

        List<Music> result = bookService.GetMusics(filter);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).Id);
    }
}
