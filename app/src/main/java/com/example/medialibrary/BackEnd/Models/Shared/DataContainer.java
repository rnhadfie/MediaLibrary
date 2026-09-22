package com.example.medialibrary.backend.models.shared;

import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.Publisher;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.video.Video;

import java.util.List;

public class DataContainer {
    public List<Tag> Tags;
    public List<Publisher> Publishers;
    public List<Book> Books;
    public List<Music> Music;
    public List<Video> Videos;
    public List<Other> Others;
}
