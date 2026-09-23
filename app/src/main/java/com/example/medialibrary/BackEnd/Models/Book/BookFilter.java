package com.example.medialibrary.backend.models.book;

import com.example.medialibrary.backend.models.shared.Filter;
import java.util.ArrayList;
import java.util.List;

public class BookFilter extends Filter {
    public Enums.BookType Type;
    public int Publisher;
    public Enums.BookFormat PrimaryFormat;

    public List<Enums.BookType> IncludedTypes = new ArrayList<>();
    public List<Enums.BookType> ExcludedTypes = new ArrayList<>();

    public List<Integer> IncludedPublishers = new ArrayList<>();
    public List<Integer> ExcludedPublishers = new ArrayList<>();

    public List<Enums.BookFormat> IncludedFormats = new ArrayList<>();
    public List<Enums.BookFormat> ExcludedFormats = new ArrayList<>();
}
