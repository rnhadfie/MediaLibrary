package com.example.medialibrary.backend.models.shared;

import com.example.medialibrary.backend.models.book.*;
import com.example.medialibrary.backend.models.music.*;
import com.example.medialibrary.backend.models.other.*;
import com.example.medialibrary.backend.models.video.*;

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
