package com.example.medialibrary.backend.serivce.book;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.Map;

import kotlin.LazyKt;
import models.book.Enums;
import serivce.BookService;
import serivce.SharedService;

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
