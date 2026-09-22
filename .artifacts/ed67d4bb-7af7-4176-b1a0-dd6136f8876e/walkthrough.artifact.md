# XML Export and Import Walkthrough

I have implemented the XML import and export functionality for the MediaLibrary application. This allows users to back up their entire library to an XML file and restore it later.

## Changes Made

### Backend
- **[DataContainer.java](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/backend/models/shared/DataContainer.java)**: A new data model to hold all library entities (Books, Videos, Music, Others, Tags, Publishers).
- **[IXmlRepository.java](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/backend/repository/Interface/Interface/IXmlRepository.java)**: Updated to include `GetAllData()` and `SaveAllData()`.
- **[XmlRepository.java](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/backend/repository/XmlRepository.java)**: Implemented the logic to retrieve all data from the database and restore it from a `DataContainer`.
- **[XmlExportImport.kt](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/backend/utils/XmlExportImport.kt)**: A utility to handle the conversion between `DataContainer` and XML format, including Base64 encoding for covers.
- **Base/Music/Other Repositories**: Updated to support adding items and shared genre serialization.

### UI
- **[MainActivity.kt](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/home/ui/MainActivity.kt)**, **[BookActivity.kt](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/book/BookActivity.kt)**, and **[VideoActivity.kt](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/video/VideoActivity.kt)**:
    - Added overflow menu handling for `nav_import` and `nav_export`.
    - Integrated Android's `ActivityResultContracts` for file picking (`GetContent`) and file creation (`CreateDocument`).
    - Added success/failure notifications via `Snackbar`.

## How to Test

1.  **Export**:
    *   Open any of the main activities (Home, Books, Videos).
    *   Open the overflow menu (three dots).
    *   Select **Export data**.
    *   Choose a location and name for the `.xml` file.
2.  **Import**:
    *   Open the overflow menu.
    *   Select **Import data**.
    *   Select a previously exported `.xml` file.
    *   The app will notify you upon success and refresh the current activity.

> [!CAUTION]
> Importing data will clear all current entries in your library and replace them with the data from the XML file.
