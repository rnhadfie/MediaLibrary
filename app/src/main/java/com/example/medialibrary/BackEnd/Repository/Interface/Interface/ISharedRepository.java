package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.shared.Tag;

import java.util.List;

public interface ISharedRepository {

    List<Tag> GetTags();
    Tag GetTag(String id);

    boolean AddTag(Tag publisher);
    boolean UpdateTag(Tag publisher);
    boolean DeleteTag(String id);
}
