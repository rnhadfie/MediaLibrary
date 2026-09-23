package com.example.medialibrary.backend.models.book;

import com.example.medialibrary.backend.models.shared.MediaItem;

import java.util.List;

public class Book extends MediaItem {
    public String Author;
    public String Artist;
    public Enums.BookType Type;
    public int Publisher;
    public List<BookItem> Items;
}
