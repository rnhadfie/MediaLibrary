package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.other.OtherSaveObj;

import java.util.List;

public interface IOtherRepository {
    List<Other> GetOtherCollections();
    List<Other> GetOtherCollections(String whereClause, List<String> selectionArgs);
    Other GetOtherCollection(int id);
    boolean AddOtherCollection(OtherSaveObj otherObj);
    boolean UpdateOtherCollection(OtherSaveObj otherObj);
    boolean DeleteOtherCollection(int id);
}
