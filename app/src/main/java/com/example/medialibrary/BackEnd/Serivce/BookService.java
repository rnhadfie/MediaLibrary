package com.example.medialibrary.backend.Serivce;

import com.example.medialibrary.backend.models.book.*;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.repository.BookRepository;
import com.example.medialibrary.backend.repository.Interface.Interface.IBookRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.Lazy;
import kotlin.LazyKt;

public class BookService {

    public Lazy<BookRepository> bookRepository;
    public Lazy<SharedService> sharedService;
    public Lazy<MediaLibraryDbHelper> dbHelper;

    private BookRepository _BookRepo;
    private SharedService _SharedService;



    public BookService(MediaLibraryDbHelper dbHelper) {
        this.bookRepository = LazyKt.lazy(() -> new BookRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));

        this._BookRepo = this.bookRepository.getValue();
        this._SharedService = this.sharedService.getValue();
    }

    public BookService(BookRepository bookRepository, SharedService sharedService) {
        this.bookRepository = LazyKt.lazy(() -> bookRepository);
        this.sharedService = LazyKt.lazy(() -> sharedService);
        this._BookRepo = bookRepository;
        this._SharedService = sharedService;
    }

    public List<Book> GetBooks(BookFilter filter) {
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = _SharedService.BuildWhereClause(filter, selectionArgs);

        List<Book> books = _BookRepo.GetBooks(whereClause, selectionArgs);
        books = _SharedService.Sort(filter, books);
        return BookItemBasedFilters(books, filter);
    }

    public List<DisplayMediaItem> GetBookDisplayLists(BookFilter filter) {
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = _SharedService.BuildWhereClause(filter, selectionArgs);

        List<Book> books = _BookRepo.GetBooks(whereClause, selectionArgs);
        books = _SharedService.Sort(filter, books);

        return _SharedService.mapToDisplayItems(BookItemBasedFilters(books, filter), false);
    }

    public Book GetBook(String id) {
        return _BookRepo.GetBook(id);
    }

    public boolean AddBook(BookSaveObject book) {
        return _BookRepo.AddBook(book);
    }

    public boolean EditBook(BookSaveObject book) {
        return _BookRepo.UpdateBook(book);
    }

    public boolean DeleteBook(String id) {
        return _BookRepo.DeleteBook(id);
    }

    public BookSetup GetBookSetup() {
        var bookSetup = new BookSetup();
        var sharedService = this.sharedService.getValue();
        bookSetup.Format = this.GetBookItemFormats();
        bookSetup.Genre = sharedService.GetGenres();
        bookSetup.Type = this.GetBookTypes();
        bookSetup.Publishers = this.GetPublishers();
        bookSetup.Tag = this.sharedService.getValue().GetTags();
        return bookSetup;
    }

    public List<Book> BookItemBasedFilters(List<Book> listOfBooks, BookFilter filter) {
        if (listOfBooks == null || listOfBooks.isEmpty()) {
            return new ArrayList<>();
        }

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
                book.CurrentOwnAny = false;
            }
        }

        List<Book> filteredList = listOfBooks;

        if (filter != null) {
            if (filter.AnyOwned != null) {
                filteredList = filteredList.stream()
                        .filter(b -> b.CurrentOwnAny == filter.AnyOwned)
                        .collect(Collectors.toList());
            }

            if (filter.Read != null) {
                filteredList = filteredList.stream()
                        .filter(b -> {
                            boolean allRead = b.Items != null && !b.Items.isEmpty() && b.Items.stream().allMatch(i -> i.Read);
                            boolean isComplete = Boolean.TRUE.equals(b.Ongoing) || Boolean.TRUE.equals(b.HasCollectedAllItems);
                            boolean isRead = isComplete && allRead;
                            return isRead == filter.Read;
                        })
                        .collect(Collectors.toList());
            }

            if (filter.Reading != null) {
                filteredList = filteredList.stream()
                        .filter(b -> {
                            boolean anyRead = b.Items != null && b.Items.stream().anyMatch(i -> i.Read);
                            boolean allRead = b.Items != null && !b.Items.isEmpty() && b.Items.stream().allMatch(i -> i.Read);
                            boolean isComplete = Boolean.TRUE.equals(b.Ongoing) || Boolean.TRUE.equals(b.HasCollectedAllItems);
                            boolean isRead = isComplete && allRead;
                            boolean isReading = anyRead && !isRead;
                            return isReading == filter.Reading;
                        })
                        .collect(Collectors.toList());
            }

            filteredList = filteredList.stream().filter(b -> {
                Enums.BookFormat commonFormat = b.Items != null ? GetCommonBookFormat(b.Items) : null;
                if (commonFormat != null) {
                    var formatNotIncluded = false;
                    var formatExcluded = false;
                    if (filter.IncludedFormats != null && !filter.IncludedFormats.isEmpty()) {
                        formatNotIncluded = !filter.IncludedFormats.contains(commonFormat);
                    }
                    if (filter.ExcludedFormats != null && !filter.ExcludedFormats.isEmpty()) {
                        formatExcluded = filter.ExcludedFormats.contains(commonFormat);
                    }
                    return !(formatNotIncluded || formatExcluded);
                }
                return true;
            }).collect(Collectors.toList());
        }

        return filteredList;
    }

    public Map<Integer, String> GetBookItemFormats() {
        var sharedService = this.sharedService.getValue();
        Enums.BookFormat[] formats = Enums.BookFormat.values();
        Map<Integer, String> formatMap = new HashMap<>();
        for (Enums.BookFormat format : formats) {
            formatMap.put(format.ordinal(), sharedService.GetSeperatedString(format.toString()));
        }
        return formatMap;
    }

    public Map<Integer, String> GetBookTypes() {
        var sharedService = this.sharedService.getValue();
        Enums.BookType[] types = Enums.BookType.values();
        Map<Integer, String> typeMap = new HashMap<>();
        for (Enums.BookType type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    }

    public List<Publisher> GetPublishers() {
        var publishers = _BookRepo.GetPublishers();
        publishers.sort(Comparator.comparing(o -> o.Name));
        return publishers;
    }

    public boolean AddPublisher(Publisher publisher) {
        return _BookRepo.AddPublisher(publisher);
    }

    public boolean UpdatePublisher(Publisher publisher) {
        return _BookRepo.UpdatePublisher(publisher);
    }

    public boolean DeletePublisher(String id) {
        return _BookRepo.DeletePublisher(id);
    }

    public Enums.BookFormat GetCommonBookFormat(List<BookItem> items) {
        Optional<Enums.BookFormat> mostCommonCity = items.stream()
                .map(BookItem::getFormat)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
        return mostCommonCity.orElse(items.stream().count() > 0 ? items.get(0).getFormat() : null);
    }
}
