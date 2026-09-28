package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.video.Video;
import com.example.medialibrary.backend.models.video.VideoSaveObject;

import java.util.List;

public interface IVideoRepository {
    List<Video> GetVideos();
    List<Video> GetVideos(String whereClause, List<String> selectionArgs);
    Video GetVideo(String id);
    boolean AddVideo(VideoSaveObject bookObj);
    boolean UpdateVideo(VideoSaveObject bookObj);
    boolean DeleteVideo(String id);
}
