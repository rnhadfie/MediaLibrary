package models.video;

import models.shared.MediaItem;

import java.util.List;

public class Video extends MediaItem {
    public Enums.VideoType Type;
    public List<VideoItem> Items;
    public Enums.VideoTag VideoTag;
}
