package com.example.medialibrary.backend.controllers;

import com.example.medialibrary.backend.Serivce.MainSerivce;
import com.example.medialibrary.backend.Serivce.MusicService;
import com.example.medialibrary.backend.Serivce.OtherService;
import com.example.medialibrary.backend.Serivce.SharedService;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.music.MusicFilter;
import com.example.medialibrary.backend.models.music.MusicObj;
import com.example.medialibrary.backend.models.music.MusicSetup;
import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.other.OtherFitler;
import com.example.medialibrary.backend.models.other.OtherSaveObj;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.models.shared.MainSetup;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.List;

import kotlin.Lazy;
import kotlin.LazyKt;

public class OtherController {
    public Lazy<OtherService> otherSerivce;
    public Lazy<MainSerivce> mainService;
    protected MediaLibraryDbHelper dbHelper;
    public OtherController(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.otherSerivce = LazyKt.lazy(() -> new OtherService(dbHelper));
        this.mainService = LazyKt.lazy(() -> new MainSerivce(dbHelper));
    }

    public OtherController() {
    }
    public List<Other> GetOtherCollections(Filter filter) {
        //Get All Books
        return this.otherSerivce.getValue().GetBooks(filter);
    }

    public List<DisplayMediaItem> GetListOfOtherCollections(Filter filter) {
        //Get All Books
        return this.otherSerivce.getValue().GetBookDisplayLists(filter);
    }

    public Other GetOtherItem(int id) {
        Other other = new Other();
        other = this.otherSerivce.getValue().GetOtherCollection(id);
        return other;
    }

    public boolean AddOther(OtherSaveObj otherObj) {
        return this.otherSerivce.getValue().AddOtherCollection(otherObj);
    }

    public boolean UpdateOther(OtherSaveObj otherObj) {
        return this.otherSerivce.getValue().EditOtherCollection(otherObj);
    }
    public boolean DeleteOther(int id) {
        return this.otherSerivce.getValue().DeleteOtherCollection(id);
    }

    public MainSetup GetSetup () {
        return this.mainService.getValue().GetSetup();
    }
}
