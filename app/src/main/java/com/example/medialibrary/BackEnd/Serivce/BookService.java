package com.example.medialibrary.backend.Serivce;

import com.example.medialibrary.backend.models.book.*;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.repository.BookRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.Lazy;
import kotlin.LazyKt;

public class BookService {
    private final MediaLibraryDbHelper dbHelper;

    public Lazy<BookRepository> bookRepository;
    public Lazy<SharedService> sharedService;

    public BookService(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.bookRepository = LazyKt.lazy(() -> new BookRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));
    }

    public List<Book> GetBooks(BookFilter filter) {
        var repo = this.bookRepository.getValue();
        List<Book> books = repo.GetBooks(filter);
        return BookItemBasedFilters(books, filter);
    }

    public List<DisplayMediaItem> GetBookDisplayLists(BookFilter filter) {
        var repo = this.bookRepository.getValue();
        var books =repo.GetBooks(filter);
        var sharedService = this.sharedService.getValue();

        return sharedService.mapToDisplayItems(BookItemBasedFilters(books, filter));
    }


    public Book GetBook(int id) {
        var repo = this.bookRepository.getValue();
        return repo.GetBook(id);

    }

    public boolean AddBook(BookSaveObject book) {
        var repo = this.bookRepository.getValue();
        return repo.AddBook(book);
    }

    public boolean EditBook(BookSaveObject book) {
        var repo = this.bookRepository.getValue();
        return repo.UpdateBook(book);
    }

    public boolean DeleteBook(int id) {
        var repo = this.bookRepository.getValue();
        return repo.DeleteBook(id);
    }

    public BookSetup GetBookSetup () {
        var bookSetup = new BookSetup();
        var sharedService = this.sharedService.getValue();
        bookSetup.Format = this.GetBookItemFormats();
        bookSetup.Genre = sharedService.GetGenres();
        bookSetup.Type = this.GetBookTypes();
        bookSetup.Publishers = this.GetPublishers();
        bookSetup.Tag = this.sharedService.getValue().GetTags();
        //get tags
        return bookSetup;
    }

    public List<Book> BookItemBasedFilters(List<Book> listOfBooks, BookFilter fitler)
    {
        List<Book> filteredList = new ArrayList<Book>();
        if(listOfBooks.stream().count() > 0) {
            for (Book book : listOfBooks) {
                if(book.Items != null) {
                    book.CurrentOwnAny = book.Items.stream().count() <= 0 || book.Items.stream().anyMatch(x -> x.Owned);
                }
                else {
                    book.CurrentOwnAny = false;
                }
                filteredList.add(book);
            }

            if (fitler != null) {
                if (fitler.AnyOwned != null) {
                    filteredList = filteredList.stream().filter(book -> book.CurrentOwnAny == fitler.AnyOwned).collect(Collectors.toList());
                }
                if (fitler.PrimaryFormat != null) {
                    filteredList = filteredList.stream().filter(book -> GetCommonBookFormat(book.Items) == fitler.PrimaryFormat).collect(Collectors.toList());
                }
            }
        }

        return filteredList;
    }


    public Map<Integer,String> GetBookItemFormats() {
        var sharedService = this.sharedService.getValue();
        Enums.BookFormat[] formats = Enums.BookFormat.values();
        Map<Integer,String> formatMap = new HashMap<>();
        for (Enums.BookFormat format : formats) {
            formatMap.put(format.ordinal(), sharedService.GetSeperatedString(format.toString()));
        }
        return formatMap;
    };


    public Map<Integer,String> GetBookTypes() {
        var sharedService = this.sharedService.getValue();
        Enums.BookType[] types = Enums.BookType.values();
        Map<Integer,String> typeMap = new HashMap<>();
        for (Enums.BookType type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    };

    public List<Publisher> GetPublishers() {
        var repo = this.bookRepository.getValue();
        return repo.GetPublishers();
    }

    public boolean AddPublisher(Publisher publisher) {
        var repo = this.bookRepository.getValue();
        return repo.AddPublisher(publisher);
    }

    public boolean UpdatePublisher(Publisher publisher) {
        var repo = this.bookRepository.getValue();
        return repo.UpdatePublisher(publisher);
    }

    public boolean DeletePublisher(int id) {
        var repo = this.bookRepository.getValue();
        return repo.DeletePublisher(id);
    }

    private Enums.BookFormat GetCommonBookFormat(List<BookItem> items) {
        Optional<Enums.BookFormat> mostCommonCity = items.stream()
                .map(BookItem::getFormat) // 1. Extract the property
                .filter(Objects::nonNull) // Optional: filter out nulls
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())) // 2. Count occurrences
                .entrySet().stream()
                .max(Map.Entry.comparingByValue()) // 3. Find the highest count
                .map(Map.Entry::getKey);
        return mostCommonCity.orElse(null);
    }
}
