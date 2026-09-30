package models.book;

import models.shared.MediaItem;

import java.util.List;

public class Book extends MediaItem {
    public String Author;
    public String Artist;
    public Enums.BookType Type;
    public String Publisher;
    public List<BookItem> Items;
}
