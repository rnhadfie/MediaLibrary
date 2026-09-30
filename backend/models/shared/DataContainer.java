package models.shared;

import models.book.Book;
import models.book.Publisher;
import models.music.Music;
import models.other.Other;
import models.video.Video;

import java.util.List;

public class DataContainer {
    public List<Tag> Tags;
    public List<Publisher> Publishers;
    public List<Book> Books;
    public List<Music> Music;
    public List<Video> Videos;
    public List<Other> Others;
}
