package repository.Interface.Interface;

import models.video.*;
import java.util.List;

public interface IVideoRepository {
    List<Video> GetVideos();
    List<Video> GetVideos(String whereClause, List<String> selectionArgs);
    Video GetVideo(String id);
    boolean AddVideo(VideoSaveObject bookObj);
    boolean UpdateVideo(VideoSaveObject bookObj);
    boolean DeleteVideo(String id);
}
