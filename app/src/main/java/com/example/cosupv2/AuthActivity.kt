package com.example.cosupv2

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

/** Sign in, register, forgot password and continue as guest. Works like the website modals. */
class AuthActivity : AppCompatActivity() {

    private val auth: FirebaseAuth? by lazy {
        if (Config.firebaseReady) FirebaseAuth.getInstance() else null
    }

    private lateinit var errorBox: TextView
    private lateinit var progress: ProgressBar
    private lateinit var panelMain: View
    private lateinit var panelForgot: View
    private lateinit var panelGuest: View
    private lateinit var formSignin: View
    private lateinit var formRegister: View
    private lateinit var tabSignin: TextView
    private lateinit var tabRegister: TextView
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(this)
        val dark = ThemeManager.isDark(this)
        val barStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
        else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val root = findViewById<View>(R.id.authRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.authLogo).setDrawableByName("logo")
        findViewById<View>(R.id.authClose).setOnClickListener { finish() }

        errorBox = findViewById(R.id.errorBox)
        progress = findViewById(R.id.authProgress)
        panelMain = findViewById(R.id.panelMain)
        panelForgot = findViewById(R.id.panelForgot)
        panelGuest = findViewById(R.id.panelGuest)
        formSignin = findViewById(R.id.formSignin)
        formRegister = findViewById(R.id.formRegister)
        tabSignin = findViewById(R.id.tabSignin)
        tabRegister = findViewById(R.id.tabRegister)

        tabSignin.setOnClickListener { setTab(true) }
        tabRegister.setOnClickListener { setTab(false) }
        setTab(true)

        findViewById<View>(R.id.btnGoogle).setOnClickListener { social("google.com") }
        findViewById<View>(R.id.btnYahoo).setOnClickListener { social("yahoo.com") }
        findViewById<View>(R.id.btnMicrosoft).setOnClickListener { social("microsoft.com") }

        findViewById<View>(R.id.btnSignin).setOnClickListener { signIn() }
        findViewById<View>(R.id.btnRegister).setOnClickListener { register() }

        findViewById<View>(R.id.tvForgot).setOnClickListener {
            val typed = findViewById<EditText>(R.id.siEmail).text.toString().trim()
            if (typed.isNotEmpty()) findViewById<EditText>(R.id.fpEmail).setText(typed)
            showPanel(panelForgot)
        }
        findViewById<View>(R.id.tvForgotBack).setOnClickListener { showPanel(panelMain) }
        findViewById<View>(R.id.btnSendReset).setOnClickListener { sendReset() }

        findViewById<View>(R.id.btnGuestOpen).setOnClickListener { showPanel(panelGuest) }
        findViewById<View>(R.id.tvGuestBack).setOnClickListener { showPanel(panelMain) }
        findViewById<View>(R.id.btnGuestGo).setOnClickListener { continueAsGuest() }

        setupPasswordChecks()

