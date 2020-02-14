package Model.GameBoard;

import java.util.Arrays;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;

public class GameBoard {
    public Dice[][] diceArray;
    public Slot[][] slotArray;
    public int rows;
    public int columns;

    public GameBoard(Dice[] dices, Slot[] slots, int _rows, int _cols)
    {
        diceArray = new Dice[4][5];
        slotArray = new Slot[4][5];
        int diceIndex = 0;
        int slotIndex = 0;
        for(int row = 0; row < _rows; row++){
            for(int col = 0; col < _cols; col++){
                Dice dice;
                Slot slot;

                dice = (diceIndex > dices.length - 1) ? null : dices[diceIndex];
                slot = (slotIndex > slots.length - 1) ? null : slots[slotIndex];

                if(dice != null && dice.row == row && dice.col == col){
                    //Assign dice to array and increment index for new dice
                    diceArray[row][col] = dice;
                    diceIndex++;
                }
                else{
                    diceArray[row][col] = new Dice("NONE", 0, row, col);
                }
                if(slot != null && slot.row == row && slot.col == col){
                    //Assign slot to array and increment index for new slot
                    slotArray[row][col] = slot;
                    slotIndex++;
                }
                else{
                    slotArray[row][col] = new Slot("NONE", row, col);
                }
            }
        }

        rows = _rows;
        columns = _cols;
    }
}
