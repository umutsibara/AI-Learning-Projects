package com.umutsibara.chess_app

enum class ChessPiece(val drawableName: String, val isWhite: Boolean) {
    // Beyaz taşlar
    WHITE_KING("kingwhite", true),
    WHITE_QUEEN("queenwhite", true),
    WHITE_ROOK("rookwhite", true),
    WHITE_BISHOP("bishopwhite", true),
    WHITE_KNIGHT("knightwhite", true),
    WHITE_PAWN("pawnwhite", true),

    // Siyah taşlar
    BLACK_KING("kingblack", false),
    BLACK_QUEEN("queenblack", false),
    BLACK_ROOK("rookblack", false),
    BLACK_BISHOP("bishopblack", false),
    BLACK_KNIGHT("knightblack", false),
    BLACK_PAWN("pawnblack", false),

    // Boş kare
    EMPTY("", false);

    // Taşın türünü döndür (renk bağımsız)
    fun getPieceType(): PieceType {
        return when (this) {
            WHITE_KING, BLACK_KING -> PieceType.KING
            WHITE_QUEEN, BLACK_QUEEN -> PieceType.QUEEN
            WHITE_ROOK, BLACK_ROOK -> PieceType.ROOK
            WHITE_BISHOP, BLACK_BISHOP -> PieceType.BISHOP
            WHITE_KNIGHT, BLACK_KNIGHT -> PieceType.KNIGHT
            WHITE_PAWN, BLACK_PAWN -> PieceType.PAWN
            EMPTY -> PieceType.EMPTY
        }
    }

    // Drawable resource ID'sini döndür
    fun getDrawableResId(context: android.content.Context): Int {
        return if (this == EMPTY) {
            0 // Boş kare için drawable yok
        } else {
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        }
    }
    fun getValue(): Int {
        return when (getPieceType()) {
            PieceType.PAWN -> 1
            PieceType.KNIGHT -> 3
            PieceType.BISHOP -> 3
            PieceType.ROOK -> 5
            PieceType.QUEEN -> 9
            PieceType.KING -> 0 // Kral değersiz (oyun bitirir)
            PieceType.EMPTY -> 0
        }
    }
}

// Taş türleri (renk bağımsız)
enum class PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN, EMPTY
}

// Satranç tahtası başlangıç pozisyonları
object ChessBoard {

    // Başlangıç tahtası - 8x8 dizi
    fun getInitialBoard(): Array<Array<ChessPiece>> {
        return arrayOf(
            // Siyah taşlar (üst sıra) - 0. satır
            arrayOf(
                ChessPiece.BLACK_ROOK, ChessPiece.BLACK_KNIGHT, ChessPiece.BLACK_BISHOP, ChessPiece.BLACK_QUEEN,
                ChessPiece.BLACK_KING, ChessPiece.BLACK_BISHOP, ChessPiece.BLACK_KNIGHT, ChessPiece.BLACK_ROOK
            ),
            // Siyah piyonlar - 1. satır
            arrayOf(
                ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN,
                ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN
            ),
            // Boş satırlar - 2,3,4,5. satırlar
            arrayOf(
                ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY,
                ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY
            ),
            arrayOf(
                ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY,
                ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY
            ),
            arrayOf(
                ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY,
                ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY
            ),
            arrayOf(
                ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY,
                ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY
            ),
            // Beyaz piyonlar - 6. satır
            arrayOf(
                ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN,
                ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN
            ),
            // Beyaz taşlar (alt sıra) - 7. satır
            arrayOf(
                ChessPiece.WHITE_ROOK, ChessPiece.WHITE_KNIGHT, ChessPiece.WHITE_BISHOP, ChessPiece.WHITE_QUEEN,
                ChessPiece.WHITE_KING, ChessPiece.WHITE_BISHOP, ChessPiece.WHITE_KNIGHT, ChessPiece.WHITE_ROOK
            )
        )
    }
}
