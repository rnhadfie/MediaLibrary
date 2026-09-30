package com.example.medialibrary.backend.serivce.main;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.List;
import java.util.Objects;

import kotlin.LazyKt;
import models.shared.GenreObject;
import serivce.MainService;
import serivce.SharedService;

public class GetGenresTest {
    @Test
    public void testMainGetGenres() {
        SharedService sharedService = mock(SharedService.class);
        when(sharedService.GetSeperatedString(anyString())).thenAnswer(i -> i.getArguments()[0]);
        
        MainService service = new MainService(null);
        service.sharedService = LazyKt.lazy(() -> sharedService);
        
        List<GenreObject> genres = service.GetGenres();
        assertNotNull(genres);
        assertTrue(genres.stream().anyMatch(it -> Objects.equals(it.genreName, "Action")));
    }
}
