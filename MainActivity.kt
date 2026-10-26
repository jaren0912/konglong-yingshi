package com.konglong.yingshi

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.InputType
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

data class Channel(val name: String, val url: String, val group: String = "全部频道")

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 27, 45)
    private val panel = Color.rgb(14, 54, 76)
    private val teal = Color.rgb(22, 196, 190)
    private val pale = Color.rgb(225, 249, 247)
    private val worker = Executors.newSingleThreadExecutor()
    private val liveSourceDefault = "http://bit.ly/jaren919"
    private val vodSourceDefault = "http://bit.ly/jaren22"
    private var liveSource = liveSourceDefault
    private var vodSource = vodSourceDefault
    private var channels: List<Channel> = emptyList()
    private var player: ExoPlayer? = null
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var status: TextView
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildShell()
        showHome()
    }

    private fun buildShell() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 20, 28, 20)
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "🦖 恐龙影视"
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(4, 8, 28, 8)
        }
        header.addView(brand)
        addNavButton(header, "首页") { showHome() }
        addNavButton(header, "电视直播") { loadLive() }
        addNavButton(header, "电影点播") { loadVod("电影") }
        addNavButton(header, "电视剧") { loadVod("电视剧") }
        addNavButton(header, "设置") { showSettings() }
        root.addView(header, LinearLayout.LayoutParams(-1, -2))
        status = TextView(this).apply {
            text = "欢迎使用恐龙影视"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(6, 6, 6, 10)
        }
        root.addView(status)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
        }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun addNavButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val b = Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel))
            setOnClickListener { action() }
            isFocusable = true
            isFocusableInTouchMode = true
        }
        val lp = LinearLayout.LayoutParams(-2, 54)
        lp.setMargins(5, 0, 5, 0)
        parent.addView(b, lp)
    }

    private fun clearContent() {
        content.removeAllViews()
    }

    private fun showHome() {
        clearContent()
        val hero = TextView(this).apply {
            text = "🦖  恐龙影视\\n\\n直播 · 电影 · 电视剧\\n\\n使用遥控器选择上方栏目开始观看"
            textSize = 27f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(panel)
            setPadding(20, 25, 20, 25)
        }
        content.addView(hero, LinearLayout.LayoutParams(-1, 0, 1f))
        status.text = "直播源与点播源已预设；首次使用请确认网络和源地址可访问。"
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        addBigButton(row, "打开直播") { loadLive() }
        addBigButton(row, "浏览点播") { loadVod("全部") }
        content.addView(row)
    }

    private fun addBigButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val b = Button(this).apply {
            text = label
            textSize = 20f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(teal))
            setOnClickListener { action() }
        }
        val lp = LinearLayout.LayoutParams(0, 68, 1f)
        lp.setMargins(12, 12, 12, 4)
        parent.addView(b, lp)
    }

    private fun loadLive() {
        clearContent()
        status.text = "正在读取直播源……"
        val loading = TextView(this).apply {
            text = "正在读取 M3U / M3U8 频道列表…"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(pale)
        }
        content.addView(loading, LinearLayout.LayoutParams(-1, -1))
        worker.execute {
            try {
                val text = fetchText(liveSource)
                val parsed = parseM3u(text)
                runOnUiThread {
                    channels = parsed
                    showChannels(parsed)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    clearContent()
                    status.text = "直播源读取失败"
                    addMessage("无法读取直播源。请检查链接是否有效、是否需要授权，或在“设置”中修改地址。\\n\\n原因：${e.message ?: "未知错误"}")
                }
            }
        }
    }

    private fun showChannels(list: List<Channel>) {
        clearContent()
        status.text = "已读取 ${list.size} 个频道"
        if (list.isEmpty()) {
            addMessage("没有识别到 M3U 频道。请确认链接返回的是 M3U 文本，而不是网页或登录页面。")
            return
        }
        val split = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val categories = LinkedHashSet<String>()
        categories.add("全部频道")
        list.forEach { categories.add(it.group) }
        val catBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(panel)
        }
        categories.forEach { group ->
            val b = Button(this).apply {
                text = group
                isAllCaps = false
                setTextColor(Color.WHITE)
                setOnClickListener { showChannelRows(list.filter { group == "全部频道" || it.group == group }) }
            }
            catBox.addView(b)
        }
        val scrollCat = ScrollView(this).apply { addView(catBox) }
        split.addView(scrollCat, LinearLayout.LayoutParams(250, -1))
        val rows = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scrollRows = ScrollView(this).apply { addView(rows) }
        split.addView(scrollRows, LinearLayout.LayoutParams(0, -1, 1f))
        content.addView(split, LinearLayout.LayoutParams(-1, -1))
        channelRowsContainer = rows
        showChannelRows(list)
    }

    private var channelRowsContainer: LinearLayout? = null

    private fun showChannelRows(list: List<Channel>) {
        val box = channelRowsContainer ?: return
        box.removeAllViews()
        list.forEachIndexed { index, ch ->
            val b = Button(this).apply {
                text = "${index + 1}.  ${ch.name}"
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                isAllCaps = false
                textSize = 17f
                setTextColor(Color.WHITE)
                setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel))
                setOnClickListener { play(ch.url, ch.name) }
            }
            box.addView(b, LinearLayout.LayoutParams(-1, 58))
        }
    }

    private fun play(url: String, title: String) {
        clearContent()
        status.text = "正在播放：$title"
        player?.release()
        player = ExoPlayer.Builder(this).build()
        playerView = PlayerView(this).apply {
            player = this@MainActivity.player
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
        }
        content.addView(playerView, LinearLayout.LayoutParams(-1, -1))
        player?.setMediaItem(MediaItem.fromUri(url))
        player?.prepare()
        player?.playWhenReady = true
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                runOnUiThread { status.text = "播放失败：${error.errorCodeName}（可能是源格式、网络或授权问题）" }
            }
        })
    }

    private fun loadVod(section: String) {
        clearContent()
        status.text = "正在读取点播接口……"
        addMessage("点播源已设置为：$vodSource\\n\\n正在检查 CMS 数据格式。")
        worker.execute {
            try {
                val body = fetchText(vodSource)
                val summary = inspectVod(body)
                runOnUiThread {
                    clearContent()
                    status.text = "点播源已读取，接口格式需要确认"
                    addMessage("已取得点播源响应（${body.length} 字符）。\\n\\n$summary\\n\\n若这里显示的是一段 CMS API 地址或配置文件，下一步需要按实际 CMS 类型解析分类、海报和播放链接。")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    clearContent()
                    status.text = "点播源读取失败"
                    addMessage("无法读取点播源。请检查短链接是否有效、是否需要授权。\\n\\n原因：${e.message ?: "未知错误"}")
                }
            }
        }
    }

    private fun inspectVod(body: String): String {
        val s = body.trim()
        return when {
            s.startsWith("http://") || s.startsWith("https://") ->
                "检测结果：响应内容看起来是一个接口地址。\\n地址：$s"
            s.startsWith("{") -> try {
                val json = JSONObject(s)
                "检测结果：JSON 接口。\\n顶层字段：" + json.keys().asSequence().take(20).joinToString(", ")
            } catch (_: Exception) { "检测结果：类似 JSON，但解析失败。" }
            s.startsWith("[") -> try {
                val arr = JSONArray(s)
                "检测结果：JSON 数组，共 ${arr.length()} 项。"
            } catch (_: Exception) { "检测结果：类似 JSON 数组，但解析失败。" }
            s.contains("#EXTM3U") -> "检测结果：这是 M3U 内容，不像点播 CMS 接口。"
            else -> "响应开头：${s.take(500)}"
        }
    }

    private fun showSettings() {
        clearContent()
        status.text = "资源设置"
        val liveEdit = EditText(this).apply {
            setText(liveSource); hint = "M3U 直播源网址"
            setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }
        val vodEdit = EditText(this).apply {
            setText(vodSource); hint = "CMS 点播源网址"
            setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }
        content.addView(label("直播源网址"))
        content.addView(liveEdit, LinearLayout.LayoutParams(-1, 58))
        content.addView(label("点播源网址"))
        content.addView(vodEdit, LinearLayout.LayoutParams(-1, 58))
        val save = Button(this).apply {
            text = "保存并测试直播源"
            isAllCaps = false
            setOnClickListener {
                liveSource = liveEdit.text.toString().trim()
                vodSource = vodEdit.text.toString().trim()
                getPreferences(MODE_PRIVATE).edit()
                    .putString("live", liveSource).putString("vod", vodSource).apply()
                status.text = "地址已保存"
                loadLive()
            }
        }
        content.addView(save)
        val note = TextView(this).apply {
            text = "提示：此原型暂时只在当前安装设备保存地址。商业发行前应加入 HTTPS、授权验证、错误日志和隐私说明。"
            setTextColor(Color.LTGRAY)
            textSize = 15f
            setPadding(0, 18, 0, 0)
        }
        content.addView(note)
    }

    private fun label(t: String) = TextView(this).apply {
        text = t; textSize = 17f; setTextColor(teal); setPadding(2, 10, 2, 4)
    }

    private fun addMessage(text: String) {
        val tv = TextView(this).apply {
            this.text = text
            textSize = 20f
            setTextColor(pale)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 20, 20, 20)
        }
        content.addView(tv, LinearLayout.LayoutParams(-1, -1))
    }

    private fun fetchText(address: String): String {
        val conn = (URL(address).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "KongLongYingshi/1.0 AndroidTV")
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            if (code !in 200..299) throw IllegalStateException("HTTP $code")
            return text
        } finally {
            conn.disconnect()
        }
    }

    private fun parseM3u(text: String): List<Channel> {
        val result = mutableListOf<Channel>()
        var name = "未知频道"
        var group = "未分类"
        text.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("#EXTINF", true)) {
                name = line.substringAfter(",", "").trim().ifBlank { "未知频道" }
                group = Regex("""group-title=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                    .find(line)?.groupValues?.getOrNull(1) ?: "未分类"
            } else if (line.isNotBlank() && !line.startsWith("#") &&
                (line.startsWith("http://", true) || line.startsWith("https://", true))) {
                result.add(Channel(name, line, group))
                name = "未知频道"
                group = "未分类"
            }
        }
        return result
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && player != null) {
            player?.release()
            player = null
            showHome()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        player?.release()
        worker.shutdownNow()
        super.onDestroy()
    }
}
