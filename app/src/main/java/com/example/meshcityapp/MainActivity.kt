package com.example.blemeshapp

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // ── BLE ──────────────────────────────────────────────────────────────────
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var advertiser: BluetoothLeAdvertiser
    private lateinit var scanner: BluetoothLeScanner

    private val MANUFACTURER_ID = 0xFFFF
    private val HEADER_SIZE     = 6
    private val CHUNK_DATA_SIZE = 20

    private val fragmentBuffer = HashMap<String, SortedMap<Int, ByteArray>>()
    private val fragmentTotal  = HashMap<String, Int>()
    private val receivedMsgIds = HashSet<String>()
    private val relayedMsgIds  = HashSet<String>()

    private data class OutgoingChunk(val data: ByteArray, val delayMs: Long)
    private val sendQueue   = ArrayDeque<OutgoingChunk>()
    private var isSending   = false
    private val sendHandler = Handler(Looper.getMainLooper())

    private var currentAdvertiseCallback: AdvertiseCallback? = null
    private var isAdvertising = false
    private var scanCallback: ScanCallback? = null

    // ── Location ─────────────────────────────────────────────────────────────
    private var locationManager: LocationManager? = null
    private var currentLocation: Location? = null

    // ── Node tracking ─────────────────────────────────────────────────────────
    data class NodeInfo(
        val address: String,
        var lastSeen: Long = System.currentTimeMillis(),
        var lat: Double?   = null,
        var lon: Double?   = null,
        var name: String   = ""
    )
    internal val nodeMap = LinkedHashMap<String, NodeInfo>()

    // ── UI ───────────────────────────────────────────────────────────────────
    private lateinit var outerScroll: NestedScrollView
    private lateinit var logScroll: ScrollView
    private lateinit var logLayout: LinearLayout
    private lateinit var statusBleView: TextView
    private lateinit var statusGpsView: TextView
    private lateinit var input: EditText
    private lateinit var leafletMap: WebView

    private var userName = "User"

    // ── Design tokens ────────────────────────────────────────────────────────
    private val C_BG           = Color.parseColor("#F8F9FB")
    private val C_SURFACE_2    = Color.parseColor("#F1F5F9")
    private val C_BORDER       = Color.parseColor("#E2E8F0")
    private val C_HEADER_BG    = Color.parseColor("#1A2744")
    private val C_ACCENT       = Color.parseColor("#1D4ED8")
    private val C_DANGER       = Color.parseColor("#DC2626")
    private val C_SUCCESS      = Color.parseColor("#059669")
    private val C_WARN         = Color.parseColor("#D97706")

    private val C_TEXT_PRIMARY  = Color.parseColor("#0F172A")
    private val C_TEXT_SECOND   = Color.parseColor("#475569")
    private val C_TEXT_MUTED    = Color.parseColor("#94A3B8")

    companion object {
        private const val TAG          = "BLEMesh"
        private const val PERM_REQUEST = 1001
        const val TYPE_TEXT     : Byte = 0x01
        const val TYPE_SOS      : Byte = 0x02
        const val TYPE_LOCATION : Byte = 0x03
        const val TYPE_RESOURCE : Byte = 0x04
        const val TYPE_RELAY    : Byte = 0x05

        private const val CHUNK_TX_DURATION_MS = 400L
        private const val CHUNK_GAP_MS         = 200L
        private const val RELAY_MIN_MS         = 200L
        private const val RELAY_MAX_MS         = 600L
        private const val DEFAULT_ZOOM         = 13
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═════════════════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userName = intent.getStringExtra("NAME") ?: "User"
        buildUI()
        setupBluetooth()
        requestAllPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAllBLE()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UI
    // ═════════════════════════════════════════════════════════════════════════

    private fun buildUI() {
        outerScroll = NestedScrollView(this).apply {
            setBackgroundColor(C_BG)
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(buildHeader())
        root.addView(buildStatusStrip())
        root.addView(buildDivider())
        root.addView(buildSosSection())
        root.addView(buildSectionHeader("Quick Alerts"))
        root.addView(buildQuickSection())
        root.addView(buildSectionHeader("Send Message"))
        root.addView(buildMessageSection())
        root.addView(buildSectionHeader("Mesh Activity Log"))
        root.addView(buildLogSection())
        root.addView(buildSectionHeader("Live Node Map"))
        root.addView(buildMapSection())
        root.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(24))
        })

        outerScroll.addView(root)
        setContentView(outerScroll)
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private fun buildHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setBackgroundColor(C_HEADER_BG)
            setPadding(dp(18), dp(44), dp(18), dp(16))

            val logoBlock = LinearLayout(this@MainActivity).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            logoBlock.addView(TextView(this@MainActivity).apply {
                text          = "MESH RELAY"
                textSize      = 18f
                typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                setTextColor(Color.WHITE)
                letterSpacing = 0.12f
            })
            logoBlock.addView(TextView(this@MainActivity).apply {
                text     = "Disaster Communication Network"
                textSize = 10f
                setTextColor(Color.parseColor("#93C5FD"))
            })

            val dot = View(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply { setMargins(0, 0, dp(6), 0) }
                background   = GradientDrawable().apply {
                    shape    = GradientDrawable.OVAL
                    setColor(Color.parseColor("#4ADE80"))
                }
            }

            val userLabel = TextView(this@MainActivity).apply {
                text          = userName.take(10).uppercase()
                textSize      = 11f
                typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                setTextColor(Color.parseColor("#BAE6FD"))
                letterSpacing = 0.06f
            }

            addView(logoBlock)
            addView(dot)
            addView(userLabel)
        }
    }

    // ── Status strip ──────────────────────────────────────────────────────────

    private fun buildStatusStrip(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#EFF6FF"))
            setPadding(dp(16), dp(10), dp(16), dp(10))
            gravity     = Gravity.CENTER_VERTICAL

            statusBleView = TextView(this@MainActivity).apply {
                text         = "BLE  ——"
                textSize     = 11f
                typeface     = Typeface.create("monospace", Typeface.NORMAL)
                setTextColor(C_TEXT_SECOND)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            statusGpsView = TextView(this@MainActivity).apply {
                text         = "GPS  ——"
                textSize     = 11f
                typeface     = Typeface.create("monospace", Typeface.NORMAL)
                setTextColor(C_TEXT_SECOND)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            addView(statusBleView)
            addView(statusGpsView)
        }
    }

    // ── SOS section ───────────────────────────────────────────────────────────

    private fun buildSosSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(8))

            val btn = Button(this@MainActivity).apply {
                text          = "⚠  SOS + LOCATION"
                textSize      = 15f
                typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                letterSpacing = 0.08f
                setTextColor(Color.WHITE)
                setPadding(dp(20), dp(16), dp(20), dp(16))
                background    = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(Color.parseColor("#B91C1C"), Color.parseColor("#DC2626"))
                ).apply { cornerRadius = dp(10).toFloat() }
                layoutParams  = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                elevation     = dp(4).toFloat()
                setOnClickListener { sendSOS() }
            }

            val sub = TextView(this@MainActivity).apply {
                text     = "Broadcasts your GPS coordinates to all nearby devices instantly"
                textSize = 10f
                setTextColor(C_TEXT_MUTED)
                gravity  = Gravity.CENTER
                setPadding(0, dp(6), 0, 0)
            }

            addView(btn)
            addView(sub)
        }
    }

    // ── Quick alerts ──────────────────────────────────────────────────────────

    private fun buildQuickSection(): LinearLayout {
        data class AlertBtn(val label: String, val sub: String, val color: Int, val action: () -> Unit)

        val buttons = listOf(
            AlertBtn("Water",   "Need water",       Color.parseColor("#0369A1")) { sendResource("NEED WATER") },
            AlertBtn("Food",    "Need food",         Color.parseColor("#92400E")) { sendResource("NEED FOOD") },
            AlertBtn("Medical", "Need medical help", Color.parseColor("#9D174D")) { sendResource("NEED MEDICAL") },
            AlertBtn("Shelter", "Need shelter",      Color.parseColor("#4C1D95")) { sendResource("NEED SHELTER") },
            AlertBtn("Safe",    "I am safe",         Color.parseColor("#065F46")) { sendStatusUpdate("I AM SAFE") },
            AlertBtn("GPS",     "Share location",    Color.parseColor("#1E3A5F")) { sendLocationUpdate() },
            AlertBtn("Flood",   "Flood/landslide",   Color.parseColor("#7C2D12")) { sendResource("FLOOD LANDSLIDE DANGER") },
            AlertBtn("Fire",    "Fire danger",       Color.parseColor("#7F1D1D")) { sendResource("FIRE DANGER AREA") }
        )

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(4), dp(12), dp(4))

            buttons.chunked(2).forEach { pair ->
                val row = LinearLayout(this@MainActivity).apply {
                    orientation  = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(8) }
                }

                pair.forEach { btn ->
                    val card = CardView(this@MainActivity).apply {
                        radius             = dp(10).toFloat()
                        cardElevation      = dp(2).toFloat()
                        setCardBackgroundColor(Color.WHITE)
                        layoutParams       = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                        ).apply { setMargins(dp(4), 0, dp(4), 0) }
                    }

                    val inner = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(12), dp(12), dp(12), dp(12))
                        setOnClickListener { btn.action() }
                    }

                    inner.addView(View(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dp(28), dp(3)).apply { bottomMargin = dp(8) }
                        background   = GradientDrawable().apply {
                            cornerRadius = dp(2).toFloat(); setColor(btn.color)
                        }
                    })
                    inner.addView(TextView(this@MainActivity).apply {
                        text          = btn.label.uppercase()
                        textSize      = 12f
                        typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                        setTextColor(C_TEXT_PRIMARY)
                        letterSpacing = 0.05f
                    })
                    inner.addView(TextView(this@MainActivity).apply {
                        text     = btn.sub
                        textSize = 10f
                        setTextColor(C_TEXT_MUTED)
                        setPadding(0, dp(2), 0, 0)
                    })

                    card.addView(inner)
                    row.addView(card)
                }

                if (pair.size == 1) {
                    row.addView(View(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                    })
                }
                addView(row)
            }
        }
    }

    // ── Message section ───────────────────────────────────────────────────────

    private fun buildMessageSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(4), dp(16), dp(12))

            val card  = CardView(this@MainActivity).apply {
                radius             = dp(12).toFloat()
                cardElevation      = dp(2).toFloat()
                setCardBackgroundColor(Color.WHITE)
            }
            val inner = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(14), dp(14), dp(14))
            }

            input = EditText(this@MainActivity).apply {
                hint           = "Type your message (max 160 characters)"
                setHintTextColor(C_TEXT_MUTED)
                setTextColor(C_TEXT_PRIMARY)
                textSize       = 14f
                setPadding(dp(12), dp(12), dp(12), dp(12))
                minLines       = 2
                maxLines       = 4
                background     = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat(); setColor(C_SURFACE_2)
                    setStroke(dp(1), C_BORDER)
                }
                layoutParams   = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(10) }
            }

            val sendBtn = Button(this@MainActivity).apply {
                text          = "TRANSMIT MESSAGE"
                textSize      = 12f
                typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                letterSpacing = 0.08f
                setTextColor(Color.WHITE)
                setPadding(dp(16), dp(12), dp(16), dp(12))
                background    = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat(); setColor(C_ACCENT)
                }
                layoutParams  = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setOnClickListener { sendCustomMessage() }
            }

            inner.addView(input)
            inner.addView(sendBtn)
            card.addView(inner)
            addView(card)
        }
    }

    // ── Log section ───────────────────────────────────────────────────────────

    private fun buildLogSection(): LinearLayout {
        return LinearLayout(this).apply {
            setPadding(dp(16), dp(4), dp(16), dp(8))

            val card = CardView(this@MainActivity).apply {
                radius             = dp(12).toFloat()
                cardElevation      = dp(2).toFloat()
                setCardBackgroundColor(Color.WHITE)
                layoutParams       = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            logScroll = ScrollView(this@MainActivity).apply {
                layoutParams               = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(300))
                isFillViewport             = false
                isVerticalScrollBarEnabled = true
            }

            logLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(10), dp(14), dp(10))
            }

            logLayout.addView(TextView(this@MainActivity).apply {
                text     = "Awaiting transmissions…"
                textSize = 12f
                typeface = Typeface.create("monospace", Typeface.NORMAL)
                setTextColor(C_TEXT_MUTED)
                gravity  = Gravity.CENTER
                setPadding(0, dp(24), 0, dp(24))
            })

            logScroll.addView(logLayout)
            card.addView(logScroll)
            addView(card)
        }
    }

    // ── Map section — Leaflet in WebView ──────────────────────────────────────

    // Queue for JS calls that arrive before Leaflet finishes loading
    private val mapJsQueue = ArrayDeque<String>()
    private var mapReady   = false

    private fun buildMapSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(4), dp(16), dp(8))

            addView(TextView(this@MainActivity).apply {
                text     = "Pinch to zoom · Tap markers for node details"
                textSize = 10f
                setTextColor(C_TEXT_MUTED)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            })

            val card = CardView(this@MainActivity).apply {
                radius        = dp(12).toFloat()
                cardElevation = dp(3).toFloat()
                setCardBackgroundColor(Color.WHITE)
                layoutParams  = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            leafletMap = WebView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(420))
                settings.apply {
                    javaScriptEnabled    = true
                    domStorageEnabled    = true
                    loadWithOverviewMode = true
                    useWideViewPort      = true
                    // Always load from network so Leaflet JS is fetched fresh
                    cacheMode            = WebSettings.LOAD_DEFAULT
                }
                // Override onPageFinished so we know when Leaflet is ready
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        runOnUiThread {
                            mapReady = true
                            // Drain any calls that arrived before the page loaded
                            while (mapJsQueue.isNotEmpty()) {
                                leafletMap.evaluateJavascript(mapJsQueue.removeFirst(), null)
                            }
                            // If we already have a self-location, pin it now
                            currentLocation?.let { loc ->
                                val self = nodeMap.getOrPut("SELF") { NodeInfo("SELF") }
                                self.lat  = loc.latitude
                                self.lon  = loc.longitude
                                self.name = userName.take(8)
                                pushMarkerToMap(self)
                            }
                        }
                    }
                }
                setOnTouchListener { v, _ ->
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    false
                }
                loadDataWithBaseURL(
                    "https://unpkg.com",
                    buildLeafletHtml(20.5937, 78.9629, DEFAULT_ZOOM),
                    "text/html",
                    "UTF-8",
                    null
                )
            }

            card.addView(leafletMap)
            addView(card)
        }
    }

    /**
     * Generates a fully self-contained Leaflet HTML page.
     * The page exposes addOrUpdateMarker() globally and signals readiness
     * via the mapReady flag — we use onPageFinished instead of a JS bridge
     * to keep things simple and permission-free.
     */
    private fun buildLeafletHtml(lat: Double, lon: Double, zoom: Int): String = """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<style>
  *{box-sizing:border-box;margin:0;padding:0}
  html,body,#map{width:100%;height:100%;background:#ddd}
</style>
</head>
<body>
<div id="map"></div>
<script>
  var map = L.map('map', {zoomControl:true, attributionControl:true})
             .setView([$lat, $lon], $zoom);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 20,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(map);

  // addr -> L.Marker
  var markers = {};
  var firstCenter = true;

  function makeDotIcon(label, isSelf) {
    var bg    = isSelf ? '#1D4ED8' : '#DC2626';
    var ring  = isSelf ? '#93C5FD' : '#FCA5A5';
    var short = (label || '?').substring(0, 4).toUpperCase();
    var html  = '<div style="'
      + 'width:42px;height:42px;border-radius:50%;'
      + 'background:' + bg + ';'
      + 'border:3px solid ' + ring + ';'
      + 'box-shadow:0 3px 10px rgba(0,0,0,0.45);'
      + 'display:flex;align-items:center;justify-content:center;'
      + 'color:#fff;font-weight:700;font-size:9px;'
      + 'font-family:sans-serif;letter-spacing:0.05em;'
      + 'position:relative;'
      + '">' + short
      + '<div style="'
      + 'position:absolute;bottom:-7px;left:50%;transform:translateX(-50%);'
      + 'width:0;height:0;'
      + 'border-left:6px solid transparent;'
      + 'border-right:6px solid transparent;'
      + 'border-top:8px solid ' + bg + ';'
      + '"></div>'
      + '</div>';
    return L.divIcon({
      className : '',
      html      : html,
      iconSize  : [42, 50],
      iconAnchor: [21, 50],
      popupAnchor:[0, -52]
    });
  }

  function addOrUpdateMarker(addr, lat, lon, label, popupHtml, isSelf) {
    var icon = makeDotIcon(label, isSelf);
    if (markers[addr]) {
      markers[addr].setLatLng([lat, lon]);
      markers[addr].setIcon(icon);
      markers[addr].getPopup().setContent(popupHtml);
    } else {
      markers[addr] = L.marker([lat, lon], {icon: icon})
                       .bindPopup(popupHtml, {maxWidth: 220})
                       .addTo(map);
    }
    if (firstCenter) {
      firstCenter = false;
      map.setView([lat, lon], $zoom);
    }
  }
</script>
</body>
</html>
    """.trimIndent()

    /**
     * Thread-safe: queues the JS if the map isn't ready yet, otherwise
     * evaluates it immediately. Must be called from any thread.
     */
    private fun updateMapMarker(node: NodeInfo) {
        val lat    = node.lat ?: return
        val lon    = node.lon ?: return
        val isSelf = node.address == "SELF"

        val label  = if (isSelf) "YOU"
        else node.name.take(4).ifEmpty { node.address.takeLast(4) }

        val title  = if (isSelf) "YOU — ${node.name}"
        else node.name.ifEmpty { node.address.takeLast(8) }

        val ts     = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(node.lastSeen))
        val coordStr = "${"%.5f".format(lat)}, ${"%.5f".format(lon)}"

        val popupBody = if (isSelf) {
            "<b style='color:#1D4ED8'>&#x25CF; YOU</b><br/>" +
                    "<span style='font-size:12px'>$title</span><br/>" +
                    "<span style='color:#64748b;font-size:11px'>$coordStr</span>"
        } else {
            "<b style='color:#DC2626'>&#x25CF; PEER NODE</b><br/>" +
                    "<span style='font-size:12px'>$title</span><br/>" +
                    "<span style='color:#64748b;font-size:11px'>$coordStr</span><br/>" +
                    "<span style='color:#94a3b8;font-size:10px'>Last seen $ts</span>"
        }

        // Escape single quotes so the JS string literals stay valid
        fun String.escJs() = replace("\\", "\\\\").replace("'", "\\'")

        val js = "addOrUpdateMarker('${node.address.escJs()}',$lat,$lon," +
                "'${label.escJs()}','${popupBody.escJs()}',${isSelf});"

        runOnUiThread {
            if (mapReady) {
                leafletMap.evaluateJavascript(js, null)
            } else {
                mapJsQueue.addLast(js)
            }
        }
    }

    /** Direct push — only call when you are already sure mapReady == true. */
    private fun pushMarkerToMap(node: NodeInfo) {
        // Re-uses the same logic, mapReady flag will route correctly
        updateMapMarker(node)
    }

    // ── Shared layout helpers ─────────────────────────────────────────────────

    private fun buildSectionHeader(title: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(8))

            addView(View(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(3), dp(14)).apply { setMargins(0, 0, dp(10), 0) }
                background   = GradientDrawable().apply {
                    cornerRadius = dp(2).toFloat(); setColor(C_ACCENT)
                }
            })
            addView(TextView(this@MainActivity).apply {
                text          = title.uppercase()
                textSize      = 10f
                typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                setTextColor(C_TEXT_SECOND)
                letterSpacing = 0.12f
            })
        }
    }

    private fun buildDivider() = View(this).apply {
        setBackgroundColor(C_BORDER)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
    }

    // ── Log entries ────────────────────────────────────────────────────────────

    private fun logMessage(text: String, type: Byte = TYPE_TEXT) {
        if (type == TYPE_RELAY) return

        runOnUiThread {
            val (accentColor, tagLabel) = when (type) {
                TYPE_SOS      -> Pair(C_DANGER,  "SOS")
                TYPE_LOCATION -> Pair(C_SUCCESS, "LOC")
                TYPE_RESOURCE -> Pair(C_WARN,    "RES")
                else          -> Pair(C_ACCENT,  "MSG")
            }

            if (logLayout.childCount == 1 &&
                (logLayout.getChildAt(0) as? TextView)?.text?.startsWith("Awaiting") == true)
                logLayout.removeAllViews()

            logLayout.addView(View(this).apply {
                setBackgroundColor(C_BORDER)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                ).apply { setMargins(0, dp(4), 0, dp(4)) }
            })

            val row = LinearLayout(this).apply {
                orientation  = LinearLayout.HORIZONTAL
                gravity      = Gravity.TOP
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            row.addView(TextView(this).apply {
                this.text    = tagLabel
                textSize     = 9f
                typeface     = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity      = Gravity.CENTER
                setPadding(dp(6), dp(3), dp(6), dp(3))
                background   = GradientDrawable().apply {
                    cornerRadius = dp(4).toFloat(); setColor(accentColor)
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, dp(2), dp(8), 0) }
            })

            row.addView(TextView(this).apply {
                this.text = text
                textSize  = 12f
                typeface  = Typeface.create("monospace", Typeface.NORMAL)
                setTextColor(C_TEXT_PRIMARY)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            logLayout.addView(row)
            logScroll.post { logScroll.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

    private fun updateStatus() {
        val bleOn = bluetoothAdapter.isEnabled
        val gpsOn = currentLocation != null
        runOnUiThread {
            statusBleView.text = if (bleOn) "BLE  ACTIVE" else "BLE  OFF"
            statusBleView.setTextColor(if (bleOn) C_SUCCESS else C_DANGER)
            statusGpsView.text = if (gpsOn) "GPS  LOCKED" else "GPS  SEARCHING"
            statusGpsView.setTextColor(if (gpsOn) C_SUCCESS else C_TEXT_MUTED)
        }
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    // ═════════════════════════════════════════════════════════════════════════
    // BLUETOOTH
    // ═════════════════════════════════════════════════════════════════════════

    private fun setupBluetooth() {
        val mgr      = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = mgr.adapter
        advertiser       = bluetoothAdapter.bluetoothLeAdvertiser
        scanner          = bluetoothAdapter.bluetoothLeScanner
        updateStatus()
    }

    private fun stopAllBLE() {
        stopCurrentAdvertising()
        sendHandler.removeCallbacksAndMessages(null)
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED)
                scanCallback?.let { scanner.stopScan(it) }
        } catch (_: Exception) {}
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FRAGMENTED SEND ENGINE
    // ═════════════════════════════════════════════════════════════════════════

    private fun transmitMessage(message: String, type: Byte) {
        val msgBytes = message.toByteArray(Charsets.UTF_8)
        val chunks   = msgBytes.toList().chunked(CHUNK_DATA_SIZE)
        val rawId    = (message + System.currentTimeMillis()).hashCode()
        val idBytes  = ByteArray(3) { i -> ((rawId shr (i * 8)) and 0xFF).toByte() }

        chunks.forEachIndexed { idx, chunk ->
            val payload = ByteArray(HEADER_SIZE + chunk.size)
            payload[0]  = type
            payload[1]  = idBytes[0]; payload[2] = idBytes[1]; payload[3] = idBytes[2]
            payload[4]  = idx.toByte()
            payload[5]  = chunks.size.toByte()
            chunk.forEachIndexed { i, b -> payload[HEADER_SIZE + i] = b }
            sendQueue.addLast(OutgoingChunk(payload, if (idx == 0) 0L else CHUNK_GAP_MS))
        }
        if (!isSending) drainQueue()
    }

    private fun drainQueue() {
        if (sendQueue.isEmpty()) { isSending = false; return }
        isSending   = true
        val chunk   = sendQueue.removeFirst()
        sendHandler.postDelayed({ broadcastChunk(chunk.data) }, chunk.delayMs)
    }

    private fun broadcastChunk(payload: ByteArray) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)
            != PackageManager.PERMISSION_GRANTED) { isSending = false; return }

        stopCurrentAdvertising()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0)
            .build()

        val adData = AdvertiseData.Builder()
            .addManufacturerData(MANUFACTURER_ID, payload)
            .setIncludeTxPowerLevel(false)
            .setIncludeDeviceName(false)
            .build()

        currentAdvertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(s: AdvertiseSettings) {
                isAdvertising = true
                updateStatus()
                sendHandler.postDelayed({
                    stopCurrentAdvertising()
                    drainQueue()
                }, CHUNK_TX_DURATION_MS)
            }
            override fun onStartFailure(err: Int) {
                isAdvertising = false
                Log.e(TAG, "Advertise failed: $err")
                updateStatus()
                sendHandler.postDelayed({ drainQueue() }, 300)
            }
        }

        try {
            advertiser.startAdvertising(settings, adData, currentAdvertiseCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "startAdvertising: ${e.message}")
            isSending = false
        }
    }

    private fun stopCurrentAdvertising() {
        if (!isAdvertising) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)
            == PackageManager.PERMISSION_GRANTED) {
            try { currentAdvertiseCallback?.let { advertiser.stopAdvertising(it) } }
            catch (_: Exception) {}
        }
        isAdvertising            = false
        currentAdvertiseCallback = null
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SCANNING & REASSEMBLY
    // ═════════════════════════════════════════════════════════════════════════

    private fun startScanning() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED) {
            logMessage("[ERR] SCAN permission missing"); return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val raw = result.scanRecord
                    ?.getManufacturerSpecificData(MANUFACTURER_ID) ?: return
                if (raw.size < HEADER_SIZE) return

                val addr = result.device.address
                val node = nodeMap.getOrPut(addr) { NodeInfo(addr) }
                node.lastSeen = System.currentTimeMillis()

                val type        = raw[0]
                val idBytes     = raw.copyOfRange(1, 4)
                val msgId       = idBytes.toHexString()
                val chunkIdx    = raw[4].toInt() and 0xFF
                val totalChunks = raw[5].toInt() and 0xFF
                val chunkData   = raw.copyOfRange(HEADER_SIZE, raw.size)

                if (receivedMsgIds.contains(msgId)) return

                val bucket = fragmentBuffer.getOrPut(msgId) { TreeMap() }
                bucket[chunkIdx] = chunkData
                fragmentTotal[msgId] = totalChunks

                if (bucket.size == totalChunks) {
                    val fullBytes = ByteArray(bucket.values.sumOf { it.size })
                    var offset    = 0
                    bucket.values.forEach { b -> b.copyInto(fullBytes, offset); offset += b.size }
                    val message = String(fullBytes, Charsets.UTF_8)

                    receivedMsgIds.add(msgId)
                    fragmentBuffer.remove(msgId)
                    fragmentTotal.remove(msgId)

                    if (type == TYPE_LOCATION || type == TYPE_SOS) {
                        parseLocationFromMessage(message, addr)
                    }

                    runOnUiThread { handleIncoming(message, type, msgId, addr) }
                }
            }

            override fun onScanFailed(err: Int) {
                logMessage("[ERR] Scan error: $err")
            }
        }

        try {
            scanner.startScan(null, settings, scanCallback)
            logMessage("[SYS] Mesh active — scanning for peers")
        } catch (e: Exception) {
            logMessage("[ERR] Scan failed: ${e.message}")
        }
    }

    // ── Incoming message handler ───────────────────────────────────────────────

    private fun handleIncoming(message: String, type: Byte, msgId: String, addr: String) {
        if (type == TYPE_RELAY) {
            if (!relayedMsgIds.contains(msgId)) {
                relayedMsgIds.add(msgId)
                sendHandler.postDelayed({
                    if (message.length <= 160) transmitMessage(message, TYPE_RELAY)
                }, (RELAY_MIN_MS..RELAY_MAX_MS).random())
            }
            return
        }

        val displayMessage = message.removePrefix("[R] ").removePrefix("[R]").trim()
        if (displayMessage.isEmpty()) return

        val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        logMessage("[$ts] $displayMessage", type)

        if (!relayedMsgIds.contains(msgId) && !displayMessage.startsWith(userName.take(8))) {
            relayedMsgIds.add(msgId)
            sendHandler.postDelayed({
                if (displayMessage.length <= 160) transmitMessage(displayMessage, TYPE_RELAY)
            }, (RELAY_MIN_MS..RELAY_MAX_MS).random())
        }
    }

    // ── Location parsing ──────────────────────────────────────────────────────

    private fun parseLocationFromMessage(message: String, addr: String) {
        val regex = Regex("""(-?\d{1,3}\.\d{3,6}),\s*(-?\d{1,3}\.\d{3,6})""")
        val match = regex.find(message) ?: return
        val lat   = match.groupValues[1].toDoubleOrNull() ?: return
        val lon   = match.groupValues[2].toDoubleOrNull() ?: return

        val node  = nodeMap.getOrPut(addr) { NodeInfo(addr) }
        node.lat  = lat
        node.lon  = lon
        Regex("""(?:SOS|LOC)\s+(\S+)""").find(message)?.let { node.name = it.groupValues[1] }
        updateMapMarker(node)
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    // ═════════════════════════════════════════════════════════════════════════
    // MESSAGE SENDERS
    // ═════════════════════════════════════════════════════════════════════════

    private fun sendSOS() {
        val ts  = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        val loc = currentLocation?.let {
            String.format(Locale.US, "%.4f,%.4f", it.latitude, it.longitude)
        } ?: "NO_GPS"
        val msg = "SOS ${userName.take(8)} @ $loc [$ts]"
        transmitMessage(msg, TYPE_SOS)
        logMessage("[$ts] SOS transmitted — $msg", TYPE_SOS)

        currentLocation?.let {
            val self = nodeMap.getOrPut("SELF") { NodeInfo("SELF") }
            self.lat = it.latitude; self.lon = it.longitude; self.name = userName.take(8)
            updateMapMarker(self)
        }
    }

    private fun sendLocationUpdate() {
        val loc = currentLocation
        if (loc == null) {
            logMessage("[SYS] No GPS fix yet — retrying")
            startLocationUpdates()
            return
        }
        val ts  = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        val msg = "LOC ${userName.take(8)}: ${
            String.format(Locale.US, "%.5f", loc.latitude)
        },${
            String.format(Locale.US, "%.5f", loc.longitude)
        } [${loc.accuracy.toInt()}m][$ts]"
        transmitMessage(msg, TYPE_LOCATION)
        logMessage("[$ts] Location transmitted", TYPE_LOCATION)

        val self = nodeMap.getOrPut("SELF") { NodeInfo("SELF") }
        self.lat = loc.latitude; self.lon = loc.longitude; self.name = userName.take(8)
        updateMapMarker(self)
    }

    private fun sendResource(resource: String) {
        val ts = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        transmitMessage("${userName.take(8)}: $resource [$ts]", TYPE_RESOURCE)
        logMessage("[$ts] $resource", TYPE_RESOURCE)
    }

    private fun sendStatusUpdate(status: String) {
        val ts = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        transmitMessage("${userName.take(8)}: $status [$ts]", TYPE_TEXT)
        logMessage("[$ts] $status", TYPE_TEXT)
    }

    private fun sendCustomMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        if (text.length > 160) {
            logMessage("[ERR] Too long (${text.length}/160)"); return
        }
        val ts = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        transmitMessage("${userName.take(8)}: $text [$ts]", TYPE_TEXT)
        logMessage("[$ts] $text", TYPE_TEXT)
        input.text.clear()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LOCATION
    // ═════════════════════════════════════════════════════════════════════════

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        fun applyFix(loc: Location) {
            currentLocation = loc
            val self = nodeMap.getOrPut("SELF") { NodeInfo("SELF") }
            self.lat      = loc.latitude
            self.lon      = loc.longitude
            self.name     = userName.take(8)
            self.lastSeen = System.currentTimeMillis()
            updateMapMarker(self)
            updateStatus()
        }

        try {
            val lm = locationManager!!
            // Immediately use the best last-known fix so the marker appears before the
            // first onLocationChanged fires (which can take several seconds outdoors).
            listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            ).forEach { provider ->
                if (lm.isProviderEnabled(provider)) {
                    lm.getLastKnownLocation(provider)?.let { applyFix(it) }
                }
            }

            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(loc: Location) { applyFix(loc) }
                @Deprecated("Deprecated")
                override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                override fun onProviderEnabled(p: String) {}
                override fun onProviderDisabled(p: String) {}
            }

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,     3000L, 3f, listener)
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 5f, listener)

        } catch (e: Exception) {
            logMessage("[ERR] GPS: ${e.message}")
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PERMISSIONS
    // ═════════════════════════════════════════════════════════════════════════

    private fun requestAllPermissions() {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) else arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(this, perms, PERM_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_REQUEST) {
            startScanning()
            startLocationUpdates()
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) logMessage("[WARN] Some permissions missing — limited functionality")
            updateStatus()
        }
    }
}