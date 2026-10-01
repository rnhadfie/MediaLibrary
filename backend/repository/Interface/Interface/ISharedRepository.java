package repository.Interface.Interface;

import models.shared.Tag;

import java.util.List;

public interface ISharedRepository {

    List<Tag> GetTags();
    Tag GetTag(String id);

    boolean AddTag(Tag publisher);
    boolean UpdateTag(Tag publisher);
    boolean DeleteTag(String id, boolean forceDelete);

    boolean TagIsBeingUsed(String id);
}
