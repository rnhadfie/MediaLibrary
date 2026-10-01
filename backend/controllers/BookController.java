package controllers;

import models.shared.DeleteConfirmationResult;
import serivce.BookService;
import models.book.*;
import models.shared.DisplayMediaItem;
import repository.database.MediaLibraryDbHelper;
import java.util.List;
import kotlin.Lazy;
import kotlin.LazyKt;

public class BookController extends BaseController {

    public Lazy<BookService> bookSerivce;

    public BookController(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.bookSerivce = LazyKt.lazy(() -> new BookService(dbHelper));
    }

    public BookController() {
    }
    public List<Book> GetBooks(BookFilter filter) {
        //Get All Books
        return this.bookSerivce.getValue().GetBooks(filter);
    }

    public List<DisplayMediaItem> GetListOfBooks(BookFilter filter) {
        //Get All Books
        return this.bookSerivce.getValue().GetBookDisplayLists(filter);
    }

    public Book GetBook(String id) {

       return this.bookSerivce.getValue().GetBook(id);

    }

    public boolean AddBook(BookSaveObject book) {
        return this.bookSerivce.getValue().AddBook(book);
    }

    public boolean UpdateBook(BookSaveObject book) {
        return this.bookSerivce.getValue().EditBook(book);
    }
    public boolean DeleteBook(String id) {
        return this.bookSerivce.getValue().DeleteBook(id);
    }

    public BookSetup GetBookSetup () {
        return this.bookSerivce.getValue().GetBookSetup();
    }

    public List<Publisher> GetPublishers() {
        return this.bookSerivce.getValue().GetPublishers();
    }

    public boolean AddPublisher(Publisher publisher) {
        return this.bookSerivce.getValue().AddPublisher(publisher);
    }

    public boolean UpdatePublisher(Publisher publisher) {
        return this.bookSerivce.getValue().UpdatePublisher(publisher);
    }

    public DeleteConfirmationResult DeletePublisher(String id, boolean forceDelete) {
        return this.bookSerivce.getValue().DeletePublisher(id, forceDelete);
    }
}
