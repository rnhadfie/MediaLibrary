package com.example.medialibrary.backend.models.shared;

import com.example.medialibrary.backend.models.book.Publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainSetup {

    public Map<Integer, String> Genre = new HashMap<>();
    public Map<Integer, String> MediaType = new HashMap<>();
    public List<Tag> Tag = new ArrayList<>();
}
