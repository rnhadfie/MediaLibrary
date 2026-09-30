package repository.Interface.Interface;

import models.shared.DataContainer;

public interface IXmlRepository {
    DataContainer GetAllData();
    String SaveAllData(DataContainer container);
}
