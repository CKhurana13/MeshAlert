package com.example.blemeshapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class LoginActivity : AppCompatActivity() {

    // ── Design tokens (mirror MainActivity exactly) ───────────────────────────
    private val C_BG           = Color.parseColor("#F8F9FB")
    private val C_SURFACE      = Color.WHITE
    private val C_SURFACE_2    = Color.parseColor("#F1F5F9")
    private val C_BORDER       = Color.parseColor("#E2E8F0")
    private val C_HEADER_BG    = Color.parseColor("#1A2744")
    private val C_ACCENT       = Color.parseColor("#1D4ED8")
    private val C_DANGER       = Color.parseColor("#DC2626")
    private val C_TEXT_PRIMARY = Color.parseColor("#0F172A")
    private val C_TEXT_SECOND  = Color.parseColor("#475569")
    private val C_TEXT_MUTED   = Color.parseColor("#94A3B8")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
    }

    private fun buildUI() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(C_BG)
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation  = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        root.addView(buildHeader())
        root.addView(buildHero())
        root.addView(buildTiles())
        root.addView(buildLoginCard())
        root.addView(buildFooter())

        scroll.addView(root)
        setContentView(scroll)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HEADER — identical structure to MainActivity
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setBackgroundColor(C_HEADER_BG)
            setPadding(dp(18), dp(44), dp(18), dp(16))

            val logoBlock = LinearLayout(this@LoginActivity).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }
            logoBlock.addView(TextView(this@LoginActivity).apply {
                text          = "MESH RELAY"
                textSize      = 18f
                typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                setTextColor(Color.WHITE)
                letterSpacing = 0.12f
            })
            logoBlock.addView(TextView(this@LoginActivity).apply {
                text          = "Disaster Communication Network"
                textSize      = 10f
                setTextColor(Color.parseColor("#93C5FD"))
                letterSpacing = 0.03f
            })

            addView(logoBlock)

            addView(TextView(this@LoginActivity).apply {
                text     = "v1.0"
                textSize = 10f
                typeface = Typeface.create("monospace", Typeface.NORMAL)
                setTextColor(Color.parseColor("#64748B"))
                setPadding(dp(8), dp(4), dp(8), dp(4))
                background = GradientDrawable().apply {
                    cornerRadius = dp(4).toFloat()
                    setColor(Color.parseColor("#0F172A"))
                }
            })
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HERO PANEL
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildHero(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity     = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(C_HEADER_BG)
            setPadding(dp(24), dp(28), dp(24), dp(36))

            addView(TextView(this@LoginActivity).apply {
                text     = "◉"
                textSize = 48f
                setTextColor(C_ACCENT)
                gravity  = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
            })

            addView(View(this@LoginActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(2)).apply {
                    gravity      = Gravity.CENTER_HORIZONTAL
                    bottomMargin = dp(14)
                }
                background = GradientDrawable().apply {
                    cornerRadius = dp(2).toFloat()
                    setColor(C_ACCENT)
                }
            })

            addView(TextView(this@LoginActivity).apply {
                text          = "PEER-TO-PEER BLE MESH"
                textSize      = 13f
                typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity       = Gravity.CENTER
                letterSpacing = 0.14f
                layoutParams  = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            })

            addView(TextView(this@LoginActivity).apply {
                text     = "Works without internet or cellular.\nConnects directly to nearby devices over Bluetooth."
                textSize = 12f
                setTextColor(Color.parseColor("#93C5FD"))
                gravity  = Gravity.CENTER
            })
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CAPABILITY TILES — mirrors quick-alert card style from MainActivity
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildTiles(): LinearLayout {
        data class Tile(val label: String, val sub: String, val color: Int)
        val tiles = listOf(
            Tile("BLE Mesh",  "No internet needed", Color.parseColor("#1D4ED8")),
            Tile("GPS Relay", "Share coordinates",   Color.parseColor("#059669")),
            Tile("SOS Burst", "Multi-hop delivery",  Color.parseColor("#DC2626"))
        )

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(16), dp(12), dp(4))

            tiles.forEach { tile ->
                val card = CardView(this@LoginActivity).apply {
                    radius        = dp(10).toFloat()
                    cardElevation = dp(2).toFloat()
                    setCardBackgroundColor(C_SURFACE)
                    layoutParams  = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    ).apply { setMargins(dp(4), 0, dp(4), 0) }
                }

                val inner = LinearLayout(this@LoginActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity     = Gravity.CENTER_HORIZONTAL
                    setPadding(dp(8), dp(14), dp(8), dp(14))
                }

                inner.addView(View(this@LoginActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(24), dp(3)).apply {
                        bottomMargin = dp(8)
                    }
                    background = GradientDrawable().apply {
                        cornerRadius = dp(2).toFloat()
                        setColor(tile.color)
                    }
                })

                inner.addView(TextView(this@LoginActivity).apply {
                    text          = tile.label.uppercase()
                    textSize      = 10f
                    typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                    setTextColor(C_TEXT_PRIMARY)
                    gravity       = Gravity.CENTER
                    letterSpacing = 0.05f
                })

                inner.addView(TextView(this@LoginActivity).apply {
                    text     = tile.sub
                    textSize = 9f
                    setTextColor(C_TEXT_MUTED)
                    gravity  = Gravity.CENTER
                    setPadding(0, dp(2), 0, 0)
                })

                card.addView(inner)
                addView(card)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN CARD
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildLoginCard(): LinearLayout {
        lateinit var nameInput: EditText
        lateinit var errorView: TextView

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }

        val card = CardView(this).apply {
            radius        = dp(14).toFloat()
            cardElevation = dp(3).toFloat()
            setCardBackgroundColor(C_SURFACE)
        }

        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(22))
        }

        inner.addView(buildSectionHeader("Identify Yourself"))

        inner.addView(TextView(this).apply {
            text     = "Your name identifies your transmissions across the mesh. Keep it short and unique."
            textSize = 11f
            setTextColor(C_TEXT_MUTED)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(18) }
        })

        inner.addView(TextView(this).apply {
            text          = "DISPLAY NAME"
            textSize      = 9f
            typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            setTextColor(C_TEXT_SECOND)
            letterSpacing = 0.10f
            layoutParams  = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(6) }
        })

        nameInput = EditText(this).apply {
            hint            = "e.g.  Arjun  ·  Rescue_2  ·  Base_Alpha"
            setHintTextColor(C_TEXT_MUTED)
            setTextColor(C_TEXT_PRIMARY)
            textSize        = 15f
            typeface        = Typeface.create("sans-serif", Typeface.NORMAL)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            filters         = arrayOf(InputFilter.LengthFilter(20))
            background      = normalInputBg()
            layoutParams    = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(6) }
        }
        inner.addView(nameInput)

        errorView = TextView(this).apply {
            text     = ""
            textSize = 11f
            setTextColor(C_DANGER)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16) }
        }
        inner.addView(errorView)

        val joinBtn = Button(this).apply {
            text          = "JOIN MESH NETWORK"
            textSize      = 13f
            typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            letterSpacing = 0.10f
            setTextColor(Color.WHITE)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background    = accentBtnBg()
            layoutParams  = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        inner.addView(joinBtn)

        card.addView(inner)
        wrapper.addView(card)

        // ── Button logic — unchanged from original ────────────────────────────
        joinBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()

            if (name.isEmpty()) {
                errorView.text    = "Please enter your name before joining."
                nameInput.background = errorInputBg()
                return@setOnClickListener
            }

            errorView.text       = ""
            nameInput.background = normalInputBg()
            joinBtn.isEnabled    = false
            joinBtn.text         = "JOINING…"
            joinBtn.background   = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(Color.parseColor("#94A3B8"))
            }

            joinBtn.postDelayed({
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("NAME", name)
                startActivity(intent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }, 500)
        }

        // Auto-focus — same as original
        nameInput.requestFocus()
        window.decorView.post {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT)
        }

        return wrapper
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildFooter(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity     = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(12), dp(24), dp(32))

            addView(View(this@LoginActivity).apply {
                setBackgroundColor(C_BORDER)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
                ).apply { bottomMargin = dp(14) }
            })

            addView(TextView(this@LoginActivity).apply {
                text     = "Operates entirely offline via Bluetooth Low Energy.\nNo data leaves your device. No servers. No accounts."
                textSize = 10f
                setTextColor(C_TEXT_MUTED)
                gravity  = Gravity.CENTER
            })
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS — same pattern as MainActivity
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildSectionHeader(title: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation  = LinearLayout.HORIZONTAL
            gravity      = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }

            addView(View(this@LoginActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(3), dp(16)).apply {
                    setMargins(0, 0, dp(10), 0)
                }
                background = GradientDrawable().apply {
                    cornerRadius = dp(2).toFloat()
                    setColor(C_ACCENT)
                }
            })

            addView(TextView(this@LoginActivity).apply {
                text          = title.uppercase()
                textSize      = 10f
                typeface      = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                setTextColor(C_TEXT_SECOND)
                letterSpacing = 0.12f
            })
        }
    }

    private fun normalInputBg() = GradientDrawable().apply {
        shape        = GradientDrawable.RECTANGLE
        cornerRadius = dp(8).toFloat()
        setColor(C_SURFACE_2)
        setStroke(dp(1), C_BORDER)
    }

    private fun errorInputBg() = GradientDrawable().apply {
        shape        = GradientDrawable.RECTANGLE
        cornerRadius = dp(8).toFloat()
        setColor(C_SURFACE_2)
        setStroke(dp(2), C_DANGER)
    }

    private fun accentBtnBg() = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.parseColor("#1E3A8A"), Color.parseColor("#1D4ED8"))
    ).apply { cornerRadius = dp(10).toFloat() }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
        ).toInt()
}