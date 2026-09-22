package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.music.MusicObj;
import com.example.medialibrary.backend.models.shared.Filter;

import java.util.List;

public interface IMusicRepository {
    <T extends Filter> List<Music>  GetMusic(T filter);
    Music GetMusic(int id);

    boolean AddMusic(MusicObj musicObj);

    boolean DeleteMusic(int id);
    boolean UpdateMusic(MusicObj musicObj);
}
