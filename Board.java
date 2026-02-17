public class Board {
    public Piece[][] pieces;

    public Board() {
        pieces = new Piece[8][8];
    }

    public void createBoard() {
        String w = "white";
        String b = "black";
        pieces[0][0] = new Rook(b);
        pieces[0][1] = new Knight(b);
        pieces[0][2] = new Bishop(b);
        pieces[0][3] = new Queen(b);
        pieces[0][4] = new King(b);
        pieces[0][5] = new Bishop(b);
        pieces[0][6] = new Knight(b);
        pieces[0][7] = new Rook(b);

        pieces[7][0] = new Rook(w);
        pieces[7][1] = new Knight(w);
        pieces[7][2] = new Bishop(w);
        pieces[7][3] = new Queen(w);
        pieces[7][4] = new King(w);
        pieces[7][5] = new Bishop(w);
        pieces[7][6] = new Knight(w);
        pieces[7][7] = new Rook(w);

        for(int i = 0; i < 8; i++) {
            pieces[1][i] = new Pawn(b);
            pieces[6][i] = new Pawn(w);
        } 

    }

    public Piece pieceThere(int row, int col) {
        if(row > 7 || row < 0 || col > 7 || col < 0) {
            return null;
        }
        Piece piece = pieces[row][col];
        if(piece == null) {
            return null;
        }
        return piece;
    }
}
