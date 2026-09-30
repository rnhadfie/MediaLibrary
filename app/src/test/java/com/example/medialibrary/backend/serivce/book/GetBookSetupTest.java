package com.example.medialibrary.backend.serivce.book;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import serivce.BookService;
import serivce.SharedService;
import models.book.BookFilter;
import models.book.BookSetup;
import models.book.Enums;
import models.book.Publisher;
import models.shared.GenreObject;
import models.shared.Tag;
import repository.BookRepository;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
public class GetBookSetupTest {
    @Mock
    private BookRepository mockBookRepository;

    @Mock
    private SharedService sharedService;
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        bookService = new BookService(mockBookRepository, sharedService);
    }

    @Test
    public void GetBookDisplayLists_emptyList_returnsEmptyList() {
        BookFilter filter = new BookFilter();
        filter.AnyOwned = true;

        List<Publisher> publishers = new ArrayList<>();
        publishers.add(new Publisher("1", "Test Publisher"));

        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("1", "Test Tag"));

        List<GenreObject> genres = new ArrayList<>();
        genres.add(new GenreObject(1, "Action"));
        genres.add(new GenreObject(2, "Adventure"));


        when(sharedService.GetTags()).thenReturn(tags);
        when(sharedService.GetGenres()).thenReturn(genres);
        when(mockBookRepository.GetPublishers()).thenReturn(publishers);


        BookSetup result = bookService.GetBookSetup();

        //Book Format
        assertNotNull(result.Format);
        Assert.assertTrue(result.Format.containsKey(Enums.BookFormat.Paperback.ordinal()));

        //Book Type

        assertNotNull(result.Type);
        Assert.assertTrue(result.Type.containsKey(Enums.BookType.Manga.ordinal()));

        //Publisher
        assertNotNull(result.Publishers);
        assertFalse(result.Publishers.isEmpty());
        Assert.assertTrue(result.Publishers.stream().anyMatch(p -> p.Id.equals("1")));

        //Genre

        assertFalse(result.Genre.isEmpty());
        Assert.assertTrue(result.Genre.stream().anyMatch(it -> Objects.equals(it.genreName, "Action")));

        //Tag
        assertNotNull(result.Tag);
        assertFalse(result.Tag.isEmpty());
        Assert.assertTrue(result.Tag.stream().anyMatch(p -> p.Id.equals("1")));
    }
}
