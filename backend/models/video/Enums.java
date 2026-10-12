package models.video;

public class Enums {
    public enum VideoFormat
    {
        NoneSelected,
        DVD,
        BluRay,
        Digital,
        UltraHD
    }

    public enum VideoTag
    {
        None,
        Anime,
        Documentary,
        Concert,
        WesternAnimation,
        LiveAction,
    }

    public enum VideoType
    {
        NoneSelected,
        TVShow,
        Movie,
        WebSeries,
        Miniseries,
    }

    public enum Rating
    {
        NotRated,
        G,
        PG,
        PG13,
        R,
        NC17
    }
}
