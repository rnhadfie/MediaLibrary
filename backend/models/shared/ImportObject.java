package models.shared;

import models.book.*;
import models.music.*;
import models.other.*;
import models.video.*;

import java.util.List;

public class ImportObject {
    public List<Tag> TagsToImport;
    public List<Publisher> PublishersToImport;
    public List<Book> BooksToImport;
    public List<Music> MusicToImport;
    public List<Video> MoviesToImport;
    public List<Other> OtherMediaToImport;
    public List<BookItem> BookItemsToImport;
    public List<VideoItem> MovieItemsToImport;
    public List<OtherItem> OtherMediaItemsToImport;

}
