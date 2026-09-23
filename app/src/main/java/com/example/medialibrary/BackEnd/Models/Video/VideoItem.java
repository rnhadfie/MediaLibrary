package com.example.medialibrary.backend.models.video;

public class VideoItem {
    public int Id;
    public int Series;
    public int DiscNumber;
    public String DiscTitle;
    public boolean Watched;
    public boolean Owned;
    public Enums.VideoFormat Format;
    public byte[] ItemCover;
}
