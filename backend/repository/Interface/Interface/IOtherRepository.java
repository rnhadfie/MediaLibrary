package repository.Interface.Interface;

import models.other.*;
import java.util.List;

public interface IOtherRepository {
    List<Other> GetOtherCollections();
    List<Other> GetOtherCollections(String whereClause, List<String> selectionArgs);
    Other GetOtherCollection(String id);
    boolean AddOtherCollection(OtherSaveObj otherObj);
    boolean UpdateOtherCollection(OtherSaveObj otherObj);
    boolean DeleteOtherCollection(String id);
}
