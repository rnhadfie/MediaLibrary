package com.example.medialibrary.shared

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding

import com.example.medialibrary.R
import models.shared.Enums

object ThemeUtils {

    const val PREF_NAME = "media_library_prefs"
    const val PREF_THEME = "selected_theme"

    const val THEME_DEFAULT = "default"
    const val THEME_SAKURA = "sakura"
    const val THEME_OCEAN = "ocean"
    const val THEME_HIGH_CONTRAST = "high_contrast"
    const val THEME_AUTUMN = "autumn"
    const val THEME_MATCHA = "matcha"
    const val THEME_TROPICAL = "tropical"
    const val THEME_WINTER = "winter"

    fun getSelectedTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_THEME, THEME_DEFAULT) ?: THEME_DEFAULT
    }

    fun setSelectedTheme(context: Context, themeKey: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_THEME, themeKey).apply()
    }

    fun resolveThemeResId(themeKey: String, activityName: String): Int {
        val simpleName = activityName.substringAfterLast('.')
        return when (themeKey) {
            THEME_SAKURA -> when (simpleName) {
                "BookActivity" -> R.style.Theme_MediaLibrary_Sakura_Book_NoActionBar
                "BookFormActivity" -> R.style.Theme_MediaLibrary_Sakura_Book
                "VideoActivity" -> R.style.Theme_MediaLibrary_Sakura_Video_NoActionBar
                "VideoFormActivity" -> R.style.Theme_MediaLibrary_Sakura_Video
                "MusicActivity" -> R.style.Theme_MediaLibrary_Sakura_Music_NoActionBar
                "MusicFormActivity" -> R.style.Theme_MediaLibrary_Sakura_Music
                "OtherActivity" -> R.style.Theme_MediaLibrary_Sakura_Other_NoActionBar
                "OtherFormActivity" -> R.style.Theme_MediaLibrary_Sakura_Other
                else -> R.style.Theme_MediaLibrary_Sakura_NoActionBar
            }
            THEME_OCEAN -> when (simpleName) {
                "BookActivity" -> R.style.Theme_MediaLibrary_Ocean_Book_NoActionBar
                "BookFormActivity" -> R.style.Theme_MediaLibrary_Ocean_Book
                "VideoActivity" -> R.style.Theme_MediaLibrary_Ocean_Video_NoActionBar
                "VideoFormActivity" -> R.style.Theme_MediaLibrary_Ocean_Video
                "MusicActivity" -> R.style.Theme_MediaLibrary_Ocean_Music_NoActionBar
                "MusicFormActivity" -> R.style.Theme_MediaLibrary_Ocean_Music
                "OtherActivity" -> R.style.Theme_MediaLibrary_Ocean_Other_NoActionBar
                "OtherFormActivity" -> R.style.Theme_MediaLibrary_Ocean_Other
                else -> R.style.Theme_MediaLibrary_Ocean_NoActionBar
            }
            THEME_HIGH_CONTRAST -> when (simpleName) {
                "BookActivity" -> R.style.Theme_MediaLibrary_HighContrast_Book_NoActionBar
                "BookFormActivity" -> R.style.Theme_MediaLibrary_HighContrast_Book
                "VideoActivity" -> R.style.Theme_MediaLibrary_HighContrast_Video_NoActionBar
                "VideoFormActivity" -> R.style.Theme_MediaLibrary_HighContrast_Video
                "MusicActivity" -> R.style.Theme_MediaLibrary_HighContrast_Music_NoActionBar
                "MusicFormActivity" -> R.style.Theme_MediaLibrary_HighContrast_Music
                "OtherActivity" -> R.style.Theme_MediaLibrary_HighContrast_Other_NoActionBar
                "OtherFormActivity" -> R.style.Theme_MediaLibrary_HighContrast_Other
                else -> R.style.Theme_MediaLibrary_HighContrast_NoActionBar
            }
            THEME_AUTUMN -> when (simpleName) {
                "BookActivity" -> R.style.Theme_MediaLibrary_Autumn_Book_NoActionBar
                "BookFormActivity" -> R.style.Theme_MediaLibrary_Autumn_Book
                "VideoActivity" -> R.style.Theme_MediaLibrary_Autumn_Video_NoActionBar
                "VideoFormActivity" -> R.style.Theme_MediaLibrary_Autumn_Video
                "MusicActivity" -> R.style.Theme_MediaLibrary_Autumn_Music_NoActionBar
                "MusicFormActivity" -> R.style.Theme_MediaLibrary_Autumn_Music
                "OtherActivity" -> R.style.Theme_MediaLibrary_Autumn_Other_NoActionBar
                "OtherFormActivity" -> R.style.Theme_MediaLibrary_Autumn_Other
                else -> R.style.Theme_MediaLibrary_Autumn_NoActionBar
            }
            THEME_MATCHA -> when (simpleName) {
                "BookActivity" -> R.style.Theme_MediaLibrary_Matcha_Book_NoActionBar
                "BookFormActivity" -> R.style.Theme_MediaLibrary_Matcha_Book
                "VideoActivity" -> R.style.Theme_MediaLibrary_Matcha_Video_NoActionBar
                "VideoFormActivity" -> R.style.Theme_MediaLibrary_Matcha_Video
                "MusicActivity" -> R.style.Theme_MediaLibrary_Matcha_Music_NoActionBar
                "MusicFormActivity" -> R.style.Theme_MediaLibrary_Matcha_Music
                "OtherActivity" -> R.style.Theme_MediaLibrary_Matcha_Other_NoActionBar
                "OtherFormActivity" -> R.style.Theme_MediaLibrary_Matcha_Other
                else -> R.style.Theme_MediaLibrary_Matcha_NoActionBar
            }
            THEME_TROPICAL -> when (simpleName) {
                "BookActivity" -> R.style.Theme_MediaLibrary_Tropical_Book_NoActionBar
                "BookFormActivity" -> R.style.Theme_MediaLibrary_Tropical_Book
                "VideoActivity" -> R.style.Theme_MediaLibrary_Tropical_Video_NoActionBar
                "VideoFormActivity" -> R.style.Theme_MediaLibrary_Tropical_Video
                "MusicActivity" -> R.style.Theme_MediaLibrary_Tropical_Music_NoActionBar
                "MusicFormActivity" -> R.style.Theme_MediaLibrary_Tropical_Music
                "OtherActivity" -> R.style.Theme_MediaLibrary_Tropical_Other_NoActionBar
                "OtherFormActivity" -> R.style.Theme_MediaLibrary_Tropical_Other
                else -> R.style.Theme_MediaLibrary_Tropical_NoActionBar
            }
            THEME_WINTER -> when (simpleName) {
                "BookActivity" -> R.style.Theme_MediaLibrary_Winter_Book_NoActionBar
                "BookFormActivity" -> R.style.Theme_MediaLibrary_Winter_Book
                "VideoActivity" -> R.style.Theme_MediaLibrary_Winter_Video_NoActionBar
                "VideoFormActivity" -> R.style.Theme_MediaLibrary_Winter_Video
                "MusicActivity" -> R.style.Theme_MediaLibrary_Winter_Music_NoActionBar
                "MusicFormActivity" -> R.style.Theme_MediaLibrary_Winter_Music
                "OtherActivity" -> R.style.Theme_MediaLibrary_Winter_Other_NoActionBar
                "OtherFormActivity" -> R.style.Theme_MediaLibrary_Winter_Other
                else -> R.style.Theme_MediaLibrary_Winter_NoActionBar
            }
            else -> when (simpleName) {
                "BookActivity" -> R.style.Theme_MediaLibrary_Book_NoActionBar
                "BookFormActivity" -> R.style.Theme_MediaLibrary_Book
                "VideoActivity" -> R.style.Theme_MediaLibrary_Video_NoActionBar
                "VideoFormActivity" -> R.style.Theme_MediaLibrary_Video
                "MusicActivity" -> R.style.Theme_MediaLibrary_Music_NoActionBar
                "MusicFormActivity" -> R.style.Theme_MediaLibrary_Music
                "OtherActivity" -> R.style.Theme_MediaLibrary_Other_NoActionBar
                "OtherFormActivity" -> R.style.Theme_MediaLibrary_Other
                else -> R.style.Theme_MediaLibrary_NoActionBar
            }
        }
    }

    fun applyTheme(activity: Activity) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val themeKey = getSelectedTheme(activity)
        val themeResId = resolveThemeResId(themeKey, activity.javaClass.simpleName)
        activity.setTheme(themeResId)
    }

    fun applyTheme(activity: Activity, viewBinding: ViewBinding) {
        val themeKey = getSelectedTheme(activity)
        val themeResId = resolveThemeResId(themeKey, activity.javaClass.simpleName)
        viewBinding.root.context.theme.applyStyle(themeResId, true)
    }

    fun getThemeColor(context: Context, attrResId: Int = androidx.appcompat.R.attr.colorPrimary): Int {
        val typedValue = TypedValue()
        if (context.theme.resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data
        }
        return ContextCompat.getColor(context, R.color.primary)
    }

    fun getThemeColor2(context: Context, type: Enums.MediaType) : SectionColors {
        val activityName = when(type) {
            Enums.MediaType.Book -> "BookActivity"
            Enums.MediaType.Video -> "VideoActivity"
            Enums.MediaType.Music -> "MusicActivity"
            Enums.MediaType.Other -> "OtherActivity"
            else -> "MainActivity"
        }
        val mainColor = getActivityColour(context, activityName);
        val lightColor = getLightActivityColour(context, activityName);
        val darkColor = getDarkActivityColour(context, activityName);

        val colours = when (type) {
            Enums.MediaType.Book -> SectionColors(
                mainColor,
                lightColor,
                darkColor,
                R.drawable.ic_book_small
            )

            Enums.MediaType.Video -> SectionColors(
                mainColor,
                lightColor,
                 darkColor,
                R.drawable.ic_movie
            )

            Enums.MediaType.Music -> SectionColors(
                 mainColor,
                 lightColor,
                 darkColor,
                R.drawable.ic_music
            )

            Enums.MediaType.Other -> SectionColors(
                 mainColor,
                 lightColor,
                darkColor,
                R.drawable.other_item_small
            )

            else -> {
                val typedValue = TypedValue()
                context.theme.resolveAttribute(
                    androidx.appcompat.R.attr.colorPrimary,
                    typedValue,
                    true
                )
                SectionColors(
                    mainColor,
                    lightColor,
                    darkColor,
                    R.drawable.ic_gallery_black_24dp
                )
            }
        }
        return colours
    }


    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun getLightActivityColour(context: Context, activityName: String): Int {
        val themeKey = getSelectedTheme(context)
        val simpleName = activityName.substringAfterLast('.')
        val colorResId = when (themeKey) {
            THEME_SAKURA -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.sakura_book_light
                "VideoActivity", "VideoFormActivity" -> R.color.sakura_video_light
                "MusicActivity", "MusicFormActivity" -> R.color.sakura_music_light
                "OtherActivity", "OtherFormActivity" -> R.color.sakura_other_light
                else -> R.color.sakura_primary
            }
            THEME_OCEAN -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.ocean_book_light
                "VideoActivity", "VideoFormActivity" -> R.color.ocean_video_light
                "MusicActivity", "MusicFormActivity" -> R.color.ocean_music_light
                "OtherActivity", "OtherFormActivity" -> R.color.ocean_other_light
                else -> R.color.ocean_primary
            }
            THEME_HIGH_CONTRAST -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.highcontrast_book_light
                "VideoActivity", "VideoFormActivity" -> R.color.highcontrast_video_light
                "MusicActivity", "MusicFormActivity" -> R.color.highcontrast_music_light
                "OtherActivity", "OtherFormActivity" -> R.color.highcontrast_other_light
                else -> R.color.highcontrast_primary
            }
            THEME_AUTUMN -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.autumn_book_light
                "VideoActivity", "VideoFormActivity" -> R.color.autumn_video_light
                "MusicActivity", "MusicFormActivity" -> R.color.autumn_music_light
                "OtherActivity", "OtherFormActivity" -> R.color.autumn_other_light
                else -> R.color.autumn_primary
            }
            THEME_MATCHA -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.matcha_book_light
                "VideoActivity", "VideoFormActivity" -> R.color.matcha_video_light
                "MusicActivity", "MusicFormActivity" -> R.color.matcha_music_light
                "OtherActivity", "OtherFormActivity" -> R.color.matcha_other_light
                else -> R.color.matcha_primary
            }
            THEME_TROPICAL -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.tropical_book_light
                "VideoActivity", "VideoFormActivity" -> R.color.tropical_video_light
                "MusicActivity", "MusicFormActivity" -> R.color.tropical_music_light
                "OtherActivity", "OtherFormActivity" -> R.color.tropical_other_light
                else -> R.color.tropical_primary
            }
            THEME_WINTER -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.winter_book_light
                "VideoActivity", "VideoFormActivity" -> R.color.winter_video_light
                "MusicActivity", "MusicFormActivity" -> R.color.winter_music_light
                "OtherActivity", "OtherFormActivity" -> R.color.winter_other_light
                else -> R.color.winter_primary
            }
            else -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.section_book_light
                "VideoActivity", "VideoFormActivity" -> R.color.section_video_light
                "MusicActivity", "MusicFormActivity" -> R.color.section_music_light
                "OtherActivity", "OtherFormActivity" -> R.color.section_other_light
                else -> R.color.primary
            }
        }
        return ContextCompat.getColor(context, colorResId);
    }

    fun getActivityColour(context: Context, activityName: String): Int {
        val themeKey = getSelectedTheme(context)
        val simpleName = activityName.substringAfterLast('.')
        val colorResId = when (themeKey) {
            THEME_SAKURA -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.sakura_book
                "VideoActivity", "VideoFormActivity" -> R.color.sakura_video
                "MusicActivity", "MusicFormActivity" -> R.color.sakura_music
                "OtherActivity", "OtherFormActivity" -> R.color.sakura_other
                else -> R.color.sakura_primary
            }
            THEME_OCEAN -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.ocean_book
                "VideoActivity", "VideoFormActivity" -> R.color.ocean_video
                "MusicActivity", "MusicFormActivity" -> R.color.ocean_music
                "OtherActivity", "OtherFormActivity" -> R.color.ocean_other
                else -> R.color.ocean_primary
            }
            THEME_HIGH_CONTRAST -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.highcontrast_book
                "VideoActivity", "VideoFormActivity" -> R.color.highcontrast_video
                "MusicActivity", "MusicFormActivity" -> R.color.highcontrast_music
                "OtherActivity", "OtherFormActivity" -> R.color.highcontrast_other
                else -> R.color.highcontrast_primary
            }
            THEME_AUTUMN -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.autumn_book
                "VideoActivity", "VideoFormActivity" -> R.color.autumn_video
                "MusicActivity", "MusicFormActivity" -> R.color.autumn_music
                "OtherActivity", "OtherFormActivity" -> R.color.autumn_other
                else -> R.color.autumn_primary
            }
            THEME_MATCHA -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.matcha_book
                "VideoActivity", "VideoFormActivity" -> R.color.matcha_video
                "MusicActivity", "MusicFormActivity" -> R.color.matcha_music
                "OtherActivity", "OtherFormActivity" -> R.color.matcha_other
                else -> R.color.matcha_primary
            }
            THEME_TROPICAL -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.tropical_book
                "VideoActivity", "VideoFormActivity" -> R.color.tropical_video
                "MusicActivity", "MusicFormActivity" -> R.color.tropical_music
                "OtherActivity", "OtherFormActivity" -> R.color.tropical_other
                else -> R.color.tropical_primary
            }
            THEME_WINTER -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.winter_book
                "VideoActivity", "VideoFormActivity" -> R.color.winter_video
                "MusicActivity", "MusicFormActivity" -> R.color.winter_music
                "OtherActivity", "OtherFormActivity" -> R.color.winter_other
                else -> R.color.winter_primary
            }
            else -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.section_book
                "VideoActivity", "VideoFormActivity" -> R.color.section_video
                "MusicActivity", "MusicFormActivity" -> R.color.section_music
                "OtherActivity", "OtherFormActivity" -> R.color.section_other
                else -> R.color.primary
            }
        }
        return ContextCompat.getColor(context, colorResId)
    }

    fun getDarkActivityColour(context: Context, activityName: String): Int {
        val themeKey = getSelectedTheme(context)
        val simpleName = activityName.substringAfterLast('.')
        val colorResId = when (themeKey) {
            THEME_SAKURA -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.sakura_book_dark
                "VideoActivity", "VideoFormActivity" -> R.color.sakura_video_dark
                "MusicActivity", "MusicFormActivity" -> R.color.sakura_music_dark
                "OtherActivity", "OtherFormActivity" -> R.color.sakura_other_dark
                else -> R.color.sakura_primary_dark
            }
            THEME_OCEAN -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.ocean_book_dark
                "VideoActivity", "VideoFormActivity" -> R.color.ocean_video_dark
                "MusicActivity", "MusicFormActivity" -> R.color.ocean_music_dark
                "OtherActivity", "OtherFormActivity" -> R.color.ocean_other_dark
                else -> R.color.ocean_primary_dark
            }
            THEME_HIGH_CONTRAST -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.highcontrast_book_dark
                "VideoActivity", "VideoFormActivity" -> R.color.highcontrast_video_dark
                "MusicActivity", "MusicFormActivity" -> R.color.highcontrast_music_dark
                "OtherActivity", "OtherFormActivity" -> R.color.highcontrast_other_dark
                else -> R.color.highcontrast_primary_dark
            }
            THEME_AUTUMN -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.autumn_book_dark
                "VideoActivity", "VideoFormActivity" -> R.color.autumn_video_dark
                "MusicActivity", "MusicFormActivity" -> R.color.autumn_music_dark
                "OtherActivity", "OtherFormActivity" -> R.color.autumn_other_dark
                else -> R.color.autumn_primary_dark
            }
            THEME_MATCHA -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.matcha_book_dark
                "VideoActivity", "VideoFormActivity" -> R.color.matcha_video_dark
                "MusicActivity", "MusicFormActivity" -> R.color.matcha_music_dark
                "OtherActivity", "OtherFormActivity" -> R.color.matcha_other_dark
                else -> R.color.matcha_primary_dark
            }
            THEME_TROPICAL -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.tropical_book_dark
                "VideoActivity", "VideoFormActivity" -> R.color.tropical_video_dark
                "MusicActivity", "MusicFormActivity" -> R.color.tropical_music_dark
                "OtherActivity", "OtherFormActivity" -> R.color.tropical_other_dark
                else -> R.color.tropical_primary_dark
            }
            THEME_WINTER -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.winter_book_dark
                "VideoActivity", "VideoFormActivity" -> R.color.winter_video_dark
                "MusicActivity", "MusicFormActivity" -> R.color.winter_music_dark
                "OtherActivity", "OtherFormActivity" -> R.color.winter_other_dark
                else -> R.color.winter_primary_dark
            }
            else -> when (simpleName) {
                "BookActivity", "BookFormActivity" -> R.color.section_book_dark
                "VideoActivity", "VideoFormActivity" -> R.color.section_video_dark
                "MusicActivity", "MusicFormActivity" -> R.color.section_music_dark
                "OtherActivity", "OtherFormActivity" -> R.color.section_other_dark
                else -> R.color.primary
            }
        }
        return ContextCompat.getColor(context, colorResId)
    }


    public data class SectionColors(
        val main: Int,
        val light: Int,
        val dark: Int,
        val iconRes: Int
    )

    fun getContrastingTextColor(backgroundColor: Int): Int {
        val red = Color.red(backgroundColor)
        val green = Color.green(backgroundColor)
        val blue = Color.blue(backgroundColor)
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    fun getSpineColor(context: Context, spineHex: String?, activityName: String, index: Int): Int {
        val main = getActivityColour(context, activityName)
        val dark = getDarkActivityColour(context, activityName)
        val light = getLightActivityColour(context, activityName)
        val primary = getThemeColor(context, androidx.appcompat.R.attr.colorPrimary)
        val secondary = getThemeColor(context, com.google.android.material.R.attr.colorSecondary)

        val themePalette = listOf(
            main,
            dark,
            primary,
            adjustAlpha(main, 0.85f),
            secondary,
            adjustAlpha(dark, 0.9f),
            light
        )

        if (!spineHex.isNullOrBlank()) {
            try {
                return Color.parseColor(spineHex)
            } catch (_: IllegalArgumentException) {}
        }
        return themePalette[Math.abs(index) % themePalette.size]
    }
}
