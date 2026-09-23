package com.example.medialibrary.backend.Serivce.shared;

import static org.junit.Assert.*;

import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.shared.Enums;
import java.util.Map;
import org.junit.Test;

public class GetGenresTest {
    @Test
    public void testGetGenres() {
        SharedService service = new SharedService(null);
        Map<Integer, String> genres = service.GetGenres();
        
        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        assertTrue(genres.containsKey(Enums.Genre.Action.ordinal()));
        assertEquals("Action", genres.get(Enums.Genre.Action.ordinal()));
    }
}
