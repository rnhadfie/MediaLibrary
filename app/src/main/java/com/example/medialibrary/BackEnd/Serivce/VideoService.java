package com.example.medialibrary.backend.Serivce;

import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.video.Enums.*;
import com.example.medialibrary.backend.models.video.Video;
import com.example.medialibrary.backend.models.video.VideoFilter;
import com.example.medialibrary.backend.models.video.VideoSaveObject;
import com.example.medialibrary.backend.models.video.VideoSetup;
import com.example.medialibrary.backend.repository.VideoRepository;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.Lazy;
import kotlin.LazyKt;

public class VideoService {

    public Lazy<VideoRepository> videoRepository;
    public Lazy<SharedService> sharedService;

    private final VideoRepository _VideoRepo;
    private final SharedService _SharedService;

    public VideoService(MediaLibraryDbHelper dbHelper) {
        this.videoRepository = LazyKt.lazy(() -> new VideoRepository(dbHelper));
        this.sharedService = LazyKt.lazy(() -> new SharedService(dbHelper));

        _VideoRepo = videoRepository.getValue();
        _SharedService = sharedService.getValue();
    }

    public List<Video> GetVideos(VideoFilter filter) {
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = this.sharedService.getValue().BuildWhereClause(filter, selectionArgs);

        List<Video> videos = _VideoRepo.GetVideos(whereClause, selectionArgs);
        videos = _SharedService.Sort(filter, videos);

        return VideoItemBasedFilters(videos, filter);
    }

    public List<DisplayMediaItem> GetVideoDisplayLists(VideoFilter filter) {
        List<String> selectionArgs = new ArrayList<>();
        String whereClause = _SharedService.BuildWhereClause(filter, selectionArgs);

        List<Video> videos = _VideoRepo.GetVideos(whereClause, selectionArgs);
        videos = _SharedService.Sort(filter, videos);

        return _SharedService.mapToDisplayItems(VideoItemBasedFilters(videos, filter), false);
    }

    public Video GetVideo(String id) {
        return _VideoRepo.GetVideo(id);
    }

    public boolean AddVideo(VideoSaveObject video) {
        return _VideoRepo.AddVideo(video);
    }

    public boolean EditVideo(VideoSaveObject video) {
        return _VideoRepo.UpdateVideo(video);
    }

    public boolean DeleteVideo(String id) {
        return _VideoRepo.DeleteVideo(id);
    }

    public VideoSetup GetVideoSetup() {
        var videoSetup = new VideoSetup();
        var sharedService = this.sharedService.getValue();
        videoSetup.VideoTags = this.GetVideoTags();
        videoSetup.Genre = sharedService.GetGenres();
        videoSetup.Types = this.GetTypes();
        videoSetup.Tag = this.sharedService.getValue().GetTags();
        videoSetup.Formats = this.GetFormats();
        return videoSetup;
    }

    public Map<Integer, String> GetFormats() {
        var sharedService = this.sharedService.getValue();
        VideoFormat[] formats = VideoFormat.values();
        Map<Integer, String> formatMap = new HashMap<>();
        for (VideoFormat format : formats) {
            formatMap.put(format.ordinal(), sharedService.GetSeperatedString(format.toString()));
        }
        return formatMap;
    }

    public Map<Integer, String> GetTypes() {
        var sharedService = this.sharedService.getValue();
        VideoType[] types = VideoType.values();
        Map<Integer, String> typeMap = new HashMap<>();
        for (VideoType type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    }

    public Map<Integer, String> GetVideoTags() {
        var sharedService = this.sharedService.getValue();
        VideoTag[] types = VideoTag.values();
        Map<Integer, String> typeMap = new HashMap<>();
        for (VideoTag type : types) {
            typeMap.put(type.ordinal(), sharedService.GetSeperatedString(type.toString()));
        }
        return typeMap;
    }

    public List<Video> VideoItemBasedFilters(List<Video> listOfVideos, VideoFilter filter) {
        if (listOfVideos == null || listOfVideos.isEmpty()) {
            return new ArrayList<>();
        }

        for (Video video : listOfVideos) {
            if (video.Items != null && !video.Items.isEmpty()) {
                boolean hasOwned = false;
                for (var item : video.Items) {
                    if (item.Owned) {
                        hasOwned = true;
                        break;
                    }
                }
                video.CurrentOwnAny = hasOwned;
            } else {
                video.CurrentOwnAny = false;
            }
        }

        List<Video> filteredList = new ArrayList<>();
        for (Video video : listOfVideos) {
            if (filter != null) {
                if (filter.AnyOwned != null && video.CurrentOwnAny != filter.AnyOwned) {
                    continue;
                }

                if (filter.Watched != null) {
                    boolean allWatched = video.Items != null && !video.Items.isEmpty() && video.Items.stream().allMatch(i -> i.Watched);
                    boolean isComplete = Boolean.TRUE.equals(video.Ongoing) || Boolean.TRUE.equals(video.HasCollectedAllItems);
                    boolean isWatched = isComplete && allWatched;
                    if (isWatched != filter.Watched) {
                        continue;
                    }
                }

                if (filter.Watching != null) {
                    boolean anyWatched = video.Items != null && video.Items.stream().anyMatch(i -> i.Watched);
                    boolean allWatched = video.Items != null && !video.Items.isEmpty() && video.Items.stream().allMatch(i -> i.Watched);
                    boolean isComplete = Boolean.TRUE.equals(video.Ongoing) || Boolean.TRUE.equals(video.HasCollectedAllItems);
                    boolean isWatched = isComplete && allWatched;
                    boolean isWatching = anyWatched && !isWatched;
                    if (isWatching != filter.Watching) {
                        continue;
                    }
                }
            }

            filteredList.add(video);
        }

        return filteredList;
    }
}
