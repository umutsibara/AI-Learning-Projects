package com.umutsibara.chess_app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.TimeUnit

class ClockFragment : Fragment(), ChessClockListener {

    interface OnClockInteractionListener {
        fun hideBottomNav()
        fun showBottomNav()
    }

    private var interactionListener: OnClockInteractionListener? = null
    private var chessClock: ChessClock? = null
    private var isGameSetup = false

    private lateinit var player1Area: ConstraintLayout
    private lateinit var player2Area: ConstraintLayout
    private lateinit var player1TimeText: TextView
    private lateinit var player2TimeText: TextView

    private val activeColor = Color.parseColor("#34C759")
    private val inactiveColor = Color.parseColor("#3A3A3C")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnClockInteractionListener) {
            interactionListener = context
        } else {
            throw RuntimeException("$context must implement OnClockInteractionListener")
        }
    }

    fun handleBackPress(): Boolean {
        if (chessClock?.isRunning() == true && chessClock?.isPaused() == false) {
            chessClock?.pause()
            exitFullScreen()
            interactionListener?.showBottomNav()
            return true
        }
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player1Area = view.findViewById(R.id.player1_area)
        player2Area = view.findViewById(R.id.player2_area)
        player1TimeText = view.findViewById(R.id.player1_time_text)
        player2TimeText = view.findViewById(R.id.player2_time_text)

        updateTimeDisplay(player1TimeText, 0)
        updateTimeDisplay(player2TimeText, 0)

        val clickListener = View.OnClickListener { v ->
            if (!isGameSetup) {
                showTimeSettingsDialog()
            } else if (chessClock?.isPaused() == true) {
                chessClock?.resume()
            } else {
                val player = if (v.id == R.id.player1_area) Player.PLAYER_ONE else Player.PLAYER_TWO
                chessClock?.handleTap(player)
            }
        }

        player1Area.setOnClickListener(clickListener)
        player2Area.setOnClickListener(clickListener)
    }

    override fun onClockPaused() {
        activity?.runOnUiThread {
            Toast.makeText(context, "Durduruldu", Toast.LENGTH_SHORT).show()
            player1Area.setBackgroundColor(inactiveColor)
            player2Area.setBackgroundColor(inactiveColor)
        }
    }

    override fun onClockResumed() {
        activity?.runOnUiThread {
            Toast.makeText(context, "Devam ediyor...", Toast.LENGTH_SHORT).show()
            chessClock?.activePlayer?.let { onTurnSwitched(it) }
        }
    }

    private fun showTimeSettingsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_time_settings, null)

        val minutesInput = dialogView.findViewById<EditText>(R.id.edit_text_minutes)
        val secondsInput = dialogView.findViewById<EditText>(R.id.edit_text_seconds)
        val incrementInput = dialogView.findViewById<EditText>(R.id.edit_text_increment)

        builder.setView(dialogView)
            .setTitle("Saat Ayarları")
            .setPositiveButton("Başlat") { _, _ ->
                val minutes = minutesInput.text.toString().toLongOrNull() ?: 0
                val seconds = secondsInput.text.toString().toLongOrNull() ?: 0
                val increment = incrementInput.text.toString().toLongOrNull() ?: 0
                val initialTimeMillis = TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds)

                if (initialTimeMillis == 0L) {
                    Toast.makeText(context, "Lütfen geçerli bir süre girin.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val incrementMillis = TimeUnit.SECONDS.toMillis(increment)

                chessClock = ChessClock(initialTimeMillis, incrementMillis, this)
                isGameSetup = true

                updateTimeDisplay(player1TimeText, initialTimeMillis)
                updateTimeDisplay(player2TimeText, initialTimeMillis)

                Toast.makeText(context, "Saat hazır. Başlamak için dokunun.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("İptal", null)
        builder.create().show()
    }

    override fun onTick(player1Time: Long, player2Time: Long) {
        activity?.runOnUiThread {
            updateTimeDisplay(player1TimeText, player1Time)
            updateTimeDisplay(player2TimeText, player2Time)
        }
    }

    override fun onTurnSwitched(activePlayer: Player) {
        activity?.runOnUiThread {
            player1Area.setBackgroundColor(if (activePlayer == Player.PLAYER_ONE) activeColor else inactiveColor)
            player2Area.setBackgroundColor(if (activePlayer == Player.PLAYER_TWO) activeColor else inactiveColor)
        }
    }

    override fun onGameOver(winner: Player) {
        isGameSetup = false
        activity?.runOnUiThread {
            Toast.makeText(context, "Oyun Bitti! Kazanan: ${if(winner == Player.PLAYER_ONE) "Oyuncu 1" else "Oyuncu 2"}", Toast.LENGTH_LONG).show()
            player1Area.setBackgroundColor(inactiveColor)
            player2Area.setBackgroundColor(inactiveColor)
            interactionListener?.showBottomNav()
            exitFullScreen()
        }
    }

    override fun onGameStart() {
        goFullScreen()
        interactionListener?.hideBottomNav()
    }

    private fun updateTimeDisplay(textView: TextView, millis: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        textView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun goFullScreen() {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun exitFullScreen() {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, true)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chessClock?.destroy()
        interactionListener?.showBottomNav()
        exitFullScreen()
    }

    override fun onDetach() {
        super.onDetach()
        interactionListener = null
    }
}