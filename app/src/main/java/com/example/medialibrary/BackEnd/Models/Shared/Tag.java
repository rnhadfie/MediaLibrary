package com.example.medialibrary.backend.models.shared;

import androidx.annotation.NonNull;

public class Tag {
    public String Id;
    public String Name;

    @NonNull
    @Override
    public String toString() {
        return Name != null ? Name : "";
    }

    public Tag() {};
    public Tag(String id, String name) {
        Id = id;
        Name = name;
    }
}
