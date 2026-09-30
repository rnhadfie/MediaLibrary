package controllers;

import serivce.VideoService;
import models.shared.DisplayMediaItem;
import models.video.*;
import repository.database.MediaLibraryDbHelper;

import java.util.List;

import kotlin.Lazy;
import kotlin.LazyKt;

public class VideoController extends BaseController {
    public Lazy<VideoService> videoSerivce;
    public VideoController(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.videoSerivce = LazyKt.lazy(() -> new VideoService(dbHelper));
    }

    public VideoController() {
    }
    public List<Video> GetVideos(VideoFilter filter) {
        //Get All Books
        return this.videoSerivce.getValue().GetVideos(filter);
    }

    public List<DisplayMediaItem> GetListOfVideos(VideoFilter filter) {
        //Get All Books
        return this.videoSerivce.getValue().GetVideoDisplayLists(filter);
    }

    public Video GetVideo(String id) {
        return this.videoSerivce.getValue().GetVideo(id);
    }

    public boolean AddVideo(VideoSaveObject video) {
        return this.videoSerivce.getValue().AddVideo(video);
    }

    public boolean UpdateVideo(VideoSaveObject video) {
        return this.videoSerivce.getValue().EditVideo(video);
    }
    public boolean DeleteVideo(String id) {
        return this.videoSerivce.getValue().DeleteVideo(id);
    }

    public VideoSetup GetVideoSetup () {
        return this.videoSerivce.getValue().GetVideoSetup();
    }


}
