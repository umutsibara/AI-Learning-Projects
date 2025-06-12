package com.umutsibara.chess_app

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.umutsibara.chess_app.databinding.FragmentPlayBinding
import kotlin.math.abs

class PlayFragment : Fragment() {

    // ... (önceki değişkenler aynı kalacak)
    private var _binding: FragmentPlayBinding? = null
    private val binding get() = _binding!!

    private var boardState = ChessBoard.getInitialBoard()
    private val squareViews = Array(8) { arrayOfNulls<ImageView>(8) }
    private var isGameStarted = false
    private var isWhiteTurn = true
    private var selectedPiece: ChessPiece? = null
    private var selectedRow = -1
    private var selectedCol = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBoardUI()
        setupClickListeners()
        updateGameStatus()
    }

    // GÜNCELLEME: makeMove fonksiyonu artık kural kontrolü yapacak
    private fun makeMove(targetRow: Int, targetCol: Int) {
        // GÜNCELLEME: Hamle yapmadan önce geçerli olup olmadığını kontrol et
        if (isValidMove(selectedRow, selectedCol, targetRow, targetCol)) {
            // Hamle geçerliyse devam et
            boardState[targetRow][targetCol] = selectedPiece!!
            boardState[selectedRow][selectedCol] = ChessPiece.EMPTY

            placePiecesOnBoard()
            clearSelection()
            refreshAllSquareColors()

            isWhiteTurn = !isWhiteTurn
            updateGameStatus()
        } else {
            // Hamle geçerli değilse uyarı ver
            Toast.makeText(requireContext(), "Geçersiz Hamle!", Toast.LENGTH_SHORT).show()
            // Seçimi iptal et, böylece oyuncu başka bir hamle deneyebilir
            clearSelection()
            refreshAllSquareColors()
        }
    }

    // --- YENİ EKLENEN HAMLE KONTROL MANTIĞI ---

    private fun isValidMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        if (fromRow == toRow && fromCol == toCol) return false // Aynı yere hamle yapılamaz

        val piece = boardState[fromRow][fromCol]
        val targetPiece = boardState[toRow][toCol]

        // Kendi taşının üzerine oynayamazsın
        if (targetPiece != ChessPiece.EMPTY && targetPiece.isWhite == piece.isWhite) {
            return false
        }

        return when (piece.getPieceType()) {
            PieceType.PAWN -> isValidPawnMove(fromRow, fromCol, toRow, toCol, piece)
            PieceType.ROOK -> isValidRookMove(fromRow, fromCol, toRow, toCol)
            PieceType.KNIGHT -> isValidKnightMove(fromRow, fromCol, toRow, toCol)
            PieceType.BISHOP -> isValidBishopMove(fromRow, fromCol, toRow, toCol)
            PieceType.QUEEN -> isValidQueenMove(fromRow, fromCol, toRow, toCol)
            PieceType.KING -> isValidKingMove(fromRow, fromCol, toRow, toCol)
            PieceType.EMPTY -> false
        }
    }

    private fun isValidPawnMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, piece: ChessPiece): Boolean {
        val targetPiece = boardState[toRow][toCol]
        val direction = if (piece.isWhite) -1 else 1 // Beyaz yukarı (-1), siyah aşağı (+1) gider
        val startRow = if (piece.isWhite) 6 else 1

        // 1. Düz ilerleme (bir kare)
        if (fromCol == toCol && targetPiece == ChessPiece.EMPTY && toRow == fromRow + direction) {
            return true
        }
        // 2. İlk hamlede iki kare ilerleme
        if (fromCol == toCol && fromRow == startRow && targetPiece == ChessPiece.EMPTY && toRow == fromRow + 2 * direction) {
            // Aradaki karenin de boş olduğunu kontrol et
            if (boardState[fromRow + direction][fromCol] == ChessPiece.EMPTY) {
                return true
            }
        }
        // 3. Çapraz yeme
        if (abs(fromCol - toCol) == 1 && toRow == fromRow + direction && targetPiece != ChessPiece.EMPTY) {
            return true
        }

        return false
    }

    private fun isValidRookMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        // Düz bir çizgide hareket etmeli (ya satır ya da sütun aynı kalmalı)
        if (fromRow != toRow && fromCol != toCol) return false

        // Engel kontrolü
        if (fromRow == toRow) { // Yatay hareket
            val step = if (toCol > fromCol) 1 else -1
            for (c in (fromCol + step) until toCol step step) {
                if (boardState[fromRow][c] != ChessPiece.EMPTY) return false
            }
        } else { // Dikey hareket
            val step = if (toRow > fromRow) 1 else -1
            for (r in (fromRow + step) until toRow step step) {
                if (boardState[r][fromCol] != ChessPiece.EMPTY) return false
            }
        }
        return true
    }

    private fun isValidKnightMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        val dRow = abs(fromRow - toRow)
        val dCol = abs(fromCol - toCol)
        // L şeklinde hareket: 2'ye 1 veya 1'e 2
        return (dRow == 2 && dCol == 1) || (dRow == 1 && dCol == 2)
    }

    private fun isValidBishopMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        // Çapraz hareket etmeli (satır ve sütun farkları eşit olmalı)
        if (abs(fromRow - toRow) != abs(fromCol - toCol)) return false

        // Engel kontrolü
        val rowStep = if (toRow > fromRow) 1 else -1
        val colStep = if (toCol > fromCol) 1 else -1
        var r = fromRow + rowStep
        var c = fromCol + colStep
        while (r != toRow || c != toCol) {
            if (boardState[r][c] != ChessPiece.EMPTY) return false
            r += rowStep
            c += colStep
        }
        return true
    }

    private fun isValidQueenMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        // Vezir, kale veya fil gibi hareket edebilir
        return isValidRookMove(fromRow, fromCol, toRow, toCol) || isValidBishopMove(fromRow, fromCol, toRow, toCol)
    }

    private fun isValidKingMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        // Her yöne sadece bir kare
        val dRow = abs(fromRow - toRow)
        val dCol = abs(fromCol - toCol)
        return dRow <= 1 && dCol <= 1
    }

    // --- (Diğer tüm fonksiyonlar aynı kalacak, aşağıya ekliyorum) ---

    private fun setupBoardUI() { /* ... aynı kod ... */
        val displayMetrics = resources.displayMetrics
        val boardSizePx = (320 * displayMetrics.density).toInt()
        val squareSize = boardSizePx / 8

        binding.chessBoard.removeAllViews()

        for (row in 0..7) {
            for (col in 0..7) {
                val square = ImageView(requireContext())
                val params = android.widget.GridLayout.LayoutParams()
                params.width = squareSize
                params.height = squareSize
                params.rowSpec = android.widget.GridLayout.spec(row)
                params.columnSpec = android.widget.GridLayout.spec(col)
                square.layoutParams = params
                square.scaleType = ImageView.ScaleType.CENTER_CROP

                refreshSquareColor(row, col)

                square.setOnClickListener {
                    onSquareClicked(row, col)
                }

                squareViews[row][col] = square
                binding.chessBoard.addView(square)
            }
        }
    }

    private fun placePiecesOnBoard() { /* ... aynı kod ... */
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = boardState[row][col]
                val squareView = squareViews[row][col]

                if (piece != ChessPiece.EMPTY) {
                    squareView?.setImageResource(piece.getDrawableResId(requireContext()))
                } else {
                    squareView?.setImageDrawable(null)
                }
            }
        }
    }

    private fun setupClickListeners() { /* ... aynı kod ... */
        binding.btnStartGame.setOnClickListener {
            startGame()
        }
        binding.btnResetGame.setOnClickListener {
            resetGame()
        }
    }

    private fun onSquareClicked(row: Int, col: Int) { /* ... aynı kod ... */
        if (!isGameStarted) {
            Toast.makeText(requireContext(), "Önce oyunu başlatın!", Toast.LENGTH_SHORT).show()
            return
        }

        val clickedPiece = boardState[row][col]

        if (selectedPiece == null) {
            if (clickedPiece != ChessPiece.EMPTY && clickedPiece.isWhite == isWhiteTurn) {
                selectPiece(row, col, clickedPiece)
            }
        } else {
            if (selectedRow == row && selectedCol == col) {
                clearSelection()
            } else {
                makeMove(row, col)
            }
        }
    }

    private fun selectPiece(row: Int, col: Int, piece: ChessPiece) { /* ... aynı kod ... */
        clearSelection()

        selectedPiece = piece
        selectedRow = row
        selectedCol = col

        squareViews[row][col]?.setBackgroundColor(Color.parseColor("#6A875A"))
    }

    private fun clearSelection() { /* ... aynı kod ... */
        if (selectedRow != -1) {
            refreshSquareColor(selectedRow, selectedCol)
        }
        selectedPiece = null
        selectedRow = -1
        selectedCol = -1
    }

    private fun refreshSquareColor(row: Int, col: Int) { /* ... aynı kod ... */
        val isLightSquare = (row + col) % 2 == 0
        val color = if (isLightSquare) Color.parseColor("#F0D9B5") else Color.parseColor("#B58863")
        squareViews[row][col]?.setBackgroundColor(color)
    }

    private fun refreshAllSquareColors() { /* ... aynı kod ... */
        for (r in 0..7) {
            for (c in 0..7) {
                refreshSquareColor(r, c)
            }
        }
    }

    private fun startGame() { /* ... aynı kod ... */
        boardState = ChessBoard.getInitialBoard()
        placePiecesOnBoard()
        refreshAllSquareColors()
        isGameStarted = true
        isWhiteTurn = true
        binding.btnStartGame.text = "Oyun Devam Ediyor"
        binding.btnStartGame.isEnabled = false
        updateGameStatus()
    }

    private fun resetGame() { /* ... aynı kod ... */
        isGameStarted = false
        clearSelection()
        boardState = ChessBoard.getInitialBoard()
        placePiecesOnBoard()
        refreshAllSquareColors()
        binding.btnStartGame.text = "Oyunu Başlat"
        binding.btnStartGame.isEnabled = true
        updateGameStatus()
    }

    private fun updateGameStatus() { /* ... aynı kod ... */
        if (isGameStarted) {
            binding.tvCurrentPlayer.text = if (isWhiteTurn) "Sıra: Beyaz" else "Sıra: Siyah"
        } else {
            binding.tvGameStatus.text = "Satranç Oyunu"
            binding.tvCurrentPlayer.text = "Oyunu başlatın"
        }
    }

    override fun onDestroyView() { /* ... aynı kod ... */
        super.onDestroyView()
        _binding = null
    }
}