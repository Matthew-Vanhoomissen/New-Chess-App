package game;
/**
 * Object to store row and column coordinates as a singular object
 * 
 * @author Matthew-Vanhoomissen
 */

public class Position {
    public int row;
    public int col;

    /**
     * Basic constructor
     * 
     * @param row
     * @param col
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Converts position into format used when traversing the board array
     * 
     * @return string
     */
    public String toString() {
        return "[" + row + "][" + col + "]";
    }

    /**
     * Basic method to check if positions share row and column
     * 
     * @param other position to compare
     * @return where they are equal
     */
    public boolean equals(Position other) {
        if(row == other.row && col == other.col) {
            return true;
        }
        return false;
    }
}
