package com.example.medialibrary.backend.models.book;

import com.example.medialibrary.backend.models.shared.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookSetup {
    public BookSetup() {

    }
    public Map<Integer, String> Genre = new HashMap<>();
    public List<Publisher> Publishers = new ArrayList<>();
    public List<Tag> Tag = new ArrayList<>();
    public Map<Integer, String> Type = new HashMap<>();
    public Map<Integer, String> Format = new HashMap<>();
}
