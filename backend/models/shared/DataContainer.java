package models.shared;

import models.book.Book;
import models.book.Publisher;
import models.music.Music;
import models.music.OldMusic;
import models.other.Other;
import models.video.Video;

import java.util.List;

public class DataContainer {
    public List<Tag> Tags;
    public List<Publisher> Publishers;
    public List<Book> Books;
    public List<Music> Music;
    public List<OldMusic> OldMusicModelList; //for moving from versions 1 to 2, remove in later releases
    public List<Video> Videos;
    public List<Other> Others;
}
