package com.example.medialibrary.backend.models.shared;

public class DisplayMediaItem {
    public int Id;
    public String Title;
    public boolean Collecting;
    public boolean ToCollect;

    public boolean CollectedOrOnGoing;
    public Enums.MediaType MediaType;
    public byte[] Cover;
}
