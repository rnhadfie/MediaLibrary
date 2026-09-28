package com.example.medialibrary.backend.models.shared;

import java.util.ArrayList;
import java.util.List;

public class MediaItem {
    public String Id;
    public Enums.MediaType MediaType;
    public String Title;
    public List<Integer> Genre = new ArrayList<>();
    public Boolean Collecting;
    public Boolean Ongoing;
    public Boolean HasCollectedAllItems;
    public Boolean CurrentOwnAny;
    public String Tag;
    public byte[] Cover;
    public Enums.CollectingPriority CollectingPriority;

    public String getTitle() {return Title != null ? Title : "";
    }

    public String getMediaType() {
        return MediaType != null ? MediaType.name() : "";
    }
}