        if (!Config.firebaseReady) {
            showMessage("Sign-in is not switched on in this build yet.", false)
        }
    }

    // ---------- screen state ----------

    private fun showPanel(panel: View) {
        panelMain.visibility = if (panel === panelMain) View.VISIBLE else View.GONE
        panelForgot.visibility = if (panel === panelForgot) View.VISIBLE else View.GONE
        panelGuest.visibility = if (panel === panelGuest) View.VISIBLE else View.GONE
        errorBox.visibility = View.GONE
    }

    private fun setTab(signin: Boolean) {
        formSignin.visibility = if (signin) View.VISIBLE else View.GONE
        formRegister.visibility = if (signin) View.GONE else View.VISIBLE
        tabSignin.setBackgroundResource(if (signin) R.drawable.bg_tab_active else 0)
        tabRegister.setBackgroundResource(if (signin) 0 else R.drawable.bg_tab_active)
        tabSignin.setTextColor(if (signin) Color.WHITE else mutedColor())
        tabRegister.setTextColor(if (signin) mutedColor() else Color.WHITE)
        errorBox.visibility = View.GONE
    }

    private fun mutedColor(): Int =
        Color.parseColor(if (ThemeManager.isDark(this)) "#93A79B" else "#6B7C72")

    private fun showMessage(text: String, ok: Boolean) {
        errorBox.text = text
        errorBox.setBackgroundResource(if (ok) R.drawable.bg_ok else R.drawable.bg_error)
        errorBox.setTextColor(Color.parseColor(if (ok) "#17642A" else "#9B2F17"))
        errorBox.visibility = View.VISIBLE
    }

    private fun setBusy(on: Boolean) {
        busy = on
        progress.visibility = if (on) View.VISIBLE else View.GONE
    }

    private fun need(): FirebaseAuth? {
        val a = auth
        if (a == null) showMessage("Sign-in is not switched on in this build yet.", false)
        return a
    }

    // ---------- sign in options ----------

    private fun social(providerId: String) {
        if (busy) return
        val a = need() ?: return
        errorBox.visibility = View.GONE
        setBusy(true)
        a.startActivityForSignInWithProvider(this, OAuthProvider.newBuilder(providerId).build())
            .addOnSuccessListener { result -> finishLogin(result.user, null, null, null) }
            .addOnFailureListener { e ->
                setBusy(false)
                showMessage(friendly(e), false)
            }
    }

    private fun signIn() {
        if (busy) return
        val a = need() ?: return
        val email = findViewById<EditText>(R.id.siEmail).text.toString().trim()
        val pw = findViewById<EditText>(R.id.siPassword).text.toString()
        if (email.isEmpty() || pw.isEmpty()) {
            showMessage("Enter your email and password.", false)
            return
        }
        errorBox.visibility = View.GONE
        setBusy(true)
        a.signInWithEmailAndPassword(email, pw).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finishLogin(task.result?.user, null, null, null)
            } else {
                setBusy(false)
                showMessage(friendly(task.exception), false)
            }
        }
    }

    private fun register() {
        if (busy) return
        val a = need() ?: return
        val first = findViewById<EditText>(R.id.rgFirst).text.toString().trim()
        val last = findViewById<EditText>(R.id.rgLast).text.toString().trim()
        val phone = findViewById<EditText>(R.id.rgPhone).text.toString().trim()
        val email = findViewById<EditText>(R.id.rgEmail).text.toString().trim()
        val pw = findViewById<EditText>(R.id.rgPassword).text.toString()
        val confirm = findViewById<EditText>(R.id.rgConfirm).text.toString()

        if (first.isEmpty() || last.isEmpty()) return showMessage("Enter your first and last name.", false)
        if (email.isEmpty()) return showMessage("Enter your email address.", false)
        if (pw.length < 6) return showMessage("Password must be at least 6 characters.", false)
        if (pw != confirm) return showMessage("Passwords do not match.", false)
        if (!findViewById<CheckBox>(R.id.rgConsent).isChecked)
            return showMessage("Please accept the POPIA consent to continue.", false)

        errorBox.visibility = View.GONE
        setBusy(true)
        a.createUserWithEmailAndPassword(email, pw).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                setBusy(false)
                showMessage(friendly(task.exception), false)
                return@addOnCompleteListener
            }
            val user = task.result?.user
            val change = UserProfileChangeRequest.Builder().setDisplayName("$first $last").build()
            if (user == null) {
                setBusy(false)
                showMessage("Something went wrong. Please try again.", false)
            } else {
                user.updateProfile(change).addOnCompleteListener { finishLogin(user, first, last, phone) }
            }
        }
    }

    private fun sendReset() {
        if (busy) return
        val a = need() ?: return
        val email = findViewById<EditText>(R.id.fpEmail).text.toString().trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Enter a valid email address.", false)
            return
        }
        setBusy(true)
        a.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            setBusy(false)
            val code = (task.exception as? FirebaseAuthException)?.errorCode
            if (task.isSuccessful || code == "ERROR_USER_NOT_FOUND") {
                showMessage("If an account exists for that email, a reset link is on its way. Check your inbox and spam folder.", true)
            } else {
                showMessage(friendly(task.exception), false)
            }
        }
    }

    private fun continueAsGuest() {
        if (busy) return
        val a = need() ?: return
        val alias = findViewById<EditText>(R.id.gsName).text.toString().trim().replace(Regex("\\s+"), " ")
        if (!Regex("^[\\p{L}\\p{N} _.\\-]{2,30}$").matches(alias))
            return showMessage("Choose a name of 2 to 30 characters (letters, numbers, spaces, - _ .).", false)
        if (!findViewById<CheckBox>(R.id.gsConsent).isChecked)
            return showMessage("Please tick the box so we can save your activity under this name.", false)

        errorBox.visibility = View.GONE
        setBusy(true)
        a.signInAnonymously().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                setBusy(false)
                showMessage(friendly(task.exception), false)
                return@addOnCompleteListener
            }
            val user = task.result?.user
            if (user == null) {
                setBusy(false)
                showMessage("Something went wrong. Please try again.", false)
                return@addOnCompleteListener
            }
            val change = UserProfileChangeRequest.Builder().setDisplayName(alias).build()
            user.updateProfile(change).addOnCompleteListener { finishLogin(user, alias, null, null) }
        }
    }

    // ---------- after Firebase says yes ----------

    private fun finishLogin(user: FirebaseUser?, first: String?, last: String?, phone: String?) {
        if (user == null) {
            setBusy(false)
            showMessage("Something went wrong. Please try again.", false)
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { result ->
                val token = result.token
                if (token == null) {
                    setBusy(false)
                    showMessage("Something went wrong. Please try again.", false)
                    return@addOnSuccessListener
                }
                Api.exchange(token, first, last, phone) { role, error ->
                    if (error != null) {
                        setBusy(false)
                        auth?.signOut()
                        showMessage(error, false)
                    } else {
                        val name = user.displayName?.takeIf { it.isNotBlank() }
                            ?: first ?: user.email ?: "COSUP user"
                        val finalRole = role ?: if (user.isAnonymous) "guest" else "user"
                        SessionStore.save(this, name, user.email, finalRole)
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
            .addOnFailureListener { e ->
                setBusy(false)
                showMessage(friendly(e), false)
            }
    }

    // ---------- password strength + confirm match ----------

    private fun setupPasswordChecks() {
        val pw = findViewById<EditText>(R.id.rgPassword)
        val confirm = findViewById<EditText>(R.id.rgConfirm)
        val bar = findViewById<ProgressBar>(R.id.pwBar)
        val label = findViewById<TextView>(R.id.pwLabel)
        val match = findViewById<TextView>(R.id.pwMatch)

        fun updateStrength() {
            val level = strength(pw.text.toString())
            if (level == 0) {
                bar.visibility = View.GONE
                label.visibility = View.GONE
                return
            }
            bar.visibility = View.VISIBLE
            label.visibility = View.VISIBLE
            val (text, color, pct) = when (level) {
                1 -> Triple("Weak", "#DC2626", 33)
                2 -> Triple("Medium", "#F59E0B", 66)
                else -> Triple("Strong", "#16A34A", 100)
            }
            label.text = text
            label.setTextColor(Color.parseColor(color))
            bar.progress = pct
            bar.progressTintList = ColorStateList.valueOf(Color.parseColor(color))
        }

        fun updateMatch() {
            val c = confirm.text.toString()
            if (c.isEmpty()) {
                match.visibility = View.GONE
                return
            }
            val ok = pw.text.toString() == c
            match.text = if (ok) "Passwords match" else "Passwords do not match"
            match.setTextColor(Color.parseColor(if (ok) "#16A34A" else "#DC2626"))
            match.visibility = View.VISIBLE
        }

        pw.doAfterTextChanged {
            updateStrength()
            updateMatch()
        }
        confirm.doAfterTextChanged { updateMatch() }
    }

    /** 0 = empty, 1 = weak, 2 = medium, 3 = strong. */
    private fun strength(p: String): Int {
        if (p.isEmpty()) return 0
        val common = listOf("password", "password1", "12345678", "123456789", "qwerty123", "iloveyou", "admin123", "welcome1", "11111111", "abcd1234")
        if (p.length < 6 || common.contains(p.lowercase())) return 1
        var score = 0
        if (p.length >= 8) score++
        if (p.length >= 12) score++
        if (p.length >= 16) score += 2
        if (p.any { it.isLowerCase() }) score++
        if (p.any { it.isUpperCase() }) score++
        if (p.any { it.isDigit() }) score++
        if (p.any { !it.isLetterOrDigit() }) score++
        return when {
            score <= 3 -> 1
            score == 4 -> 2
            else -> 3
        }
    }

    // ---------- friendly error messages ----------

    private fun friendly(e: Exception?): String {
        if (e is FirebaseNetworkException) return "Network problem. Check your connection."
        val code = (e as? FirebaseAuthException)?.errorCode
        return when (code) {
            "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> "Incorrect email or password."
            "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "That email is already registered. Try signing in."
            "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "This email is already registered with a different sign-in method."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please wait a moment and try again."
            "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in option has not been switched on yet."
            else -> "Something went wrong. ${e?.message ?: "Please try again."}"
        }
    }
}
