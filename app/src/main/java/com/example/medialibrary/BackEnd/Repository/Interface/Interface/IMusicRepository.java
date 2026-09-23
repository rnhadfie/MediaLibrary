package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.music.MusicObj;

import java.util.List;

public interface IMusicRepository {
    List<Music> GetMusic();
    List<Music>  GetMusic(String whereClause, List<String> selectionArgs);
    Music GetMusic(int id);
    boolean AddMusic(MusicObj musicObj);
    boolean DeleteMusic(int id);
    boolean UpdateMusic(MusicObj musicObj);
}
