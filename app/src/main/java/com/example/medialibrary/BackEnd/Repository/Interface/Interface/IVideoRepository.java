package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.shared.Filter;
import com.example.medialibrary.backend.models.video.Video;
import com.example.medialibrary.backend.models.video.VideoSaveObject;

import java.util.List;

public interface IVideoRepository {

    <T extends Filter> List<Video> GetVideos(T filter);
    Video GetVideo(int id);

    boolean AddVideo(VideoSaveObject bookObj);
    boolean UpdateVideo(VideoSaveObject bookObj);
    boolean DeleteVideo(int id);
}
