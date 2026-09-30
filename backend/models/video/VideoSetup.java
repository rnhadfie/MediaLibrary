package models.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.shared.*;

public class VideoSetup {
    public Boolean Watched;
    public Boolean Watching;

    public List<GenreObject> Genre = new ArrayList<>();
    public Map<Integer, String> VideoTags = new HashMap<>();
    public Map<Integer, String> Types = new HashMap<>();
    public Map<Integer, String> Formats = new HashMap<>();
    public List<Tag> Tag = new ArrayList<>();
}
