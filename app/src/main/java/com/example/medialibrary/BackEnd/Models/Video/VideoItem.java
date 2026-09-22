package com.example.medialibrary.backend.models.video;

import com.example.medialibrary.backend.models.video.Enums;

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
