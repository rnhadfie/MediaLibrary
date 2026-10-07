package models.book;

import models.shared.GenreObject;
import models.shared.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookSetup {
    public BookSetup() {

    }
    public List<GenreObject> Genre = new ArrayList<>();
    public List<Publisher> Publishers = new ArrayList<>();
    public List<Tag> Tag = new ArrayList<>();
    public Map<Integer, String> Type = new HashMap<>();
    public Map<Integer, String> Format = new HashMap<>();
    public Map<Integer, String> Demographics = new HashMap<>();
}
