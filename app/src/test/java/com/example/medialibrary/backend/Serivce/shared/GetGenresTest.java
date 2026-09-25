package com.example.medialibrary.backend.Serivce.shared;

import static org.junit.Assert.*;
import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.shared.GenreObject;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

public class GetGenresTest {
    @Test
    public void testGetGenres() {
        SharedService service = new SharedService(null);
        List<GenreObject> genres = service.GetGenres();
        
        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        assertTrue(genres.stream().anyMatch(it -> Objects.equals(it.genreName, "Action")));
    }
}
