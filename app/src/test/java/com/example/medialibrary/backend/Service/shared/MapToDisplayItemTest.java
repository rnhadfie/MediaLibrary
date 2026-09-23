package com.example.medialibrary.backend.Service.shared;

import static org.junit.Assert.*;
import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.shared.Enums;
import org.junit.Test;

public class MapToDisplayItemTest {
    @Test
    public void testMapToDisplayItem_Book() {
        SharedService service = new SharedService(null);
        Book book = new Book();
        book.Id = 1;
        book.Title = "Test Book";
        book.Collecting = true;
        book.CurrentOwnAny = false;

        DisplayMediaItem displayItem = service.mapToDisplayItem(book);

        assertEquals(1, displayItem.Id);
        assertEquals("Test Book", displayItem.Title);
        assertEquals(Enums.MediaType.Book, displayItem.MediaType);
        assertTrue(displayItem.Collecting);
        assertTrue(displayItem.ToCollect);
    }
}
