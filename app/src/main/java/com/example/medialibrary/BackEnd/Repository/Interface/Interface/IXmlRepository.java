package com.example.medialibrary.backend.repository.Interface.Interface;

import com.example.medialibrary.backend.models.shared.DataContainer;

public interface IXmlRepository {
    DataContainer GetAllData();
    String SaveAllData(DataContainer container);
}
