package com.example.medialibrary.backend.serivce.book;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import serivce.BookService;
import serivce.SharedService;
import models.book.Enums;
import java.util.Map;
import org.junit.Test;

import kotlin.LazyKt;

public class GetBookTypesTest {
    @Test
    public void GetBookTypes_returnsMap() {
        SharedService sharedService = mock(SharedService.class);
        when(sharedService.GetSeperatedString(anyString())).thenAnswer(i -> i.getArguments()[0]);
        
        BookService service = new BookService(null);
        service.sharedService = LazyKt.lazy(() -> sharedService);
        
        Map<Integer, String> types = service.GetBookTypes();
        assertNotNull(types);
        assertTrue(types.containsKey(Enums.BookType.Manga.ordinal()));
    }
}
