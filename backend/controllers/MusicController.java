package controllers;

import serivce.MusicService;
import models.music.Music;
import models.music.MusicFilter;
import models.music.MusicObj;
import models.music.MusicSetup;
import models.shared.DisplayMediaItem;
import repository.database.MediaLibraryDbHelper;

import java.util.List;

import kotlin.Lazy;
import kotlin.LazyKt;

public class MusicController  extends BaseController{
    public Lazy<MusicService> musicSerivce;
    public MusicController(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.musicSerivce = LazyKt.lazy(() -> new MusicService(dbHelper));
    }

    public MusicController() {
    }
    public List<Music> GetMusics(MusicFilter filter) {
        //Get All Books
        return this.musicSerivce.getValue().GetMusics(filter);
    }

    public List<DisplayMediaItem> GetListOfCds(MusicFilter filter) {
        //Get All Books
        return this.musicSerivce.getValue().GetMusicDisplayLists(filter);
    }

    public Music GetMusic(String id) {
        return this.musicSerivce.getValue().GetCd(id);
    }

    public boolean AddMusic(MusicObj music) {
        return this.musicSerivce.getValue().AddMusic(music);
    }

    public boolean UpdateMusic(MusicObj music) {
        return this.musicSerivce.getValue().EditMusic(music);
    }
    public boolean DeleteMusic(String id) {
        return this.musicSerivce.getValue().DeleteMusic(id);
    }

    public MusicSetup GetMusicSetup () {
        return this.musicSerivce.getValue().GetSetup();
    }


}
