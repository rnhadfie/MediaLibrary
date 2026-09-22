package com.example.medialibrary.backend.models.book;

import com.example.medialibrary.backend.models.shared.Filter;

public class BookFilter extends Filter {
    public Enums.BookType Type;
    public int Publisher;
    public Enums.BookFormat PrimaryFormat;

}
