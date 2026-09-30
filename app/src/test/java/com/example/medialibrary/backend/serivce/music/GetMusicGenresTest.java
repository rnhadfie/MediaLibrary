package com.example.medialibrary.backend.serivce.music;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.Map;

import kotlin.LazyKt;
import models.music.Enums.MusicGenre;
import serivce.MusicService;
import serivce.SharedService;


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
