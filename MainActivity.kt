package com.konglong.yingshi

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.InputType
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

    private val bg = Color.rgb(9, 14, 27)
    private val panel = Color.rgb(17, 25, 43)
    private val panelLight = Color.rgb(25, 36, 57)
    private val cyan = Color.rgb(48, 210, 220)
    private val white = Color.rgb(244, 248, 255)
    private val muted = Color.rgb(151, 165, 188)
    private val worker = Executors.newSingleThreadExecutor()

    private var liveSource = "http://bit.ly/jaren919"
    private var vodSource = "http://bit.ly/jaren22"

    private val channels = mutableListOf<Channel>()

    private lateinit var root: LinearLayout
    private lateinit var sidebar: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var statusText: TextView

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    private var currentPage = "home"
    private var currentChannel: Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0
        buildShell()
        showHome()
    }

    private fun buildShell() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(bg)
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }

        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            setPadding(dp(18), dp(28), dp(18), dp(20))
            setBackgroundColor(Color.rgb(12, 19, 34))
        }

        val brand = TextView(this).apply {
            text = "恐龙影视"
            textSize = 23f
            setTextColor(cyan)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(10))
        }

        val subtitle = TextView(this).apply {
            text = "DINOSAUR TV"
            textSize = 10f
            setTextColor(muted)
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
            setPadding(0, 0, 0, dp(32))
        }

        sidebar.addView(
            brand,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        sidebar.addView(subtitle)

        addSideButton("⌂", "首页", "home")
        addSideButton("▶", "电视直播", "live")
        addSideButton("▣", "影视点播", "vod")
        addSideButton("⚙", "系统设置", "settings")

        val spacer = View(this)
        sidebar.addView(
            spacer,
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        val version = TextView(this).apply {
            text = "PREMIUM TV\n版本 2.0"
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setLineSpacing(dp(4).toFloat(), 1f)
        }
        sidebar.addView(version)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(26), dp(22), dp(26), dp(18))
            setBackgroundColor(bg)
            isFocusable = false
        }

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        statusText = TextView(this).apply {
            text = "欢迎使用恐龙影视"
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }

        content.addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = false
            addView(body)
        }

        content.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            sidebar,
            LinearLayout.LayoutParams(dp(180), ViewGroup.LayoutParams.MATCH_PARENT)
        )

        root.addView(
            content,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        )

        setContentView(root)

        bodyContainer = body
    }

    private lateinit var bodyContainer: LinearLayout

    private fun addSideButton(
        icon: String,
        title: String,
        page: String
    ) {
        val button = TextView(this).apply {
            text = "$icon   $title"
            textSize = 15f
            setTextColor(if (page == "home") cyan else muted)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), 0, dp(8), 0)
            isFocusable = true
            isClickable = true
            background = rounded(
                if (page == "home") panelLight else Color.TRANSPARENT,
                12
            )

            setOnFocusChangeListener { view, hasFocus ->
                view.background = rounded(
                    if (hasFocus) panelLight else Color.TRANSPARENT,
                    12
                )
                setTextColor(if (hasFocus) cyan else muted)
            }

            setOnClickListener {
                when (page) {
                    "home" -> showHome()
                    "live" -> loadLive()
                    "vod" -> loadVod("全部影片")
                    "settings" -> showSettings()
                }
            }
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(48)
        )
        params.bottomMargin = dp(8)
        sidebar.addView(button, params)
    }

    private fun rounded(
        color: Int,
        radius: Int,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun clearBody() {
        bodyContainer.removeAllViews()
    }

    private fun setStatus(message: String) {
        statusText.text = message
    }

    private fun addTitle(
        title: String,
        subtitle: String = ""
    ) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(18))
        }

        val heading = TextView(this).apply {
            text = title
            textSize = 27f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }

        wrapper.addView(heading)

        if (subtitle.isNotBlank()) {
            val sub = TextView(this).apply {
                text = subtitle
                textSize = 12f
                setTextColor(muted)
                setPadding(0, dp(6), 0, 0)
            }
            wrapper.addView(sub)
        }

        bodyContainer.addView(wrapper)
    }

    private fun makeButton(
        label: String,
        onClick: () -> Unit,
        primary: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 14f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (primary) bg else white)
            setPadding(dp(18), dp(12), dp(18), dp(12))
            isFocusable = true
            isClickable = true

            background = rounded(
                if (primary) cyan else panelLight,
                10
            )

            setOnFocusChangeListener { view, focused ->
                view.background = rounded(
                    if (focused) Color.rgb(75, 231, 238)
                    else if (primary) cyan else panelLight,
                    10,
                    if (focused) white else Color.TRANSPARENT
                )
                view.scaleX = if (focused) 1.03f else 1f
                view.scaleY = if (focused) 1.03f else 1f
            }

            setOnClickListener { onClick() }
        }
    }

    private fun addButtonRow(
        vararg buttons: Pair<String, () -> Unit>
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        buttons.forEachIndexed { index, item ->
            val button = makeButton(
                item.first,
                item.second,
                index == 0
            )
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(46)
            )
            params.rightMargin = dp(12)
            row.addView(button, params)
        }

        bodyContainer.addView(row)
    }

    private fun showHome() {
        currentPage = "home"
        clearBody()
        setStatus("恐龙影视  ·  为你打造沉浸式观影体验")

        addTitle(
            "发现精彩内容",
            "WELCOME BACK  ·  开启你的大屏娱乐体验"
        )

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(30), dp(24), dp(30), dp(24))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.rgb(18, 57, 81),
                    Color.rgb(24, 38, 75),
                    Color.rgb(38, 31, 70)
                )
            ).apply {
                cornerRadius = dp(20).toFloat()
            }
            minimumHeight = dp(205)
        }

        val tag = TextView(this).apply {
            text = "DINOSAUR  ORIGINAL"
            textSize = 11f
            letterSpacing = 0.15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cyan)
        }

        val heroTitle = TextView(this).apply {
            text = "让精彩，即刻呈现"
            textSize = 31f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(0, dp(12), 0, dp(8))
        }

        val heroDesc = TextView(this).apply {
            text = "直播频道 · 家庭影院 · 大屏娱乐"
            textSize = 14f
            setTextColor(Color.rgb(208, 224, 242))
        }

        hero.addView(tag)
        hero.addView(heroTitle)
        hero.addView(heroDesc)

        val heroButton = makeButton("立即观看  →", {
            loadLive()
        }, true)

        val heroButtonParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dp(46)
        )
        heroButtonParams.topMargin = dp(18)
        hero.addView(heroButton, heroButtonParams)

        bodyContainer.addView(
            hero,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(24)
            }
        )

        addTitle("选择你的娱乐方式", "CHOOSE YOUR EXPERIENCE")

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        addHomeCard(
            row,
            "▶",
            "电视直播",
            "观看直播频道",
            "进入直播"
        ) {
            loadLive()
        }

        addHomeCard(
            row,
            "▣",
            "影视点播",
            "探索影片内容",
            "进入点播"
        ) {
            loadVod("全部影片")
        }

        addHomeCard(
            row,
            "⚙",
            "播放设置",
            "管理影视源地址",
            "打开设置"
        ) {
            showSettings()
        }

        bodyContainer.addView(row)

        val note = TextView(this).apply {
            text = "提示：实际可播放的内容取决于你配置的影视源。"
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(22), 0, dp(12))
        }
        bodyContainer.addView(note)

        heroButton.requestFocus()
    }

    private fun addHomeCard(
        row: LinearLayout,
        icon: String,
        title: String,
        description: String,
        action: String,
        onClick: () -> Unit
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(panel, 14)
            isFocusable = true
            isClickable = true

            setOnFocusChangeListener { view, focused ->
                view.background = rounded(
                    if (focused) panelLight else panel,
                    14,
                    if (focused) cyan else Color.TRANSPARENT
                )
            }

            setOnClickListener { onClick() }
        }

        val iconView = TextView(this).apply {
            text = icon
            textSize = 28f
            setTextColor(cyan)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(0, dp(16), 0, dp(8))
        }

        val descView = TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(muted)
        }

        val actionView = TextView(this).apply {
            text = "$action  →"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cyan)
            setPadding(0, dp(18), 0, 0)
        }

        card.addView(iconView)
        card.addView(titleView)
        card.addView(descView)
        card.addView(actionView)

        val params = LinearLayout.LayoutParams(
            0,
            dp(175),
            1f
        )
        params.rightMargin = dp(12)
        row.addView(card, params)
    }

    private fun loadLive() {
        currentPage = "live"
        clearBody()
        setStatus("电视直播  ·  正在加载频道列表")

        addTitle("电视直播", "LIVE TV  ·  选择频道，开始观看")

        val loading = TextView(this).apply {
            text = "正在连接直播源，请稍候……"
            textSize = 15f
            setTextColor(muted)
            setPadding(0, dp(20), 0, dp(20))
        }
        bodyContainer.addView(loading)

        worker.execute {
            val result = try {
                fetchText(liveSource)
            } catch (e: Exception) {
                ""
            }

            val parsed = parseM3U(result)

            runOnUiThread {
                if (currentPage != "live") return@runOnUiThread

                clearBody()
                addTitle("电视直播", "LIVE TV  ·  选择频道，开始观看")

                if (parsed.isEmpty()) {
                    val empty = TextView(this).apply {
                        text = "暂时无法取得频道列表。\n\n请检查网络连接或在系统设置中确认直播源地址。"
                        textSize = 15f
                        setTextColor(muted)
                        setPadding(0, dp(20), 0, dp(20))
                        setLineSpacing(dp(6).toFloat(), 1f)
                    }
                    bodyContainer.addView(empty)

                    addButtonRow(
                        "重新加载" to { loadLive() },
                        "返回首页" to { showHome() },
                        "设置源地址" to { showSettings() }
                    )
                    setStatus("直播源连接失败或没有可用频道")
                    return@runOnUiThread
                }

                channels.clear()
                channels.addAll(parsed)

                setStatus("已载入 ${parsed.size} 个频道")

                val list = ListView(this).apply {
                    divider = GradientDrawable().apply {
                        setColor(Color.rgb(32, 43, 61))
                        setSize(1, dp(1))
                    }
                    dividerHeight = dp(1)
                    setBackgroundColor(bg)
                    isVerticalScrollBarEnabled = false
                    choiceMode = ListView.CHOICE_MODE_SINGLE

                    adapter = object : BaseAdapter() {
                        override fun getCount(): Int = parsed.size
                        override fun getItem(position: Int): Any = parsed[position]
                        override fun getItemId(position: Int): Long = position.toLong()

                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val item = parsed[position]

                            val row = LinearLayout(this@MainActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                gravity = Gravity.CENTER_VERTICAL
                                setPadding(dp(14), dp(10), dp(14), dp(10))
                                background = rounded(panel, 10)
                                isFocusable = true
                                isClickable = true

                                setOnFocusChangeListener { view, focused ->
                                    view.background = rounded(
                                        if (focused) panelLight else panel,
                                        10,
                                        if (focused) cyan else Color.TRANSPARENT
                                    )
                                }
                            }

                            val number = TextView(this@MainActivity).apply {
                                text = "%02d".format(position + 1)
                                textSize = 15f
                                setTextColor(cyan)
                                typeface = Typeface.DEFAULT_BOLD
                                gravity = Gravity.CENTER
                            }

                            row.addView(
                                number,
                                LinearLayout.LayoutParams(dp(48), dp(42))
                            )

                            val labels = LinearLayout(this@MainActivity).apply {
                                orientation = LinearLayout.VERTICAL
                                gravity = Gravity.CENTER_VERTICAL
                            }

                            val name = TextView(this@MainActivity).apply {
                                text = item.name
                                textSize = 15f
                                setTextColor(white)
                                typeface = Typeface.DEFAULT_BOLD
                                maxLines = 1
                            }

                            val group = TextView(this@MainActivity).apply {
                                text = item.group
                                textSize = 11f
                                setTextColor(muted)
                                setPadding(0, dp(4), 0, 0)
                                maxLines = 1
                            }

                            labels.addView(name)
                            labels.addView(group)

                            row.addView(
                                labels,
                                LinearLayout.LayoutParams(
                                    0,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                            )

                            val play = TextView(this@MainActivity).apply {
                                text = "▶"
                                textSize = 17f
                                setTextColor(cyan)
                                gravity = Gravity.CENTER
                            }

                            row.addView(play, LinearLayout.LayoutParams(dp(36), dp(42)))

                            return row
                        }
                    }

                    onItemClickListener =
                        AdapterView.OnItemClickListener { _, _, position, _ ->
                            val selected = parsed[position]
                            currentChannel = selected
                            playVideo(selected.url)
                        }
                }

                bodyContainer.addView(
                    list,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(440)
                    )
                )

                list.requestFocus()
            }
        }
    }

    private fun parseM3U(text: String): List<Channel> {
        val result = mutableListOf<Channel>()
        var name = ""
        var group = "全部频道"

        text.lineSequence().forEach { raw ->
            val line = raw.trim()

            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    val groupMatch = Regex(
                        """group-title\s*=\s*["']([^"']*)["']""",
                        RegexOption.IGNORE_CASE
                    ).find(line)

                    group = groupMatch?.groupValues?.get(1)
                        ?.takeIf { it.isNotBlank() }
                        ?: "全部频道"

                    name = line.substringAfterLast(",").trim()

                    if (name.isBlank()) {
                        name = "未知频道"
                    }
                }

                line.isNotBlank() &&
                    !line.startsWith("#") &&
                    (line.startsWith("http://") ||
                     line.startsWith("https://")) -> {

                    result.add(
                        Channel(
                            name = name.ifBlank { "频道 ${result.size + 1}" },
                            url = line,
                            group = group
                        )
                    )

                    name = ""
                    group = "全部频道"
                }
            }
        }

        return result
    }

    private fun loadVod(category: String) {
        currentPage = "vod"
        clearBody()
        setStatus("影视点播  ·  点播源管理")

        addTitle(
            "影视点播",
            "MOVIE CENTER  ·  发现你的下一部精彩作品"
        )

        val banner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(22), dp(24), dp(22))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(34, 49, 91),
                    Color.rgb(18, 70, 88)
                )
            ).apply {
                cornerRadius = dp(16).toFloat()
            }
        }

        val bannerTitle = TextView(this).apply {
            text = "你的家庭影院"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        }

        val bannerDesc = TextView(this).apply {
            text = "连接影视源，畅享大屏观影体验。"
            textSize = 13f
            setTextColor(Color.rgb(210, 225, 241))
            setPadding(0, dp(8), 0, dp(18))
        }

        banner.addView(bannerTitle)
        banner.addView(bannerDesc)

        val refresh = makeButton("检查点播源", {
            checkVodSource()
        }, true)

        banner.addView(
            refresh,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(44)
            )
        )

        bodyContainer.addView(
            banner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(24)
            }
        )

        addTitle("影片分类", "MOVIE COLLECTION")

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        addHomeCard(
            row,
            "🎬",
            "全部影片",
            "浏览点播内容",
            "检查点播源"
        ) {
            checkVodSource()
        }

        addHomeCard(
            row,
            "★",
            "我的收藏",
            "收藏功能预留",
            "敬请期待"
        ) {
            showMessage("收藏功能暂未接入")
        }

        bodyContainer.addView(row)

        val info = TextView(this).apply {
            text = "目前保留了点播源地址设置和连接检查功能。要显示真实电影目录，还需要知道你的点播源 API 或数据格式；不能仅凭一个地址自动判断影片列表。"
            textSize = 13f
            setTextColor(muted)
            setLineSpacing(dp(5).toFloat(), 1f)
            setPadding(0, dp(22), 0, dp(12))
        }

        bodyContainer.addView(info)
    }

    private fun checkVodSource() {
        setStatus("正在检查点播源……")

        worker.execute {
            val result = try {
                fetchText(vodSource)
            } catch (e: Exception) {
                ""
            }

            runOnUiThread {
                if (currentPage != "vod") return@runOnUiThread

                if (result.isNotBlank()) {
                    showMessage(
                        "点播源已返回内容。\n\n但要显示真实影片目录，还需要识别该源的数据格式。"
                    )
                    setStatus("点播源已返回数据")
                } else {
                    showMessage(
                        "暂时无法取得点播源内容。\n\n请检查网络和点播源地址。"
                    )
                    setStatus("点播源连接失败")
                }
            }
        }
    }

    private fun showSettings() {
        currentPage = "settings"
        clearBody()
        setStatus("系统设置  ·  管理影视源")

        addTitle("系统设置", "SETTINGS  ·  配置你的影视内容")

        val info = TextView(this).apply {
            text = "影视源配置"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(0, 0, 0, dp(16))
        }
        bodyContainer.addView(info)

        val liveLabel = makeLabel("直播源地址")

        bodyContainer.addView(liveLabel)

        val liveInput = makeInput(liveSource)
        bodyContainer.addView(
            liveInput,
            inputParams()
        )

        val vodLabel = makeLabel("点播源地址")

        val vodInput = makeInput(vodSource)
        bodyContainer.addView(vodLabel)
        bodyContainer.addView(
            vodInput,
            inputParams()
        )

        val hint = TextView(this).apply {
            text = "请填写有效的影视源地址。修改后会在当前运行期间生效；重新安装或重启后是否保留，取决于后续是否加入本地保存功能。"
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(12), 0, dp(20))
            setLineSpacing(dp(4).toFloat(), 1f)
        }
        bodyContainer.addView(hint)

        addButtonRow(
            "保存并测试直播源" to {
                val address = liveInput.text.toString().trim()
                if (address.isNotBlank()) {
                    liveSource = address
                    showMessage("直播源地址已更新")
                    loadLive()
                } else {
                    showMessage("请输入直播源地址")
                }
            },
            "保存点播源" to {
                val address = vodInput.text.toString().trim()
                if (address.isNotBlank()) {
                    vodSource = address
                    showMessage("点播源地址已更新")
                } else {
                    showMessage("请输入点播源地址")
                }
            }
        )

        val back = makeButton("返回首页", { showHome() })
        val backParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dp(46)
        )
        backParams.topMargin = dp(16)
        bodyContainer.addView(back, backParams)

        liveInput.requestFocus()
    }

    private fun makeLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(muted)
            setPadding(0, dp(10), 0, dp(8))
        }
    }

    private fun makeInput(value: String): EditText {
        return EditText(this).apply {
            setText(value)
            textSize = 14f
            setTextColor(white)
            setHintTextColor(muted)
            hint = "请输入地址"
            singleLine = true
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_URI
            setPadding(dp(14), dp(8), dp(14), dp(8))
            background = rounded(panelLight, 10, Color.rgb(48, 65, 90))
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    private fun inputParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(50)
        ).apply {
            bottomMargin = dp(8)
        }
    }

    private fun playVideo(url: String) {
        if (url.isBlank()) {
            showMessage("播放地址为空")
            return
        }

        currentPage = "player"
        clearBody()
        setStatus(
            "正在播放：${currentChannel?.name ?: "影视内容"}"
        )

        val heading = TextView(this).apply {
            text = currentChannel?.name ?: "正在播放"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(0, dp(8), 0, dp(14))
        }
        bodyContainer.addView(heading)

        val pv = PlayerView(this).apply {
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            setBackgroundColor(Color.BLACK)
        }

        bodyContainer.addView(
            pv,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(330)
            )
        )

        player?.release()
        player = ExoPlayer.Builder(this).build()
        playerView = pv
        pv.player = player

        player?.setMediaItem(MediaItem.fromUri(url))
        player?.prepare()
        player?.playWhenReady = true

        val back = makeButton("返回频道列表", {
            loadLive()
        })

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dp(46)
        )
        params.topMargin = dp(18)
        bodyContainer.addView(back, params)

        back.requestFocus()
    }

    private fun showMessage(message: String) {
        if (isFinishing) return

        android.app.AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }

    private fun fetchText(address: String): String {
        val connection = URL(address).openConnection() as HttpURLConnection

        return try {
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
            )
            connection.connect()

            if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use {
                    it.readText()
                }
            } else {
                ""
            }
        } finally {
            connection.disconnect()
        }
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when (currentPage) {
                "home" -> return super.onKeyDown(keyCode, event)
                "live" -> {
                    showHome()
                    return true
                }
                "vod" -> {
                    showHome()
                    return true
                }
                "settings" -> {
                    showHome()
                    return true
                }
                "player" -> {
                    player?.stop()
                    loadLive()
                    return true
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        playerView?.player = null
        player?.release()
        player = null
        worker.shutdownNow()
        super.onDestroy()
    }
}
