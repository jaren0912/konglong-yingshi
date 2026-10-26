package com.konglong.yingshi

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.VideoView
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private val bgColor = Color.rgb(9, 15, 29)
    private val panelColor = Color.rgb(17, 27, 46)
    private val cardColor = Color.rgb(23, 37, 60)
    private val cyan = Color.rgb(0, 211, 220)
    private val white = Color.WHITE
    private val muted = Color.rgb(155, 171, 194)

    private lateinit var root: LinearLayout
    private lateinit var content: FrameLayout
    private lateinit var sidebar: LinearLayout

    private var currentPage = "首页"
    private var playlistUrl = ""
    private var currentVideoUrl = ""

    private val channels = mutableListOf<Channel>()

    data class Channel(
        val name: String,
        val url: String,
        val group: String = "其他"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playlistUrl = getPreferences(MODE_PRIVATE)
            .getString("playlist_url", "") ?: ""

        createLayout()
        showHome()
    }

    private fun createLayout() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(bgColor)
            isFocusable = true
        }

        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(20))
            setBackgroundColor(Color.rgb(12, 21, 38))
        }

        val logo = TextView(this).apply {
            text = "🐉 恐龙影视"
            textSize = 22f
            setTextColor(cyan)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(28))
        }

        sidebar.addView(
            logo,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(70)
            )
        )

        val menuItems = listOf(
            "首页" to "⌂",
            "电视直播" to "▣",
            "影视专区" to "▶",
            "设置" to "⚙"
        )

        for ((title, icon) in menuItems) {
            val item = TextView(this).apply {
                text = "$icon   $title"
                textSize = 16f
                setTextColor(white)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), 0, dp(8), 0)
                isFocusable = true
                isClickable = true
                background = rounded(cardColor, 12)
                setOnClickListener {
                    when (title) {
                        "首页" -> showHome()
                        "电视直播" -> showLive()
                        "影视专区" -> showMovies()
                        "设置" -> showSettings()
                    }
                }
            }

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
            )
            params.bottomMargin = dp(10)
            sidebar.addView(item, params)
        }

        val spacer = View(this)
        sidebar.addView(
            spacer,
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        val version = TextView(this).apply {
            text = "恐龙影视\nSMART TV"
            textSize = 11f
            setTextColor(muted)
            gravity = Gravity.CENTER
        }

        sidebar.addView(
            version,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(55)
            )
        )

        content = FrameLayout(this).apply {
            setPadding(dp(28), dp(22), dp(28), dp(22))
        }

        root.addView(
            sidebar,
            LinearLayout.LayoutParams(dp(205), ViewGroup.LayoutParams.MATCH_PARENT)
        )

        root.addView(
            content,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        )

        setContentView(root)
    }

    private fun showHome() {
        currentPage = "首页"
        content.removeAllViews()

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        page.addView(title("欢迎来到恐龙影视", 28f))
        page.addView(subtitle("探索精彩内容，享受大屏观影体验"))

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(28), dp(22), dp(28), dp(22))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(12, 72, 110),
                    Color.rgb(25, 45, 83),
                    Color.rgb(27, 32, 59)
                )
            ).apply {
                cornerRadius = dp(20).toFloat()
            }
        }

        val badge = TextView(this).apply {
            text = "  SMART ENTERTAINMENT  "
            textSize = 11f
            setTextColor(bgColor)
            setPadding(dp(8), dp(7), dp(8), dp(7))
            background = rounded(cyan, 6)
        }

        hero.addView(
            badge,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val heroTitle = TextView(this).apply {
            text = "精彩世界\n一触即达"
            textSize = 32f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(16), 0, dp(8))
        }

        hero.addView(heroTitle)

        hero.addView(
            TextView(this).apply {
                text = "开启你的家庭影院体验"
                textSize = 15f
                setTextColor(Color.rgb(210, 225, 243))
            }
        )

        val startButton = makeButton("进入电视直播  →")
        startButton.setOnClickListener { showLive() }

        val buttonParams = LinearLayout.LayoutParams(
            dp(210),
            dp(48)
        )
        buttonParams.topMargin = dp(22)
        hero.addView(startButton, buttonParams)

        val heroParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(250)
        )
        heroParams.topMargin = dp(22)
        page.addView(hero, heroParams)

        page.addView(
            sectionTitle("快捷入口"),
            marginParams(top = 24, bottom = 12)
        )

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(
            homeCard("▣", "电视直播", "浏览直播频道") {
                showLive()
            },
            LinearLayout.LayoutParams(0, dp(125), 1f).apply {
                marginEnd = dp(10)
            }
        )

        row.addView(
            homeCard("▶", "影视专区", "探索影视内容") {
                showMovies()
            },
            LinearLayout.LayoutParams(0, dp(125), 1f).apply {
                marginEnd = dp(10)
            }
        )

        row.addView(
            homeCard("⚙", "播放设置", "配置直播源") {
                showSettings()
            },
            LinearLayout.LayoutParams(0, dp(125), 1f)
        )

        page.addView(row)

        val scroll = ScrollView(this)
        scroll.addView(page)
        content.addView(scroll)
    }

    private fun showLive() {
        currentPage = "电视直播"
        content.removeAllViews()

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        page.addView(title("电视直播", 27f))
        page.addView(subtitle("选择频道，开始观看"))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val channelPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(panelColor, 14)
        }

        val channelHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        channelHeader.addView(
            TextView(this).apply {
                text = "频道列表"
                textSize = 17f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(0, dp(40), 1f)
        )

        val refreshButton = makeSmallButton("刷新")
        refreshButton.setOnClickListener { loadPlaylist() }
        channelHeader.addView(refreshButton)

        channelPanel.addView(channelHeader)

        val channelScroll = ScrollView(this)
        val channelList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        channelScroll.addView(channelList)

        channelPanel.addView(
            channelScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val playerPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(panelColor, 14)
        }

        val videoView = VideoView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFocusable = true
        }

        playerPanel.addView(
            videoView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        playerPanel.addView(
            TextView(this).apply {
                text = "播放提示：部分直播源可能需要等待加载"
                textSize = 12f
                setTextColor(muted)
                setPadding(0, dp(10), 0, dp(5))
            }
        )

        body.addView(
            channelPanel,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.37f)
                .apply { marginEnd = dp(14) }
        )

        body.addView(
            playerPanel,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.63f)
        )

        page.addView(
            body,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                topMargin = dp(18)
            }
        )

        content.addView(page)

        fun refreshChannels() {
            channelList.removeAllViews()

            if (channels.isEmpty()) {
                channelList.addView(
                    TextView(this).apply {
                        text = if (playlistUrl.isBlank()) {
                            "尚未配置直播源\n\n请到设置页面添加 M3U 地址"
                        } else {
                            "暂无频道\n\n请点击刷新重新加载"
                        }
                        textSize = 14f
                        setTextColor(muted)
                        gravity = Gravity.CENTER
                        setPadding(dp(12), dp(35), dp(12), dp(35))
                    }
                )
                return
            }

            for (channel in channels) {
                val item = TextView(this).apply {
                    text = "▶  ${channel.name}"
                    textSize = 14f
                    setTextColor(white)
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(10), 0, dp(8), 0)
                    background = rounded(cardColor, 8)
                    isFocusable = true
                    isClickable = true

                    setOnClickListener {
                        currentVideoUrl = channel.url

                        try {
                            videoView.setVideoURI(Uri.parse(channel.url))
                            videoView.setOnPreparedListener { player ->
                                player.isLooping = true
                                videoView.start()
                            }
                            videoView.setOnErrorListener { _, _, _ ->
                                showMessage(
                                    "播放失败",
                                    "该频道可能无法播放，请尝试其他频道或检查直播源。"
                                )
                                true
                            }
                        } catch (e: Exception) {
                            showMessage("播放失败", e.message ?: "未知错误")
                        }
                    }
                }

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(44)
                )
                params.bottomMargin = dp(8)
                channelList.addView(item, params)
            }
        }

        refreshChannels()

        if (channels.isEmpty() && playlistUrl.isNotBlank()) {
            loadPlaylist {
                if (currentPage == "电视直播") {
                    refreshChannels()
                }
            }
        }
    }

    private fun loadPlaylist(onComplete: (() -> Unit)? = null) {
        if (playlistUrl.isBlank()) {
            showMessage("尚未配置直播源", "请先到设置页面输入 M3U 地址。")
            onComplete?.invoke()
            return
        }

        thread {
            var errorMessage: String? = null
            val result = mutableListOf<Channel>()

            try {
                val connection = URL(playlistUrl)
                    .openConnection() as HttpURLConnection

                connection.connectTimeout = 12000
                connection.readTimeout = 15000
                connection.requestMethod = "GET"
                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0"
                )

                try {
                    connection.inputStream.bufferedReader().use { reader ->
                        var name = ""
                        var group = "其他"

                        while (true) {
                            val line = reader.readLine() ?: break
                            val text = line.trim()

                            when {
                                text.startsWith("#EXTINF", ignoreCase = true) -> {
                                    val groupMatch = Regex(
                                        """group-title=["']([^"']*)["']""",
                                        RegexOption.IGNORE_CASE
                                    ).find(text)

                                    group = groupMatch?.groupValues?.get(1)
                                        ?: "其他"

                                    name = text.substringAfterLast(",")
                                        .trim()
                                        .ifBlank { "未知频道" }
                                }

                                text.isNotBlank() &&
                                    !text.startsWith("#") &&
                                    (text.startsWith("http://") ||
                                     text.startsWith("https://")) -> {
                                    if (name.isBlank()) {
                                        name = "频道 ${result.size + 1}"
                                    }

                                    result.add(
                                        Channel(
                                            name = name,
                                            url = text,
                                            group = group
                                        )
                                    )

                                    name = ""
                                    group = "其他"
                                }
                            }
                        }
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "无法读取直播源"
            }

            runOnUiThread {
                channels.clear()
                channels.addAll(result)

                if (errorMessage != null) {
                    showMessage(
                        "读取直播源失败",
                        errorMessage ?: "请检查网络或地址。"
                    )
                } else if (channels.isEmpty()) {
                    showMessage(
                        "没有找到频道",
                        "请确认地址是有效的 M3U 播放列表。"
                    )
                }

                onComplete?.invoke()
            }
        }
    }

    private fun showMovies() {
        currentPage = "影视专区"
        content.removeAllViews()

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        page.addView(title("影视专区", 28f))
        page.addView(subtitle("打造你的家庭娱乐空间"))

        val banner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(20))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(24, 61, 103),
                    Color.rgb(36, 39, 77)
                )
            ).apply {
                cornerRadius = dp(18).toFloat()
            }
        }

        banner.addView(
            title("精彩影视，即将开启", 26f)
        )

        banner.addView(
            TextView(this).apply {
                text = "你可以在这里扩展电影、电视剧和点播内容。"
                textSize = 14f
                setTextColor(muted)
                setPadding(0, dp(10), 0, dp(18))
            }
        )

        val settingsButton = makeButton("配置播放源")
        settingsButton.setOnClickListener { showSettings() }

        banner.addView(
            settingsButton,
            LinearLayout.LayoutParams(dp(180), dp(46))
        )

        page.addView(
            banner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(220)
            ).apply {
                topMargin = dp(24)
            }
        )

        page.addView(
            sectionTitle("内容分类"),
            marginParams(top = 26, bottom = 12)
        )

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val categories = listOf(
            "🎬" to "电影",
            "📺" to "电视剧",
            "✨" to "精选推荐"
        )

        for ((icon, name) in categories) {
            row.addView(
                homeCard(icon, name, "敬请期待") {},
                LinearLayout.LayoutParams(0, dp(120), 1f).apply {
                    marginEnd = dp(10)
                }
            )
        }

        page.addView(row)
        content.addView(page)
    }

    private fun showSettings() {
        currentPage = "设置"
        content.removeAllViews()

        val scroll = ScrollView(this)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        page.addView(title("设置中心", 28f))
        page.addView(subtitle("管理直播源与应用设置"))

        val sourceCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(20))
            background = rounded(panelColor, 16)
        }

        sourceCard.addView(
            sectionTitle("直播源配置")
        )

        sourceCard.addView(
            TextView(this).apply {
                text = "输入有效的 M3U 播放列表网址。"
                textSize = 13f
                setTextColor(muted)
                setPadding(0, dp(8), 0, dp(16))
            }
        )

        val input = makeInput(playlistUrl)

        sourceCard.addView(
            input,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )

        val saveButton = makeButton("保存并加载频道")
        saveButton.setOnClickListener {
            val newUrl = input.text.toString().trim()

            if (newUrl.isNotBlank() &&
                !newUrl.startsWith("http://") &&
                !newUrl.startsWith("https://")) {
                showMessage(
                    "地址格式不正确",
                    "请输入以 http:// 或 https:// 开头的网址。"
                )
                return@setOnClickListener
            }

            playlistUrl = newUrl

            getPreferences(MODE_PRIVATE)
                .edit()
                .putString("playlist_url", playlistUrl)
                .apply()

            channels.clear()

            if (playlistUrl.isBlank()) {
                showMessage("已清除直播源", "你可以稍后重新添加直播源。")
                return@setOnClickListener
            }

            loadPlaylist {
                if (currentPage == "设置") {
                    showMessage(
                        "加载完成",
                        "已读取 ${channels.size} 个频道。"
                    )
                }
            }
        }

        sourceCard.addView(
            saveButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                topMargin = dp(16)
            }
        )

        val clearButton = makeSmallButton("清除直播源")
        clearButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("清除直播源")
                .setMessage("确定要删除已保存的直播源地址吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    playlistUrl = ""
                    channels.clear()
                    input.setText("")

                    getPreferences(MODE_PRIVATE)
                        .edit()
                        .remove("playlist_url")
                        .apply()

                    showMessage("已清除", "直播源地址已删除。")
                }
                .show()
        }

        sourceCard.addView(
            clearButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(42)
            ).apply {
                topMargin = dp(10)
            }
        )

        page.addView(
            sourceCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        )

        val aboutCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            background = rounded(panelColor, 16)
        }

        aboutCard.addView(sectionTitle("关于应用"))

        aboutCard.addView(
            TextView(this).apply {
                text = "恐龙影视\n版本 1.0\n\n专为大屏娱乐体验设计。"
                textSize = 14f
                setTextColor(muted)
                setLineSpacing(dp(5).toFloat(), 1f)
                setPadding(0, dp(12), 0, 0)
            }
        )

        page.addView(
            aboutCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(16)
            }
        )

        scroll.addView(page)
        content.addView(scroll)
    }

    private fun makeInput(value: String): EditText {
        return EditText(this).apply {
            setText(value)
            textSize = 14f
            setTextColor(white)
            setHintTextColor(muted)
            hint = "请输入 M3U 直播源地址"
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_URI
            setPadding(dp(14), dp(8), dp(14), dp(8))
            background = rounded(
                cardColor,
                10,
                Color.rgb(48, 65, 90)
            )
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    private fun makeButton(textValue: String): Button {
        return Button(this).apply {
            text = textValue
            textSize = 14f
            setTextColor(bgColor)
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(cyan, 10)
            isFocusable = true
        }
    }

    private fun makeSmallButton(textValue: String): Button {
        return Button(this).apply {
            text = textValue
            textSize = 12f
            setTextColor(white)
            isAllCaps = false
            background = rounded(cardColor, 8)
            isFocusable = true
        }
    }

    private fun title(textValue: String, size: Float): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = size
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun subtitle(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 14f
            setTextColor(muted)
            setPadding(0, dp(8), 0, 0)
        }
    }

    private fun sectionTitle(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 18f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun homeCard(
        icon: String,
        heading: String,
        description: String,
        action: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(12), dp(12))
            background = rounded(panelColor, 14)
            isFocusable = true
            isClickable = true

            addView(
                TextView(this@MainActivity).apply {
                    text = icon
                    textSize = 25f
                    setTextColor(cyan)
                }
            )

            addView(
                TextView(this@MainActivity).apply {
                    text = heading
                    textSize = 16f
                    setTextColor(white)
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(0, dp(5), 0, dp(3))
                }
            )

            addView(
                TextView(this@MainActivity).apply {
                    text = description
                    textSize = 12f
                    setTextColor(muted)
                }
            )

            setOnClickListener { action() }
        }
    }

    private fun rounded(
        color: Int,
        radius: Int,
        strokeColor: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()

            if (strokeColor != null) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun marginParams(
        top: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }
    }

    private fun showMessage(
        title: String,
        message: String
    ) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when (currentPage) {
                "首页" -> return super.onKeyDown(keyCode, event)
                else -> {
                    showHome()
                    return true
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
