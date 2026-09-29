package com.example.medialibrary.backend.Serivce.music;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.medialibrary.backend.Serivce.MusicService;
import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.music.Enums.MusicGenre;
import java.util.Map;
import org.junit.Test;

import kotlin.LazyKt;


public class GetMusicGenresTest {
    @Test
    public void testGetMusicGenres() {
        SharedService sharedService = mock(SharedService.class);
        when(sharedService.GetSeperatedString(anyString())).thenAnswer(i -> i.getArguments()[0]);
        
        MusicService service = new MusicService(null);
        service.sharedService = LazyKt.lazy(() -> sharedService);
        
        Map<Integer, String> genres = service.GetMusicGenres();
        assertNotNull(genres);
        assertTrue(genres.containsKey(MusicGenre.Pop.ordinal()));
    }
}
