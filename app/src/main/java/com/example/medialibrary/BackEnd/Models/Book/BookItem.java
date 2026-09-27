package com.example.medialibrary.backend.models.book;

public class BookItem {
    public int Id;
    public int Series;
    public String VolumeNumber;
    public String VolumeTitle;
    public boolean Read;
    public boolean Owned;
    public Enums.BookFormat Format;
    public byte[] ItemCover;

    public Enums.BookFormat getFormat() { return Format; }

    public String GetVolumeNumber() { return  VolumeNumber; }
}
