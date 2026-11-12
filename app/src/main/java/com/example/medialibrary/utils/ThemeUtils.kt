package com.example.medialibrary.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.example.medialibrary.R

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

    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
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
}
