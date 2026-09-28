package com.example.medialibrary.backend.models.shared;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    public String Search;
    public int Tag;
    public int Genre;
    public Enums.MediaType MediaType;
    public Boolean Collecting;
    public Boolean Ongoing;
    public Boolean AnyOwned;
    public Boolean Collected;

    public List<String> IncludedTags = new ArrayList<>();
    public List<String> ExcludedTags = new ArrayList<>();

    public List<Integer> IncludedGenres = new ArrayList<>();
    public List<Integer> ExcludedGenres = new ArrayList<>();

    public List<Enums.MediaType> IncludedMediaTypes = new ArrayList<>();
    public List<Enums.MediaType> ExcludedMediaTypes = new ArrayList<>();

    public Boolean SortAlphabetical = true;
    public Boolean SortPriority = null;
    public Boolean SortItemMediaType = null;
}
