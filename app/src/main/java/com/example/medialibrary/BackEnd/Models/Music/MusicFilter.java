package com.example.medialibrary.backend.models.music;

import com.example.medialibrary.backend.models.shared.Filter;
import java.util.ArrayList;
import java.util.List;

public class MusicFilter extends Filter {
    public int MusicGenre;

    public List<Integer> IncludedMusicGenres = new ArrayList<>();
    public List<Integer> ExcludedMusicGenres = new ArrayList<>();
}
