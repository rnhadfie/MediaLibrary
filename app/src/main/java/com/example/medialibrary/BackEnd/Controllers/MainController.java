package com.example.medialibrary.backend.controllers;

import com.example.medialibrary.backend.Serivce.MainService;
import com.example.medialibrary.backend.models.shared.*;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.List;

import kotlin.Lazy;
import kotlin.LazyKt;

public class MainController {

    public Lazy<MainService> mainSerivce;
    protected MediaLibraryDbHelper dbHelper;

    public MainController() {

    }
    public MainController(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.mainSerivce = LazyKt.lazy(() -> new MainService(dbHelper));
    }

    public List<DisplayMediaItem> GetAllItems(Filter  filter) {
        return this.mainSerivce.getValue().GetDisplayList(filter);
    }

    public MediaItem GetItem(int id) {
       return null;
    }

    public List<MediaItem> GetMediaItems(Filter  filter)
    {
        return this.mainSerivce.getValue().GetAllItems(filter);
    }

    public MainSetup GetSetup() {
        return this.mainSerivce.getValue().GetSetup();
    }

    public List<Tag> GetTags() {
        return this.mainSerivce.getValue().GetTags();
    }

    public boolean AddTag(Tag tag) {
        return this.mainSerivce.getValue().AddTag(tag);
    }

    public boolean UpdateTag(Tag tag) {
        return this.mainSerivce.getValue().UpdateTag(tag);
    }

    public boolean DeleteTag(int id) {
        return this.mainSerivce.getValue().DeleteTag(id);
    }
}
