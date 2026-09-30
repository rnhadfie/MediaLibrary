package com.example.medialibrary.backend.serivce.shared;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.Objects;

import models.shared.GenreObject;
import repository.database.MediaLibraryDbHelper;
import serivce.SharedService;

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
