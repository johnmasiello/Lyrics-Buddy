package com.example.john.lyricsbuddy

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.widget.Toast

/**
 * Created by john on 4/24/18.
 * Helper class to provide a global implementation supporting action on lyrics
 */

fun share(fragment: Fragment, vararg songLyrics: LyricDatabaseHelper.SongLyrics) {
    val jsonArray = LyricDatabaseHelper.SongLyrics.toJSONArray(*songLyrics)

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = fragment.getString(R.string.mimeType)
    intent.putExtra(Intent.EXTRA_TEXT, jsonArray.toString())

    val context = fragment.context
    if (context != null) {
        intent.putExtra(Intent.EXTRA_SUBJECT,
                LyricDatabaseHelper.SongLyrics.extractSubjectLine(context, jsonArray))
    }
    fragment.startActivity(Intent.createChooser(intent,
            fragment.getString(R.string.share_intent_message)))
}

fun failShare(context: Context?, msgId: Int) {
    if (context != null) {
        Toast.makeText(context,
                context.getString(msgId),
                Toast.LENGTH_SHORT).show()
    }
}

fun test(a: Int?):Int {
    return a?.let { a + 1 } ?: 0
}
