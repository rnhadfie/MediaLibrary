package com.example.medialibrary.backend.Serivce.book;

import static org.junit.Assert.*;

import com.example.medialibrary.backend.Serivce.BookService;
import com.example.medialibrary.backend.models.book.BookItem;
import com.example.medialibrary.backend.models.book.Enums;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class GetCommonBookFormatTest {
    @Test
    public void testGetCommonBookFormat() {
        BookService service = new BookService(null);
        BookItem item1 = new BookItem(); item1.Format = Enums.BookFormat.Paperback;
        BookItem item2 = new BookItem(); item2.Format = Enums.BookFormat.Hardcover;
        BookItem item3 = new BookItem(); item3.Format = Enums.BookFormat.Paperback;
        
        List<BookItem> items = Arrays.asList(item1, item2, item3);
        
        Enums.BookFormat result = service.GetCommonBookFormat(items);
        assertEquals(Enums.BookFormat.Paperback, result);
    }
}
