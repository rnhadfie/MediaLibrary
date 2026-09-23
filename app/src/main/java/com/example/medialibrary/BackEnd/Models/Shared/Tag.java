package com.example.medialibrary.backend.models.shared;

import androidx.annotation.NonNull;

public class Tag {
    public int Id;
    public String Name;

    @NonNull
    @Override
    public String toString() {
        return Name != null ? Name : "";
    }

    public Tag() {};
    public Tag(int id, String name) {
        Id = id;
        Name = name;
    }
}
