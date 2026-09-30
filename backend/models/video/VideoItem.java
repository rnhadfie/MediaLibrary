package models.video;

import android.annotation.SuppressLint;

public class VideoItem {
    public String Id;
    public String Series;
    public int Season;
    public String DiscTitle;
    public boolean Watched;
    public boolean Owned;
    public Enums.VideoFormat Format;
    public byte[] ItemCover;

    @SuppressLint("DefaultLocale")
    public String GetSeason()
    {
        return String.format("%d", Season);
    }
}
