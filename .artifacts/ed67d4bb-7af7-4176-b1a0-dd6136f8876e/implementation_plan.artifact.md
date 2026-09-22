# XML Export and Import Implementation Plan

This plan outlines the steps to add XML import and export functionality to the MediaLibrary application.

## User Review Required

> [!IMPORTANT]
> The import process will replace all existing data in the database with the data from the XML file to ensure consistency.

## Proposed Changes

### [Backend Components]

#### [MODIFY] [IXmlRepository.java](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/backend/repository/Interface/Interface/IXmlRepository.java)
- Define a `DataContainer` class to hold all library data.
- Add methods: `DataContainer GetAllData()` and `void SaveAllData(DataContainer container)`.

#### [MODIFY] [XmlRepository.java](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/backend/repository/XmlRepository.java)
- Implement `GetAllData()` by querying all repositories/tables.
- Implement `SaveAllData()` by clearing all tables and inserting new data from the container.

#### [NEW] [XmlExportImport.kt](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/backend/utils/XmlExportImport.kt)
- Implement `exportToXml(DataContainer container): String` using `XmlSerializer` or similar.
- Implement `importFromXml(String xml): DataContainer` using `XmlPullParser`.

### [UI Components]

#### [MODIFY] [MainActivity.kt](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/home/ui/MainActivity.kt)
- Add `nav_import` and `nav_export` handling in `onOptionsItemSelected`.
- Use `registerForActivityResult` with `CreateDocument` and `GetContent`.
- Call `XmlExportImport` methods and `XmlRepository` to perform the operations.

#### [MODIFY] [BookActivity.kt](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/book/BookActivity.kt)
- Implement similar logic for overflow menu items.

#### [MODIFY] [VideoActivity.kt](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/java/com/example/medialibrary/video/VideoActivity.kt)
- Implement similar logic for overflow menu items.

## Verification Plan

### Automated Tests
- Create unit tests for `XmlExportImport` to verify that a `DataContainer` can be serialized to XML and deserialized back correctly.

### Manual Verification
1. Open the app and add some data (Books, Videos).
2. Use the "Export data" option from the overflow menu and save the XML file.
3. Clear app data or delete some items.
4. Use the "Import data" option, select the previously exported XML file.
5. Verify that all data is restored correctly.
