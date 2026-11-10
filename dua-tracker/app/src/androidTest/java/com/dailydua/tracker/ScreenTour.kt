package com.dailydua.tracker

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Walks through every screen in light and dark mode and saves a screenshot of each step to
 * /data/local/tmp/shots on the device. Used by the UI test workflow for a visual review.
 */
@RunWith(AndroidJUnit4::class)
class ScreenTour {
    @get:Rule(order = 0)
    val notifications: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private val inst get() = InstrumentationRegistry.getInstrumentation()
    private val device get() = UiDevice.getInstance(inst)

    private fun shell(cmd: String) {
        inst.uiAutomation.executeShellCommand(cmd).use { pfd ->
            FileInputStream(pfd.fileDescriptor).use { it.readBytes() }
        }
    }

    /** The slow CI emulator sometimes shows a system "isn't responding" dialog; press Wait. */
    private fun dismissSystemDialogs() {
        repeat(3) {
            val wait = device.findObject(androidx.test.uiautomator.By.text("Wait")) ?: return
            wait.click()
            Thread.sleep(400)
        }
    }

    private fun shot(name: String, settleMs: Long = 900) {
        dismissSystemDialogs()
        rule.waitForIdle()
        Thread.sleep(settleMs)
        val bmp = inst.uiAutomation.takeScreenshot() ?: return
        val dir = File(inst.targetContext.getExternalFilesDir(null), "shots").apply { mkdirs() }
        val f = File(dir, "$name.png")
        FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        shell("mkdir -p /data/local/tmp/shots")
        shell("cp ${f.absolutePath} /data/local/tmp/shots/$name.png")
    }

    private fun step(name: String, block: () -> Unit) {
        try {
            dismissSystemDialogs()
            block()
        } catch (t: Throwable) {
            android.util.Log.e("ScreenTour", "Step $name failed", t)
            shot("ERR_$name")
            throw t
        }
    }

    private fun scrollTo(text: String) {
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text))
    }

    private fun back() {
        device.pressBack()
        rule.waitForIdle()
        Thread.sleep(500)
    }

    private fun tab(name: String) {
        rule.onNodeWithContentDescription(name).performClick()
        rule.waitForIdle()
    }

    @Test
    fun tour() {
        step("today") {
            shot("01_today_light", 1500)
            scrollTo("Dua al-Qunoot")
            shot("02_today_recitations")
            scrollTo("Janazah prayer guide")
            shot("03_today_more")
        }
        step("janazah") {
            rule.onNodeWithText("Janazah prayer guide").performClick()
            shot("04_janazah_reader")
            rule.onAllNodes(hasScrollAction()).onFirst().performTouchInput { swipeUp() }
            shot("05_janazah_scrolled")
            back()
        }
        step("short_dua") {
            scrollTo("Upon waking up")
            rule.onNodeWithText("Upon waking up").performClick()
            rule.waitUntil(5000) { rule.onAllNodesWithText("Mark as done").fetchSemanticsNodes().isNotEmpty() }
            shot("06_short_dua_sheet")
            back()
        }
        step("recitation") {
            scrollTo("Dua al-Qunoot")
            rule.onNodeWithText("Dua al-Qunoot").performClick()
            shot("07_qunoot_reader")
            rule.onNodeWithTag("play").performClick()
            Thread.sleep(6000)
            shot("08_qunoot_playing")
            // The background reads this value to move with the voice.
            val samples = (0 until 40).map {
                Thread.sleep(100)
                val v = rule.runOnUiThread { com.dailydua.tracker.playback.AudioLevel.level }
                v
            }
            android.util.Log.i("ScreenTour", "AUDIO_LEVEL max=${samples.max()} min=${samples.min()} avg=${samples.average()}")
            back()
            shot("09_today_mini_player")
        }
        step("quran") {
            tab("Quran")
            shot("10_quran_tab")
            rule.onAllNodesWithText("Benefits").onFirst().performClick()
            shot("10b_quran_benefits", 1200)
            rule.onAllNodesWithText("Benefits").onFirst().performClick()
            rule.onAllNodesWithText("Start reading").onFirst().performClick()
            shot("11_text_reader", 2000)
            rule.onNodeWithTag("sizeUp").performClick()
            rule.onNodeWithTag("sizeUp").performClick()
            rule.onNodeWithTag("sizeUp").performClick()
            shot("11c_text_larger", 1200)
            rule.onNodeWithTag("favorite").performClick()
            shot("11d_favourited", 800)
            rule.onNodeWithTag("repeat").performClick()
            shot("11f_repeat_menu", 800)
            back()
            rule.onNodeWithTag("setReminder").performClick()
            shot("11e_reminder_from_reader", 1200)
            rule.onNodeWithText("Cancel").performClick()
            rule.onNodeWithTag("sizeDown").performClick()
            rule.onNodeWithTag("sizeDown").performClick()
            rule.onNodeWithTag("sizeDown").performClick()
            rule.onNodeWithTag("englishToggle").performClick()
            shot("11a_text_english", 1500)
            rule.onNodeWithTag("englishToggle").performClick()
            rule.onNodeWithTag("fullscreen").performClick()
            shot("11b_fullscreen", 1500)
            rule.onNodeWithTag("exitFullscreen").performClick()
            rule.onNodeWithTag("verseToggle").performClick()
            shot("11_mushaf_page1", 2000)
            rule.onRoot().performTouchInput { swipeRight(startX = width * 0.3f, endX = width * 0.75f) }
            shot("12_mushaf_page2", 1500)
            rule.onRoot().performTouchInput { swipeRight(startX = width * 0.3f, endX = width * 0.75f) }
            shot("12b_mushaf_page3", 1500)
            back()
            shot("13_quran_continue")
            scrollTo("The 99 Names of Allah")
            rule.onNodeWithText("Read the names").performClick()
            shot("13b_names_reader", 1500)
            back()
            tab("Favourites")
            shot("13c_favourites_tab", 1500)
            tab("Quran")
        }
        step("playlist") {
            tab("Playlist")
            shot("14_playlist_empty")
            rule.onNodeWithText("Add all").performClick()
            shot("15_playlist_filled")
            rule.onNodeWithText("Play all", substring = true).performClick()
            Thread.sleep(2500)
            shot("16_playlist_playing")
        }
        step("notification") {
            device.openNotification()
            Thread.sleep(2500)
            shot("17_notification_shade", 300)
            back()
        }
        step("settings") {
            tab("Settings")
            shot("18_settings_light")
            scrollTo("+ Add a custom reminder")
            shot("19_settings_reminders")
            rule.onNodeWithText("+ Add a custom reminder").performClick()
            shot("20_reminder_dialog")
            rule.onNodeWithContentDescription("Choose a dua").performClick()
            shot("20b_reminder_dropdown", 1000)
            back()
            rule.onNodeWithText("Cancel").performClick()
        }
        step("dark") {
            scrollTo("Dark")
            rule.onNodeWithText("Dark").performClick()
            shot("21_settings_dark", 1200)
            tab("Today")
            shot("22_today_dark", 1500)
            scrollTo("Surah Al-Mulk")
            rule.onNodeWithText("Surah Al-Mulk").performClick()
            shot("23_mulk_reader_dark")
            back()
            tab("Quran")
            shot("24_quran_dark")
            rule.onAllNodesWithText("Surah Al-Waqi’ah").onFirst().performClick()
            shot("25_mushaf_dark", 2000)
            back()
            tab("Playlist")
            shot("26_playlist_dark")
        }
    }
}
