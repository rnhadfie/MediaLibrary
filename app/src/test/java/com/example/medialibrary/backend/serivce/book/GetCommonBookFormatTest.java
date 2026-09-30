package com.example.medialibrary.backend.serivce.book;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import models.book.BookItem;
import models.book.Enums;
import serivce.BookService;

public class GetCommonBookFormatTest {
    @Test
    public void GetCommonBookFormat_OneTypeIsMoreCommon_returnsMostCommon() {
        BookService service = new BookService(null);
        BookItem item1 = new BookItem(); item1.Format = Enums.BookFormat.Paperback;
        BookItem item2 = new BookItem(); item2.Format = Enums.BookFormat.Hardcover;
        BookItem item3 = new BookItem(); item3.Format = Enums.BookFormat.Paperback;
        
        List<BookItem> items = Arrays.asList(item1, item2, item3);
        
        Enums.BookFormat result = service.GetCommonBookFormat(items);
        assertEquals(Enums.BookFormat.Paperback, result);
    }

    @Test
    public void GetCommonBookFormat_AllTypesAreEqual_returnsNull() {
        BookService service = new BookService(null);
        BookItem item1 = new BookItem(); item1.Format = Enums.BookFormat.Paperback;
        BookItem item2 = new BookItem(); item2.Format = Enums.BookFormat.Hardcover;
        BookItem item3 = new BookItem(); item3.Format = Enums.BookFormat.EBook;

        List<BookItem> items = Arrays.asList(item1, item2, item3);

        Enums.BookFormat result = service.GetCommonBookFormat(items);
        assertNotNull(result);
    }

    @Test
    public void GetCommonBookFormat_ListIsEmpty_returnsNull() {
        BookService service = new BookService(null);

        List<BookItem> items = List.of();

        Enums.BookFormat result = service.GetCommonBookFormat(items);
        assertNull(result);
    }
}
