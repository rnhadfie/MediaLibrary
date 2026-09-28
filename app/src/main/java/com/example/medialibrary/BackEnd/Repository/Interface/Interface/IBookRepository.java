package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.BookItem;
import com.example.medialibrary.backend.models.book.BookSaveObject;
import com.example.medialibrary.backend.models.book.Publisher;
import com.example.medialibrary.backend.models.shared.DataContainer;
import com.example.medialibrary.backend.models.shared.ImportObject;

import java.util.List;
import java.util.Map;

public interface IBookRepository {
    List<Book> GetBooks();
    List<Book> GetBooks(String whereClause, List<String> selectionArgs);
    Book GetBook(String id);

    boolean AddBook(BookSaveObject bookObj);
    boolean UpdateBook(BookSaveObject bookObj);
    boolean DeleteBook(String id);

    List<Publisher> GetPublishers();

    boolean AddPublisher(Publisher publisher);
    boolean UpdatePublisher(Publisher publisher);
    boolean DeletePublisher(String id);

}
