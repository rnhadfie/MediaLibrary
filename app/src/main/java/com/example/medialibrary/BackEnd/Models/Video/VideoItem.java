package com.example.medialibrary.backend.models.video;

public class VideoItem {
    public String Id;
    public String Series;
    public int Season;
    public String DiscTitle;
    public boolean Watched;
    public boolean Owned;
    public Enums.VideoFormat Format;
    public byte[] ItemCover;
}
