package com.example.medialibrary.backend.Serivce;

import com.example.medialibrary.backend.models.video.Enums.*;
import com.example.medialibrary.backend.models.book.Publisher;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.shared.Enums;
import com.example.medialibrary.backend.models.video.Video;
import com.example.medialibrary.backend.models.video.VideoFilter;
import com.example.medialibrary.backend.models.video.VideoSaveObject;
import com.example.medialibrary.backend.models.video.VideoSetup;
import com.example.medialibrary.backend.repository.BookRepository;
import com.example.medialibrary.backend.repository.VideoRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.Lazy;
import kotlin.LazyKt;

public class VideoService {
    private final MediaLibraryDbHelper dbHelper;

    public Lazy<VideoRepository> videoRepository;
    public Lazy<SharedService> sharedService;

    public VideoService(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.videoRepository = LazyKt.lazy(() -> new VideoRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));
    }

    public List<Video> GetVideos(VideoFilter filter) {
        var repo = this.videoRepository.getValue();
        List<Video> videos = repo.GetVideos(filter);
        return videos;
    }

    public List<DisplayMediaItem> GetVideoDisplayLists(VideoFilter filter) {
        var repo = this.videoRepository.getValue();
        var videos =repo.GetVideos(filter);
        var sharedService = this.sharedService.getValue();

        return sharedService.mapToDisplayItems(videos);
    }


    public Video GetVideo(int id) {
        var repo = this.videoRepository.getValue();
        return repo.GetVideo(id);

    }

    public boolean AddVideo(VideoSaveObject video) {
        var repo = this.videoRepository.getValue();
        return repo.AddVideo(video);
    }

    public boolean EditVideo(VideoSaveObject video) {
        var repo = this.videoRepository.getValue();
        return repo.UpdateVideo(video);
    }

    public boolean DeleteVideo(int id) {
        var repo = this.videoRepository.getValue();
        return repo.DeleteVideo(id);
    }

    public VideoSetup GetVideoSetup () {
        var videoSetup = new VideoSetup();
        var sharedService = this.sharedService.getValue();
        videoSetup.VideoTags = this.GetVideoTags();
        videoSetup.Genre = sharedService.GetGenres();
        videoSetup.Types = this.GetTypes();
        videoSetup.Tag = this.sharedService.getValue().GetTags();
        videoSetup.Formats = this.GetFormats();
        return videoSetup;
    }




    public Map<Integer,String> GetFormats() {
        var sharedService = this.sharedService.getValue();
        VideoFormat[] formats = VideoFormat.values();
        Map<Integer,String> formatMap = new HashMap<>();
        for (VideoFormat format : formats) {
            formatMap.put(format.ordinal(), sharedService.GetSeperatedString(format.toString()));
        }
        return formatMap;
    };



    public Map<Integer,String> GetTypes() {
        var sharedService = this.sharedService.getValue();
        VideoType[] types = VideoType.values();
        Map<Integer,String> typeMap = new HashMap<>();
        for (VideoType type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    };

    public Map<Integer,String> GetVideoTags() {
        var sharedService = this.sharedService.getValue();
        VideoTag[] types = VideoTag.values();
        Map<Integer,String> typeMap = new HashMap<>();
        for (VideoTag type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    };

}
