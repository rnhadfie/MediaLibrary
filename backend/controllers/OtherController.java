package controllers;

import serivce.MainService;
import serivce.OtherService;
import models.other.Other;
import models.other.OtherSaveObj;
import models.shared.DisplayMediaItem;
import models.shared.Filter;
import models.shared.MainSetup;
import repository.database.MediaLibraryDbHelper;

import java.util.List;

import kotlin.Lazy;
import kotlin.LazyKt;

public class OtherController extends BaseController{
    public Lazy<OtherService> otherSerivce;
    public Lazy<MainService> mainService;
    public OtherController(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.otherSerivce = LazyKt.lazy(() -> new OtherService(dbHelper));
        this.mainService = LazyKt.lazy(() -> new MainService(dbHelper));
    }

    public OtherController() {
    }

    public List<DisplayMediaItem> GetListOfOtherCollections(Filter filter) {
        //Get All Books
        return this.otherSerivce.getValue().GetOtherDisplayLists(filter);
    }

    public Other GetOtherItem(String id) {
        return this.otherSerivce.getValue().GetOtherCollection(id);
    }

    public boolean AddOther(OtherSaveObj otherObj) {
        return this.otherSerivce.getValue().AddOtherCollection(otherObj);
    }

    public boolean UpdateOther(OtherSaveObj otherObj) {
        return this.otherSerivce.getValue().EditOtherCollection(otherObj);
    }
    public boolean DeleteOther(String id) {
        return this.otherSerivce.getValue().DeleteOtherCollection(id);
    }

    public MainSetup GetSetup () {
        return this.mainService.getValue().GetSetup();
    }
}
