package repository.Interface.Interface;

import models.music.*;
import java.util.List;

public interface IMusicRepository {
    List<Music> GetMusic();
    List<Music>  GetMusic(String whereClause, List<String> selectionArgs);
    Music GetMusic(String id);
    boolean AddMusic(MusicObj musicObj);
    boolean DeleteMusic(String id);
    boolean UpdateMusic(MusicObj musicObj);
}
