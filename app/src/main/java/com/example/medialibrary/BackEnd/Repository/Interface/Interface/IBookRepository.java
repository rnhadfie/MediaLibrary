package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.BookSaveObject;
import com.example.medialibrary.backend.models.book.Publisher;
import java.util.List;

public interface IBookRepository {
    List<Book> GetBooks();
    List<Book> GetBooks(String whereClause, List<String> selectionArgs);
    Book GetBook(int id);

    boolean AddBook(BookSaveObject bookObj);
    boolean UpdateBook(BookSaveObject bookObj);
    boolean DeleteBook(int id);

    List<Publisher> GetPublishers();

    boolean AddPublisher(Publisher publisher);
    boolean UpdatePublisher(Publisher publisher);
    boolean DeletePublisher(int id);
}
