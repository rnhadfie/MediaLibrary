package com.example.medialibrary.backend.Serivce.main;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.medialibrary.backend.Serivce.MainService;
import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.models.shared.MediaItem;
import com.example.medialibrary.backend.repository.BookRepository;
import com.example.medialibrary.backend.repository.MusicRepository;
import com.example.medialibrary.backend.repository.OtherRepository;
import com.example.medialibrary.backend.repository.VideoRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

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
