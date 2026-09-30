package com.example.medialibrary.backend.serivce.main;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import serivce.MainService;
import serivce.SharedService;
import models.shared.GenreObject;
import java.util.List;
import java.util.Objects;
import org.junit.Test;
import kotlin.LazyKt;

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
