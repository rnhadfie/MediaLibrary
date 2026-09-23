package com.example.medialibrary.backend.models.book;

import androidx.annotation.NonNull;

public class Publisher {
    public Publisher() {}
    public Publisher(int id, String name) {
        this.Id = id;
        this.Name = name;
    }
    public int Id;
    public String Name;

    @NonNull
    @Override
    public String toString() {
        return Name != null ? Name : "";
    }
}
