package com.example.medialibrary.backend.serivce.shared;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import models.book.Book;
import models.shared.DisplayMediaItem;
import models.shared.Enums;
import repository.database.MediaLibraryDbHelper;
import serivce.SharedService;

public class MapToDisplayItemTest {
    @Test
    public void testMapToDisplayItem_Book() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        Book book = new Book();
        book.Id = "1";
        book.Title = "Test Book";
        book.Collecting = true;
        book.CurrentOwnAny = false;

        DisplayMediaItem displayItem = service.mapToDisplayItem(book, false);

        assertEquals("1", displayItem.Id);
        assertEquals("Test Book", displayItem.Title);
        assertEquals(Enums.MediaType.Book, displayItem.MediaType);
    }
}
