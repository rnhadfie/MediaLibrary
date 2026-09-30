package models.book;

import androidx.annotation.NonNull;

public class Publisher {
    public Publisher() {}
    public Publisher(String id, String name) {
        this.Id = id;
        this.Name = name;
    }
    public String Id;
    public String Name;

    @NonNull
    @Override
    public String toString() {
        return Name != null ? Name : "";
    }
}
