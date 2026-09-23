package com.example.medialibrary.backend.models.video;

import com.example.medialibrary.backend.models.shared.Filter;
import java.util.ArrayList;
import java.util.List;

public class VideoFilter extends Filter {
    public Enums.VideoType Type;
    public Enums.VideoTag VideoTag;

    public List<Enums.VideoType> IncludedTypes = new ArrayList<>();
    public List<Enums.VideoType> ExcludedTypes = new ArrayList<>();

    public List<Enums.VideoTag> IncludedVideoTags = new ArrayList<>();
    public List<Enums.VideoTag> ExcludedVideoTags = new ArrayList<>();
}
