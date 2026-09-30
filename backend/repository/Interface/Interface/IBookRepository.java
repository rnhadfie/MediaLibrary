package repository.Interface.Interface;

import models.book.*;
import java.util.List;


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
