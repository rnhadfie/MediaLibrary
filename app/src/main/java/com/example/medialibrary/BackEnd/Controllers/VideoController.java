package com.example.medialibrary.backend.controllers;

import com.example.medialibrary.backend.Serivce.BookService;
import com.example.medialibrary.backend.Serivce.VideoService;
import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.BookFilter;
import com.example.medialibrary.backend.models.book.BookSaveObject;
import com.example.medialibrary.backend.models.book.BookSetup;
import com.example.medialibrary.backend.models.book.Publisher;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.models.video.Video;
import com.example.medialibrary.backend.models.video.VideoFilter;
import com.example.medialibrary.backend.models.video.VideoSaveObject;
import com.example.medialibrary.backend.models.video.VideoSetup;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.List;

import kotlin.Lazy;
import kotlin.LazyKt;

public class VideoController {
    public Lazy<VideoService> videoSerivce;
    protected MediaLibraryDbHelper dbHelper;
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

    public Video GetVideo(int id) {
        Video video = new Video();
        video = this.videoSerivce.getValue().GetVideo(id);
        return video;
    }

    public boolean AddVideo(VideoSaveObject video) {
        return this.videoSerivce.getValue().AddVideo(video);
    }

    public boolean UpdateVideo(VideoSaveObject video) {
        return this.videoSerivce.getValue().EditVideo(video);
    }
    public boolean DeleteVideo(int id) {
        return this.videoSerivce.getValue().DeleteVideo(id);
    }

    public VideoSetup GetVideoSetup () {
        return this.videoSerivce.getValue().GetVideoSetup();
    }


}
