package com.example.medialibrary.backend.Serivce;

import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.music.MusicFilter;
import com.example.medialibrary.backend.models.music.MusicObj;
import com.example.medialibrary.backend.models.music.MusicSetup;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;

import com.example.medialibrary.backend.repository.MusicRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;
import com.example.medialibrary.backend.models.music.Enums.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.Lazy;
import kotlin.LazyKt;

public class MusicService {

    public Lazy<MusicRepository> musicRepository;
    public Lazy<SharedService> sharedService;

    public MusicService(MediaLibraryDbHelper dbHelper) {
        this.musicRepository = LazyKt.lazy(() -> new MusicRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));
    }

    public List<Music> GetMusics(MusicFilter filter) {
        var repo = this.musicRepository.getValue();
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = this.sharedService.getValue().BuildWhereClause(filter, selectionArgs);
        return repo.GetMusic(whereClause, selectionArgs);
    }

    public List<DisplayMediaItem> GetMusicDisplayLists(MusicFilter filter) {
        var repo = this.musicRepository.getValue();
        var sharedService = this.sharedService.getValue();

        List<String> selectionArgs = new ArrayList<>();
        String whereClause = sharedService.BuildWhereClause(filter, selectionArgs);
        List<Music> music = repo.GetMusic(whereClause, selectionArgs);

        return sharedService.mapToDisplayItems(music, false);
    }


    public Music GetCd(String id) {
        var repo = this.musicRepository.getValue();
        return repo.GetMusic(id);

    }

    public boolean AddMusic(MusicObj musicObj) {
        var repo = this.musicRepository.getValue();
        return repo.AddMusic(musicObj);
    }

    public boolean EditMusic(MusicObj musicObj) {
        var repo = this.musicRepository.getValue();
        return repo.UpdateMusic(musicObj);
    }

    public boolean DeleteMusic(String id) {
        var repo = this.musicRepository.getValue();
        return repo.DeleteMusic(id);
    }

    public MusicSetup GetSetup () {
        var musicSetup = new MusicSetup();
        musicSetup.MusicGenre = this.GetMusicGenres();
        musicSetup.Tags = this.sharedService.getValue().GetTags();
        //get tags
        return musicSetup;
    }



    public Map<Integer,String> GetMusicGenres() {
        var sharedService = this.sharedService.getValue();
        MusicGenre[] mGenres = MusicGenre.values();
        Map<Integer,String> mGanreMap = new HashMap<>();
        for (MusicGenre mGenre : mGenres) {
            mGanreMap.put(mGenre.ordinal(), sharedService.GetSeperatedString(mGenre.toString()));
        }
        return mGanreMap;
    }

}
