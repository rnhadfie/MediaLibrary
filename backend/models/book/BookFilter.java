package models.book;

import models.shared.Filter;
import java.util.ArrayList;
import java.util.List;

public class BookFilter extends Filter {
    public Enums.BookType Type;
    public int Publisher;

    public Boolean Read;
    public Boolean Reading;

    public List<Enums.BookType> IncludedTypes = new ArrayList<>();
    public List<Enums.BookType> ExcludedTypes = new ArrayList<>();

    public List<String> IncludedPublishers = new ArrayList<>();
    public List<String> ExcludedPublishers = new ArrayList<>();

    public List<Enums.BookFormat> IncludedFormats = new ArrayList<>();
    public List<Enums.BookFormat> ExcludedFormats = new ArrayList<>();
}
