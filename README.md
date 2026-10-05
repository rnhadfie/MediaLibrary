# 📚 Media Library for Android

[![Android Build Status](https://github.com/Rhian/MediaLibrary/actions/workflows/android.yml/badge.svg)](https://github.com/Rhian/MediaLibrary/actions/workflows/android.yml)
[![Platform](https://img.shields.io/badge/Platform-Android%2010%2B-brightgreen.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-blue.svg)](https://kotlinlang.org/)

**Media Library** is a powerful, modern Android application designed to track, organize, and visualize all your physical and digital collections in one place. Whether you collect books, movies & TV shows, music CDs, or custom merchandise, Media Library gives you complete control over your library with statistics, volume tracking, custom tags, and seamless XML import/export.

---

## 📥 Download Release APK

Get the latest stable build ready to install on your Android device:
- 🚀 **[Download Latest Release APK](https://github.com/rnhadfie/MediaLibrary/actions/runs/37380461618/artifacts/11372984340)** *(Select the latest GitHub Actions workflow run and download the `MediaLibrary-release-apk` artifact)*
- 📦 **Direct APK File**: `app-release.apk` compiled for sideloading.

> [!TIP]
> To install on Android: Enable *"Install from Unknown Sources"* in your Android Settings when prompted by your browser or file manager.

---

## ✨ Key Features

### 📖 Books & Series
- Track **Novels, Manga, Light Novels, Comics, Graphic Novels**, and more.
- Manage **Authors, Artists, Publishers**, and custom tags.
- Detailed volume tracking for each book series (Volume number, title, format, read/unread, owned status).

### 🎬 Movies & TV Shows (Videos)
- Catalog **DVDs, Blu-Rays, 4K Ultra HD**, and **Digital** videos.
- Organize by video types: **Movies, TV Shows, Miniseries, Web Series**.
- Disc and season tracking with watched/unwatched status.

### 🎵 Music & Albums
- Organize your **CDs and Music Albums** by artist, release year, and genres.
- Track currently collecting vs. completed discographies.

### 📦 Custom Collections ("Other")
- Track any custom collection: **Action Figures, Board Games, Video Games, Merchandise**, or collectibles.

### 📊 Rich Analytics & Visual Graphs
- **Reading & Watch Progress**: Pie charts displaying percentage of read books / watched videos.
- **Format Breakdown**: Visual pie charts showing distribution across Paperback, Hardcover, E-Book, DVD, Blu-Ray, etc.
- **Type Distribution**: Bar charts showing count per media type.
- **Dual-Column Summary Cards**: Top genres, artists, and publishers sorted by collection volume.

### 🔍 Advanced Tri-State Filtering & Sorting
- **Real-Time Live Search**: Instant query response as you type.
- **Tri-State Filters**: Filter by Included, Excluded, or Neutral states for formats, genres, tags, publishers, collecting status, and read/watched status.
- **Sorting Options**: Sort alphabetically, by collecting priority (High, Medium, Low), or by media type.

---

## 📸 Screenshots & Showcase

| Main Dashboard | Book Library | Movie & TV Library |
| :---: | :---: | :---: |
| <img width="391" height="837" alt="Screenshot 2026-10-02 132212" src="https://github.com/user-attachments/assets/da1c23d6-0c66-4ed4-9c62-beef5b38e3f6" /> | <img width="311" height="659" alt="image" src="https://github.com/user-attachments/assets/ddbc15e1-e805-4292-8f73-fb01c25e7336" /> | <img width="281" height="628" alt="image" src="https://github.com/user-attachments/assets/5697c48a-ffd0-4183-9dc1-ac63cc485b0a" />
 |

| Collection Statistics | Form Entry & Volume Management | Filtering & Sorting |
| :---: | :---: | :---: |
| <img width="398" height="852" alt="Screenshot 2026-10-02 133001" src="https://github.com/user-attachments/assets/948d8178-ec81-4763-9033-a4d4cf9f0260" /> | <img width="402" height="780" alt="Screenshot 2026-10-02 133024" src="https://github.com/user-attachments/assets/ed114bc9-aa12-48e9-a91b-a8d32bf8a25b" /> | <img width="280" height="600" alt="image" src="https://github.com/user-attachments/assets/4e2de5e1-591d-4ba7-ab94-16986aca14c7" /> |

---

## 🔄 Data Import & Export (XML)

Media Library allows you to back up your collection or migrate data between devices using standard XML files.

### How to Export Data
1. Open the navigation drawer and tap **Export Data**.
2. Choose a destination folder on your device.
3. Media Library generates an XML backup file containing all your collections, tags, publishers, and cover images.

### How to Import Data
1. Open the navigation drawer and tap **Import Data**.
2. Select your valid `.xml` backup file.
3. Media Library parses and restores your entire collection into the database.

> [!IMPORTANT]
> Importing XML data will safely restore tags, publishers, books, videos, music, and other items into your library database.

---

## 📄 XML Format Specification

If you wish to generate or edit XML data manually prior to importing, format your XML file following the schema below:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<MediaLibrary>
    <!-- Custom Tags -->
    <Tags>
        <Tag id="1" name="Sci-Fi" />
        <Tag id="2" name="Favorite" />
    </Tags>

    <!-- Publishers -->
    <Publishers>
        <Publisher id="1" name="Penguin Random House" />
        <Publisher id="2" name="Tor Books" />
    </Publishers>

    <!-- Books -->
    <Books>
        <Book>
            <Id>uuid-book-1</Id>
            <Title>Dune</Title>
            <collecting>true</collecting>
            <Ongoing>false</Ongoing>
            <Collected>true</Collected>
            <TagId>1</TagId>
            <CollectingPriority>High</CollectingPriority>
            <Genres>1,2</Genres>
            <Author>Frank Herbert</Author>
            <Artist>Sam Weber</Artist>
            <Type>Novel</Type>
            <PublisherId>2</PublisherId>
            <Items>
                <BookItem>
                    <VolumeNumber>1</VolumeNumber>
                    <VolumeTitle>Dune Part 1</VolumeTitle>
                    <Read>true</Read>
                    <Owned>true</Owned>
                    <Format>Hardcover</Format>
                </BookItem>
            </Items>
        </Book>
    </Books>

    <!-- Movies & TV Shows -->
    <Videos>
        <Video>
            <Id>uuid-video-1</Id>
            <Title>Star Wars</Title>
            <collecting>true</collecting>
            <Ongoing>false</Ongoing>
            <Collected>true</Collected>
            <TagId>1</TagId>
            <CollectingPriority>Medium</CollectingPriority>
            <Genres>1</Genres>
            <Type>Movie</Type>
            <VideoTag>SciFi</VideoTag>
            <Items>
                <VideoItem>
                    <DiscNumber>1</DiscNumber>
                    <DiscTitle>A New Hope</DiscTitle>
                    <Watched>true</Watched>
                    <Owned>true</Owned>
                    <Format>BluRay</Format>
                </VideoItem>
            </Items>
        </Video>
    </Videos>

    <!-- Music CDs -->
    <MusicList>
        <Music>
            <Id>uuid-music-1</Id>
            <Title>The Dark Side of the Moon</Title>
            <collecting>false</collecting>
            <Ongoing>false</Ongoing>
            <Collected>true</Collected>
            <TagId>2</TagId>
            <CollectingPriority>Low</CollectingPriority>
            <Artist>Pink Floyd</Artist>
            <Year>1973</Year>
        </Music>
    </MusicList>

    <!-- Other Collections -->
    <Others>
        <Other>
            <Id>uuid-other-1</Id>
            <Title>Limited Edition Figurine</Title>
            <collecting>true</collecting>
            <Ongoing>true</Ongoing>
            <Collected>false</Collected>
            <TagId>2</TagId>
            <CollectingPriority>High</CollectingPriority>
        </Other>
    </Others>
</MediaLibrary>
```

### Element Descriptions
| XML Element | Description | Valid Values / Example                                               |
| :--- | :--- |:---------------------------------------------------------------------|
| `<Tag>` | Custom categorization tag | `id="1" name="Sci-Fi"`                                               |
| `<Publisher>` | Book or media publisher | `id="1" name="Tor Books"`                                            |
| `<collecting>` | Is the collection currently active? | `true` / `false`                                                     |
| `<Ongoing>` | Is the series currently ongoing? | `true` / `false`                                                     |
| `<Collected>` | Have all items/volumes been collected? | `true` / `false`                                                     |
| `<CollectingPriority>` | Priority level | `None`, `Low`, `Medium`, `High`                                      |
| `<Type>` (Book) | Book publication type | `Novel`, `Manga`, `LightNovel`, `Comic`, `GraphicNovel`, `Audiobook` |
| `<Type>` (Video) | Video classification | `Movie`, `TVShow`, `Miniseries`, `WebSeries`                         |
| `<Format>` (Book) | Physical/digital format | `Paperback`, `Hardcover`, `EBook`                                    |
| `<Format>` (Video) | Disc/digital format | `DVD`, `BluRay`, `Digital`, `UltraHD`                                |

---

## 🛠️ Building & Development Setup

### Prerequisites
- **Android Studio 2026.1.4+** or **Android Studio Ladybug+**
- **JDK 17**
- **Android SDK 37** (minSdk: 29)

### Building via Command Line
```bash
# Clone the repository
git clone https://github.com/Rhian/MediaLibrary.git
cd MediaLibrary

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Run Unit Tests
./gradlew testDebugUnitTest
```
