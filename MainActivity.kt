package com.konglong.yingshi

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.*
import android.text.InputType
import android.content.res.ColorStateList
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class Channel(
    val name: String,
    val url: String,
    val group: String = "全部频道"
)

class MainActivity : Activity() {

    private val black = Color.rgb(12, 12, 14)
    private val gold = Color.rgb(218, 181, 105)
    private val card = Color.rgb(35, 32, 27)
    private val cream = Color.rgb(248, 239, 218)
    private val gray = Color.rgb(175, 175, 175)

    private val worker = Executors.newSingleThreadExecutor()

    private var liveSource = "http://bit.ly/jaren919"
    private var vodSource = "http://bit.ly/jaren22"

    private var channels: List<Channel> = emptyList()
    private var player: ExoPlayer? = null

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var status: TextView
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = black
        window.navigationBarColor = black
        buildShell()
        showHome()
    }

    private fun buildShell() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(black)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 14, 22, 12)
            setBackgroundColor(black)
        }

        header.addView(TextView(this).apply {
            text = "🦖  恐龙影视"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
        })

        header.addView(TextView(this).apply {
            text = "KONG LONG CINEMA"
            textSize = 11f
            letterSpacing = 0.2f
            setTextColor(gray)
        })

        root.addView(header)

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(card)
        }

        addNavButton(nav, "首页") { showHome() }
        addNavButton(nav, "直播") { loadLive() }
        addNavButton(nav, "点播") { loadVod("全部") }
        addNavButton(nav, "设置") { showSettings() }

        root.addView(nav)

        status = TextView(this).apply {
            text = "欢迎使用恐龙影视"
            textSize = 12f
            setTextColor(gold)
            setPadding(14, 8, 14, 8)
        }

        root.addView(status)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(black)
        }

        root.addView(
            content,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(root)
    }

    private fun addNavButton(
        row: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(gold)
            setBackgroundTintList(ColorStateList.valueOf(card))
            isFocusable = true
            setOnClickListener { action() }
        }

        row.addView(button, LinearLayout.LayoutParams(0, 48, 1f))
    }

    private fun showHome() {
        clearContent()
        status.text = "WELCOME TO KONG LONG CINEMA"

        val scroll = ScrollView(this)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 10, 8, 20)
        }

        page.addView(TextView(this).apply {
            text = "欢迎来到\n恐龙影视"
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            setPadding(8, 16, 8, 6)
        })

        page.addView(TextView(this).apply {
            text = "YOUR PRIVATE CINEMA"
            textSize = 12f
            letterSpacing = 0.2f
            setTextColor(gray)
            setPadding(10, 0, 8, 20)
        })

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(card)
        }

        hero.addView(TextView(this).apply {
            text = "✦  PREMIUM CINEMA"
            textSize = 13f
            setTextColor(gold)
        })

        hero.addView(TextView(this).apply {
            text = "好电影，\n值得大屏欣赏"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cream)
            setPadding(0, 18, 0, 12)
        })

        hero.addView(TextView(this).apply {
            text = "电影 · 电视剧 · 动漫 · 综艺 · 直播"
            textSize = 14f
            setTextColor(gray)
        })

        hero.addView(makeButton("▶  浏览点播", gold, Color.BLACK) {
            loadVod("全部")
        }, LinearLayout.LayoutParams(-1, 54).apply {
            setMargins(0, 22, 0, 0)
        })

        hero.addView(makeButton("📺  进入电视直播", card, cream) {
            loadLive()
        }, LinearLayout.LayoutParams(-1, 54).apply {
            setMargins(0, 10, 0, 0)
        })

        page.addView(hero, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 4, 0, 20)
        })

        page.addView(TextView(this).apply {
            text = "精选频道  /  EXPLORE"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            setPadding(4, 8, 4, 12)
        })

        addCategoryRow(page, "📺  电视直播", "🎬  电影点播",
            { loadLive() }, { loadVod("电影") })

        addCategoryRow(page, "📽️  电视剧", "🎌  动漫",
            { loadVod("电视剧") }, { loadVod("动漫") })

        addCategoryRow(page, "🎤  综艺节目", "⚙️  资源设置",
            { loadVod("综艺") }, { showSettings() })

        page.addView(TextView(this).apply {
            text = "KONG LONG CINEMA  •  BLACK & GOLD"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            setPadding(8, 26, 8, 12)
        })

        scroll.addView(page)
        content.addView(scroll, LinearLayout.LayoutParams(-1, -1))
    }

    private fun addCategoryRow(
        page: LinearLayout,
        first: String,
        second: String,
        action1: () -> Unit,
        action2: () -> Unit
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(
            makeButton(first, card, cream, action1),
            LinearLayout.LayoutParams(0, 76, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
        )

        row.addView(
            makeButton(second, card, cream, action2),
            LinearLayout.LayoutParams(0, 76, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
        )

        page.addView(row)
    }

    private fun makeButton(
        label: String,
        background: Int,
        foreground: Int,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 15f
            isAllCaps = false
            gravity = Gravity.CENTER
            setTextColor(foreground)
            setBackgroundTintList(ColorStateList.valueOf(background))
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun clearContent() {
        content.removeAllViews()
    }

    private fun loadLive() {
        clearContent()
        status.text = "正在加载电视直播……"

        content.addView(TextView(this).apply {
            text = "电视直播"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            setPadding(8, 8, 8, 12)
        })

        val list = ListView(this)
        list.setBackgroundColor(black)
        content.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))

        worker.execute {
            try {
                val text = fetchText(liveSource)
                val parsed = parseM3U(text)

                runOnUiThread {
                    channels = parsed

                    if (parsed.isEmpty()) {
                        status.text = "没有找到频道，请检查直播源"
                        return@runOnUiThread
                    }

                    status.text = "已加载 ${parsed.size} 个频道"

                    list.adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        parsed.map { it.name }
                    )

                    list.setOnItemClickListener { _, _, position, _ ->
                        playVideo(parsed[position].url)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = "直播加载失败，请检查网络或直播源"
                }
            }
        }
    }

    private fun parseM3U(text: String): List<Channel> {
        val result = mutableListOf<Channel>()
        var name = "电视直播"
        var group = "全部频道"

        for (raw in text.lines()) {
            val line = raw.trim()

            if (line.startsWith("#EXTINF", true)) {
                name = line.substringAfterLast(",", "电视直播").trim()

                val match = Regex("""group-title="([^"]*)"""").find(line)
                group = match?.groupValues?.get(1) ?: "全部频道"
            } else if (
                line.startsWith("http://") || line.startsWith("https://")
            ) {
                result.add(Channel(name, line, group))
                name = "电视直播"
            }
        }

        return result
    }

    private fun loadVod(category: String) {
        clearContent()
        status.text = "正在检查点播资源……"

        content.addView(TextView(this).apply {
            text = if (category == "全部") "电影与点播" else category
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            setPadding(8, 8, 8, 12)
        })

        content.addView(TextView(this).apply {
            text = "点播资源连接检查\n\n若无法连接，请进入资源设置检查点播地址。"
            textSize = 16f
            setTextColor(cream)
            setPadding(16, 20, 16, 20)
        })

        worker.execute {
            try {
                fetchText(vodSource)
                runOnUiThread {
                    status.text = "点播源连接成功，但尚未解析影片目录"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = "点播源连接失败，请检查地址或网络"
                }
            }
        }
    }

    private fun playVideo(url: String) {
        try {
            clearContent()

            playerView = PlayerView(this).apply {
                useController = true
            }

            content.addView(playerView, LinearLayout.LayoutParams(-1, -1))

            player?.release()
            player = ExoPlayer.Builder(this).build().also {
                playerView.player = it
                it.setMediaItem(MediaItem.fromUri(url))
                it.prepare()
                it.playWhenReady = true
            }

            status.text = "正在播放"
        } catch (e: Exception) {
            status.text = "播放失败，请检查视频地址"
        }
    }

    private fun showSettings() {
        clearContent()

        content.addView(TextView(this).apply {
            text = "资源设置"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            setPadding(8, 8, 8, 18)
        })

        content.addView(TextView(this).apply {
            text = "电视直播源地址"
            setTextColor(cream)
        })

        val liveInput = EditText(this).apply {
            setText(liveSource)
            setTextColor(cream)
            setHintTextColor(gray)
            hint = "输入直播源网址"
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }
        content.addView(liveInput)

        content.addView(TextView(this).apply {
            text = "点播资源地址"
            setTextColor(cream)
            setPadding(0, 18, 0, 0)
        })

        val vodInput = EditText(this).apply {
            setText(vodSource)
            setTextColor(cream)
            setHintTextColor(gray)
            hint = "输入点播源网址"
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }
        content.addView(vodInput)

        content.addView(makeButton("保存设置", gold, Color.BLACK) {
            liveSource = liveInput.text.toString().trim()
            vodSource = vodInput.text.toString().trim()
            status.text = "资源地址已保存至当前运行状态"
            showHome()
        }, LinearLayout.LayoutParams(-1, 54).apply {
            setMargins(0, 24, 0, 0)
        })
    }

    private fun fetchText(address: String): String {
        val connection = URL(address).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")

        return try {
            connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
        } finally {
            connection.disconnect()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showHome()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        player?.release()
        player = null
        worker.shutdownNow()
        super.onDestroy()
    }
}
