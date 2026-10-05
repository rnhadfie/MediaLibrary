package com.example.medialibrary.backend.serivce.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import models.book.Book;
import models.book.BookFilter;
import models.book.BookItem;
import models.shared.Enums;
import repository.BookRepository;
import repository.database.MediaLibraryDbHelper;
import serivce.BookService;
import serivce.SharedService;

@ExtendWith(MockitoExtension.class)
public class GetBooksTests {
    @Mock
    private BookRepository mockBookRepository;

    private SharedService sharedService;
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        sharedService = new SharedService((MediaLibraryDbHelper) null);
        bookService = new BookService(mockBookRepository, sharedService);
    }

    @Test
    public void GetBookDisplayLists_emptyList_returnsEmptyList() {
        List<Book> books = new ArrayList<>();
        BookFilter filter = new BookFilter();
        filter.AnyOwned = true;

        when(mockBookRepository.GetBooks(any(), any())).thenReturn(books);

        List<Book> result = bookService.GetBooks(filter);
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

        books.add(book1);

        String whereClause = sharedService.BuildWhereClause(filter, new ArrayList<>());

        when(mockBookRepository.GetBooks(eq(whereClause), any())).thenReturn(books);

        List<Book> result = bookService.GetBooks(filter);

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

        List<Book> result = bookService.GetBooks(filter);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).Id);
    }

    @Test
    public void GetBooks_sortPriorityAscending_returnsHighestToLowestPriority() {
        List<Book> books = new ArrayList<>();
        BookFilter filter = new BookFilter();
        filter.SortPriority = true; // Ascending: Highest to Lowest Priority

        Book bookLow = new Book();
        bookLow.Id = "1";
        bookLow.Title = "Low Priority";
        bookLow.CollectingPriority = Enums.CollectingPriority.Low;
        bookLow.Items = new ArrayList<>();

        Book bookHigh = new Book();
        bookHigh.Id = "2";
        bookHigh.Title = "High Priority";
        bookHigh.CollectingPriority = Enums.CollectingPriority.High;
        bookHigh.Items = new ArrayList<>();

        books.add(bookLow);
        books.add(bookHigh);

        when(mockBookRepository.GetBooks(any(), any())).thenReturn(books);

        List<Book> result = bookService.GetBooks(filter);

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).Id); // High priority first
        assertEquals("1", result.get(1).Id); // Low priority second
    }

    @Test
    public void GetBooks_sortPriorityDescending_returnsLowestToHighestPriority() {
        List<Book> books = new ArrayList<>();
        BookFilter filter = new BookFilter();
        filter.SortPriority = false; // Descending: Lowest to Highest Priority

        Book bookLow = new Book();
        bookLow.Id = "1";
        bookLow.Title = "Low Priority";
        bookLow.CollectingPriority = Enums.CollectingPriority.Low;
        bookLow.Items = new ArrayList<>();

        Book bookHigh = new Book();
        bookHigh.Id = "2";
        bookHigh.Title = "High Priority";
        bookHigh.CollectingPriority = Enums.CollectingPriority.High;
        bookHigh.Items = new ArrayList<>();

        books.add(bookHigh);
        books.add(bookLow);

        when(mockBookRepository.GetBooks(any(), any())).thenReturn(books);

        List<Book> result = bookService.GetBooks(filter);

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).Id); // Low priority first
        assertEquals("2", result.get(1).Id); // High priority second
    }

    @Test
    public void GetBooks_customSortOrder_respectsSpecifiedOrder() {
        List<Book> books = new ArrayList<>();
        BookFilter filter = new BookFilter();
        filter.SortAlphabetical = true; // "A Book" vs "B Book"
        filter.SortPriority = true; // High vs Low
        filter.SortOrder = List.of("Alphabetical", "Priority");

        Book bookA = new Book();
        bookA.Id = "1";
        bookA.Title = "A Book";
        bookA.CollectingPriority = Enums.CollectingPriority.Low;

        Book bookB = new Book();
        bookB.Id = "2";
        bookB.Title = "B Book";
        bookB.CollectingPriority = Enums.CollectingPriority.High;

        books.add(bookB);
        books.add(bookA);

        when(mockBookRepository.GetBooks(any(), any())).thenReturn(books);

        List<Book> result = bookService.GetBooks(filter);

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).Id); // "A Book" first because Alphabetical was primary

        // Reverse the sort order preference: Priority primary
        filter.SortOrder = List.of("Priority", "Alphabetical");
        List<Book> resultPriorityFirst = bookService.GetBooks(filter);
        assertEquals("2", resultPriorityFirst.get(0).Id); // "B Book" (High Priority) first because Priority was primary
    }
}
