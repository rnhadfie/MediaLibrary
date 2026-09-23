package com.example.medialibrary.backend.models.shared;

import java.util.ArrayList;
import java.util.List;

public class MediaItem {
    public int Id;
    public Enums.MediaType MediaType;
    public String Title;
    public List<Integer> Genre = new ArrayList<>();
    public Boolean Collecting;
    public Boolean HasSeriesEnded;
    public Boolean HasCollectedAllItems;
    public Boolean CurrentOwnAny;
    public int Tag;
    public byte[] Cover;

}
