package com.example.medialibrary.backend.Serivce.book;

import static org.junit.Assert.*;

import com.example.medialibrary.backend.Serivce.BookService;
import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.BookFilter;
import com.example.medialibrary.backend.models.book.BookItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BookItemBasedFiltersTest {

    @Test
    public void BookItemBasedFilters_emptyList_returnsEmptyList() {
        BookService bookService = new BookService(null);
        List<Book> result = bookService.BookItemBasedFilters(new ArrayList<>(), null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void BookItemBasedFilters_anyOwnedTrue_returnsFilteredList() {
        BookService bookService = new BookService(null);
        List<Book> books = new ArrayList<>();

        Book book1 = new Book();
        book1.Items = new ArrayList<>();
        BookItem item1 = new BookItem();
        item1.Owned = true;
        book1.Items.add(item1);

        Book book2 = new Book();
        book2.Items = new ArrayList<>();
        BookItem item2 = new BookItem();
        item2.Owned = false;
        book2.Items.add(item2);

        books.add(book1);
        books.add(book2);

        BookFilter filter = new BookFilter();
        filter.AnyOwned = true;

        List<Book> result = bookService.BookItemBasedFilters(books, filter);
        assertEquals(1, result.size());
        assertTrue(result.get(0).CurrentOwnAny);
    }

    @Test
    public void BookItemBasedFilters_anyOwnedFalse_returnsFilteredList() {
        BookService bookService = new BookService(null);
        List<Book> books = new ArrayList<>();

        Book book1 = new Book();
        book1.Items = new ArrayList<>();
        BookItem item1 = new BookItem();
        item1.Owned = true;
        book1.Items.add(item1);

        Book book2 = new Book();
        book2.Items = new ArrayList<>();
        BookItem item2 = new BookItem();
        item2.Owned = false;
        book2.Items.add(item2);

        books.add(book1);
        books.add(book2);

        BookFilter filter = new BookFilter();
        filter.AnyOwned = false;

        List<Book> result = bookService.BookItemBasedFilters(books, filter);
        assertEquals(1, result.size());
        assertFalse(result.get(0).CurrentOwnAny);
    }
}
