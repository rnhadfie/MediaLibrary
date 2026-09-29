package com.example.medialibrary.backend.Serivce.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.medialibrary.backend.Serivce.BookService;
import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.BookFilter;
import com.example.medialibrary.backend.models.book.BookItem;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetBookDisplayListsTest {

    @Mock
    private BookRepository mockBookRepository;

    private SharedService sharedService;
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        sharedService = new SharedService(null);
        bookService = new BookService(mockBookRepository, sharedService);
    }

    @Test
    public void GetBookDisplayLists_emptyList_returnsEmptyList() {
        List<Book> books = new ArrayList<>();
        BookFilter filter = new BookFilter();
        filter.AnyOwned = true;

        when(mockBookRepository.GetBooks(any(), any())).thenReturn(books);

        List<DisplayMediaItem> result = bookService.GetBookDisplayLists(filter);
        assertTrue(result.isEmpty());
    }

    @Test
    public void GetBookDisplayLists_filterIsActive_returnsFilteredList() {
        List<Book> books = new ArrayList<>();
        BookFilter filter = new BookFilter();
        filter.AnyOwned = true;

        Book book1 = new Book();
        book1.Id = "1";
        book1.Title = "Test Book";
        BookItem item1 = new BookItem();
        item1.Owned = true;
        book1.Items = List.of(item1);

        Book book2 = new Book();
        book2.Id = "2";
        book2.Title = "Filtered Book";
        book2.Items = new ArrayList<>();
        books.add(book1);
        books.add(book2);

        when(mockBookRepository.GetBooks(any(), any())).thenReturn(books);

        List<DisplayMediaItem> result = bookService.GetBookDisplayLists(filter);

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).Id);
    }

    @Test
    public void GetBookDisplayLists_sortIsApplied_returnsSortedList() {
        List<Book> books = new ArrayList<>();
        BookFilter filter = new BookFilter();
        filter.SortAlphabetical = true;

        Book book1 = new Book();
        book1.Id = "1";
        book1.Title = "Test Book";
        book1.Items = new ArrayList<>();

        Book book2 = new Book();
        book2.Id = "2";
        book2.Title = "Filtered Book";
        book2.Items = new ArrayList<>();
        books.add(book1);
        books.add(book2);

        when(mockBookRepository.GetBooks(any(), any())).thenReturn(books);

        List<DisplayMediaItem> result = bookService.GetBookDisplayLists(filter);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).Id);
    }
}