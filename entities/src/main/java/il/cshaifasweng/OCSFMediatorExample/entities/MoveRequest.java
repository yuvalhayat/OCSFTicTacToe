package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class MoveRequest implements Serializable {
    //private static final long serialVersionUID = -8224097662914849956L;

    private int row;
    private int col;
    private char symbol;
    public MoveRequest(int row, int col, char symbol) {
        this.row = row;
        this.col = col;
        this.symbol = symbol;
    }

    public int getCol() {return col;}
    public int getRow() {return row;}
    public char getSymbol() {return symbol;}
}
