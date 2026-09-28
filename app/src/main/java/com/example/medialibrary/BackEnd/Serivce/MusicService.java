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

    private final MusicRepository _MusicRepo;
    private final SharedService _SharedService;

    public MusicService(MediaLibraryDbHelper dbHelper) {
        this.musicRepository = LazyKt.lazy(() -> new MusicRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));

        _MusicRepo = musicRepository.getValue();
        _SharedService = sharedService.getValue();
    }

    public List<Music> GetMusics(MusicFilter filter) {
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = this.sharedService.getValue().BuildWhereClause(filter, selectionArgs);
        var music = _MusicRepo.GetMusic(whereClause, selectionArgs);
        music = _SharedService.Sort(filter, music);
        return music;
    }

    public List<DisplayMediaItem> GetMusicDisplayLists(MusicFilter filter) {


        List<String> selectionArgs = new ArrayList<>();
        String whereClause = _SharedService.BuildWhereClause(filter, selectionArgs);
        List<Music> music = _MusicRepo.GetMusic(whereClause, selectionArgs);
        music = _SharedService.Sort(filter, music);
        return _SharedService.mapToDisplayItems(music, false);
    }


    public Music GetCd(String id) {
        return _MusicRepo.GetMusic(id);
    }

    public boolean AddMusic(MusicObj musicObj) {
        return _MusicRepo.AddMusic(musicObj);
    }

    public boolean EditMusic(MusicObj musicObj) {
        return _MusicRepo.UpdateMusic(musicObj);
    }

    public boolean DeleteMusic(String id) {
        return _MusicRepo.DeleteMusic(id);
    }

    public MusicSetup GetSetup () {
        var musicSetup = new MusicSetup();
        musicSetup.MusicGenre = this.GetMusicGenres();
        musicSetup.Tags = this.sharedService.getValue().GetTags();
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
