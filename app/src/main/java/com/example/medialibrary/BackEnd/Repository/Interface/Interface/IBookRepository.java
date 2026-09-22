package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.BookFilter;
import com.example.medialibrary.backend.models.book.BookSaveObject;
import com.example.medialibrary.backend.models.book.Publisher;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.models.shared.MediaItem;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public interface IBookRepository {
    <T extends Filter> List<Book> GetBooks(T filter);
    Book GetBook(int id);

    boolean AddBook(BookSaveObject bookObj);
    boolean UpdateBook(BookSaveObject bookObj);
    boolean DeleteBook(int id);

    List<Publisher> GetPublishers();
    Publisher GetPublisher(int id);

    boolean AddPublisher(Publisher publisher);
    boolean UpdatePublisher(Publisher publisher);
    boolean DeletePublisher(int id);
}
