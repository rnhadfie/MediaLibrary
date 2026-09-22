package com.example.medialibrary.backend.models.book;

public class Publisher {
    public Publisher() {}
    public Publisher(int id, String name) {
        this.Id = id;
        this.Name = name;
    }
    public int Id;
    public String Name;

    @Override
    public String toString() {
        return Name != null ? Name : "";
    }
}
