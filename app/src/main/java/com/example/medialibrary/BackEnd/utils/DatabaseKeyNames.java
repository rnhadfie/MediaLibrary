package com.example.medialibrary.backend.utils;

public class DatabaseKeyNames {
    //region Table names
    public static final String TABLE_TAGS = "Tags";
    public static final String TABLE_PUBLISHERS = "Publishers";
    public static final String TABLE_BOOKS = "Books";
    public static final String TABLE_BOOK_ITEMS = "BookItems";
    public static final String TABLE_MUSIC = "Music";
    public static final String TABLE_VIDEOS = "Videos";
    public static final String TABLE_VIDEO_ITEMS = "VideoItems";
    public static final String TABLE_OTHERS = "Other";
    public static final String TABLE_OTHER_ITEMS = "OtherItems";

    //endregion

    //region Common Column names

    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_COLLECTING = "Collecting";
    public static final String COLUMN_ONGOING = "Ongoing";
    public static final String COLUMN_COLLECTED = "Collected";
    public static final String COLUMN_TAG = "Tag";
    public static final String COLUMN_COVER = "Cover";
    public static final String COLUMN_PUBLISHER = "Publisher";
    public static final String COLUMN_GENRE = "Genre";

    public static final String COLUMN_SERIES = "Series";
    public static final String COLUMN_OWNED = "Owned";
    public static final String COLUMN_ITEM_COVER = "ItemCover";

    public static final String COLUMN_ARTIST = "Artist";
    public static final String COLUMN_TYPE = "Type";

    public static final String COLUMN_FORMAT = "Format";

    public static final String COLUMN_NAME = "Name";

    public static final String COLUMN_COLLECTING_PRIORITY = "CollectingPriority";

    //endregion


    //region Book Column names

    public static final String COLUMN_AUTHOR = "Author";

    //endregion

    //region BookItem Column names

    public static final String COLUMN_VOLUME_NUMBER = "VolumeNumber";
    public static final String COLUMN_VOLUME_TITLE = "VolumeTitle";
    public static final String COLUMN_READ = "Read";

    //endregion

    //region Music Column names

    public static final String COLUMN_YEAR = "Year";

    //endregion

    //region Video Column names

    public static final String COLUMN_DISC_NUMBER = "DiscNumber";
    public static final String COLUMN_DISC_TITLE = "DiscTitle";
    public static final String COLUMN_WATCHED = "Watched";
    public static final String COLUMN_VIDEO_TAG = "VideoTag";

    //endregion
}
