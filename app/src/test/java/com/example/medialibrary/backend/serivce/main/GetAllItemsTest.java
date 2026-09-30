package com.example.medialibrary.backend.serivce.main;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import serivce.MainService;
import serivce.SharedService;
import models.book.Book;
import models.shared.Filter;
import models.shared.MediaItem;
import repository.BookRepository;
import repository.MusicRepository;
import repository.OtherRepository;
import repository.VideoRepository;
import repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import kotlin.LazyKt;

public class GetAllItemsTest {
    @Test
    public void testGetAllItems_Aggregation() {
        BookRepository bookRepo = mock(BookRepository.class);
        VideoRepository videoRepo = mock(VideoRepository.class);
        MusicRepository musicRepo = mock(MusicRepository.class);
        OtherRepository otherRepo = mock(OtherRepository.class);
        SharedService sharedService = new SharedService((MediaLibraryDbHelper) null);
        
        Book book = new Book(); book.Title = "B1";
        when(bookRepo.GetBooks(any(), any())).thenReturn(Collections.singletonList(book));
        when(videoRepo.GetVideos(any(), any())).thenReturn(new ArrayList<>());
        when(musicRepo.GetMusic(any(), any())).thenReturn(new ArrayList<>());
        when(otherRepo.GetOtherCollections(any(), any())).thenReturn(new ArrayList<>());

        MainService service = new MainService(null);
        service.bookRepository = LazyKt.lazy(() -> bookRepo);
        service.videoRepository = LazyKt.lazy(() -> videoRepo);
        service.musicRepository = LazyKt.lazy(() -> musicRepo);
        service.otherRepository = LazyKt.lazy(() -> otherRepo);
        service.sharedService = LazyKt.lazy(() -> sharedService);
        
        Filter filter = new Filter();
        List<MediaItem> result = service.GetAllItems(filter);
        
        assertEquals(1, result.size());
        assertEquals("B1", result.get(0).Title);
    }
}
