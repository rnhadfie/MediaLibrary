package com.example.medialibrary.backend.serivce.shared;

import static org.junit.Assert.*;
import serivce.SharedService;
import models.shared.GenreObject;
import repository.database.MediaLibraryDbHelper;

import java.util.List;
import java.util.Objects;

import org.junit.Test;

public class GetGenresTest {
    @Test
    public void testGetGenres() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        List<GenreObject> genres = service.GetGenres();
        
        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        assertTrue(genres.stream().anyMatch(it -> Objects.equals(it.genreName, "Action")));
    }
}
