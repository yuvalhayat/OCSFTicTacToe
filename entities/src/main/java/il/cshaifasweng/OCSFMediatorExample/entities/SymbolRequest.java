package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class SymbolRequest implements Serializable {
    //private static final long serialVersionUID = -8224097662914849956L;

    private char mySymbol;
    private char opponentSymbol;
    public SymbolRequest(char mySymbol,char opponentSymbol) {
        this.mySymbol = mySymbol;
        this.opponentSymbol = opponentSymbol;
    }

    public char getMySymbol() {return mySymbol;}
    public char getOpponentSymbol() {return opponentSymbol;}

}
