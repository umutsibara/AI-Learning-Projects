package com.umutsibara.chess_app

import android.os.CountDownTimer

interface ChessClockListener {
    fun onTick(player1Time: Long, player2Time: Long)
    fun onTurnSwitched(activePlayer: Player)
    fun onGameOver(winner: Player)
    fun onGameStart()
    fun onClockPaused()
    fun onClockResumed()
}

enum class Player {
    PLAYER_ONE, PLAYER_TWO
}

class ChessClock(
    private val initialTimeMillis: Long,
    private val incrementMillis: Long,
    private val listener: ChessClockListener
) {
    private var player1Time = initialTimeMillis
    private var player2Time = initialTimeMillis
    private var player1Timer: CountDownTimer? = null
    private var player2Timer: CountDownTimer? = null
    var activePlayer: Player? = null
        private set
    private var isRunning = false
    private var isPaused = false

    fun isPaused() = this.isPaused
    fun isRunning() = this.isRunning

    fun handleTap(tappedPlayer: Player) {
        if (!isRunning) {
            isRunning = true
            val startingPlayer = if (tappedPlayer == Player.PLAYER_ONE) Player.PLAYER_TWO else Player.PLAYER_ONE
            switchTurn(startingPlayer)
            listener.onGameStart()
            return
        }
        if (isPaused) return

        if (tappedPlayer == activePlayer) {
            stopCurrentTimer()
            addIncrement(tappedPlayer)
            val nextPlayer = if (tappedPlayer == Player.PLAYER_ONE) Player.PLAYER_TWO else Player.PLAYER_ONE
            switchTurn(nextPlayer)
        }
    }

    fun pause() {
        if (isRunning && !isPaused) {
            stopCurrentTimer()
            isPaused = true
            listener.onClockPaused()
        }
    }

    fun resume() {
        if (isRunning && isPaused) {
            isPaused = false
            activePlayer?.let { startTimerFor(it) }
            listener.onClockResumed()
        }
    }

    private fun switchTurn(nextPlayer: Player) {
        activePlayer = nextPlayer
        startTimerFor(nextPlayer)
        listener.onTurnSwitched(nextPlayer)
    }

    private fun startTimerFor(player: Player) {
        val timeToCount = if (player == Player.PLAYER_ONE) player1Time else player2Time
        val timer = createTimer(timeToCount) { newTime ->
            if (player == Player.PLAYER_ONE) player1Time = newTime else player2Time = newTime
            listener.onTick(player1Time, player2Time)
        }
        if (player == Player.PLAYER_ONE) player1Timer = timer else player2Timer = timer
        timer.start()
    }

    private fun createTimer(timeMillis: Long, onTickAction: (Long) -> Unit): CountDownTimer {
        return object : CountDownTimer(timeMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                onTickAction(millisUntilFinished)
            }
            override fun onFinish() {
                onTickAction(0)
                isRunning = false
                val winner = if (activePlayer == Player.PLAYER_ONE) Player.PLAYER_TWO else Player.PLAYER_ONE
                listener.onGameOver(winner)
            }
        }
    }

    private fun stopCurrentTimer() {
        if (activePlayer == Player.PLAYER_ONE) player1Timer?.cancel() else player2Timer?.cancel()
    }

    private fun addIncrement(player: Player) {
        if (player == Player.PLAYER_ONE) {
            player1Time += incrementMillis
        } else {
            player2Time += incrementMillis
        }
        listener.onTick(player1Time, player2Time)
    }

    fun destroy() {
        player1Timer?.cancel()
        player2Timer?.cancel()
        isRunning = false
        isPaused = false
    }
}