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

    public Lazy<BookRepository> bookRepository;
    public Lazy<SharedService> sharedService;

    public BookService(MediaLibraryDbHelper dbHelper) {
        this.bookRepository = LazyKt.lazy(() -> new BookRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));
    }

    public List<Book> GetBooks(BookFilter filter) {
        var repo = this.bookRepository.getValue();

        List<String> selectionArgs = new ArrayList<>();
        String whereClause = sharedService.getValue().BuildWhereClause(filter, selectionArgs);

        List<Book> books = repo.GetBooks(whereClause, selectionArgs);
        return BookItemBasedFilters(books, filter);
    }

    public List<DisplayMediaItem> GetBookDisplayLists(BookFilter filter) {
        var repo = this.bookRepository.getValue();

        var sharedService = this.sharedService.getValue();

        List<String> selectionArgs = new ArrayList<>();
        String whereClause = sharedService.BuildWhereClause(filter, selectionArgs);

        List<Book> books = repo.GetBooks(whereClause, selectionArgs);

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

    public List<Book> BookItemBasedFilters(List<Book> listOfBooks, BookFilter filter)
    {
        if (listOfBooks == null || listOfBooks.isEmpty()) {
            return new ArrayList<>();
        }

        List<Book> filteredList = new ArrayList<>();
        for (Book book : listOfBooks) {
            if (book.Items != null && !book.Items.isEmpty()) {
                boolean hasOwned = false;
                for (BookItem item : book.Items) {
                    if (item.Owned) {
                        hasOwned = true;
                        break;
                    }
                }
                book.CurrentOwnAny = hasOwned;
            } else {
                book.CurrentOwnAny = true; // Match stream logic: count() <= 0 || anyMatch(Owned) -> true if count is 0
            }

            if (filter != null) {
                if (filter.AnyOwned != null && book.CurrentOwnAny != filter.AnyOwned) {
                    continue;
                }
                Enums.BookFormat commonFormat = book.Items != null ? GetCommonBookFormat(book.Items) : null;
                if (filter.IncludedFormats != null && !filter.IncludedFormats.isEmpty()) {
                    if (commonFormat == null || !filter.IncludedFormats.contains(commonFormat)) {
                        continue;
                    }
                } else if (filter.PrimaryFormat != null && filter.PrimaryFormat != Enums.BookFormat.NoneSelected) {
                    if (commonFormat != filter.PrimaryFormat) {
                        continue;
                    }
                }
                if (filter.ExcludedFormats != null && !filter.ExcludedFormats.isEmpty()) {
                    if (commonFormat != null && filter.ExcludedFormats.contains(commonFormat)) {
                        continue;
                    }
                }
            }
            filteredList.add(book);
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
    }


    public Map<Integer,String> GetBookTypes() {
        var sharedService = this.sharedService.getValue();
        Enums.BookType[] types = Enums.BookType.values();
        Map<Integer,String> typeMap = new HashMap<>();
        for (Enums.BookType type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    }

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

    public Enums.BookFormat GetCommonBookFormat(List<BookItem> items) {
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
