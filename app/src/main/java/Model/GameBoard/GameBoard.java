package Model.GameBoard;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import Model.Points.Quests.CQ_TYPES;
import Model.Points.Quests.PQ_TYPES;
import Model.Points.Quests.Quest;
import Model.Rules.RuleHandler;

public class GameBoard {
    private Dice[][] diceArray;
    private Slot[][] slotArray;
    public int rows;
    public int columns;
    private RuleHandler ruleHandler;

    public GameBoard(/*Dice[] dices, Slot[] slots, */int _rows, int _cols)
    {
        diceArray = new Dice[4][5];
        slotArray = new Slot[4][5];
        rows = _rows;
        columns = _cols;
        ruleHandler = new RuleHandler();
        /*int diceIndex = 0;
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
        }*/
    }

    public int Evaluation(PQ_TYPES personalQuest, CQ_TYPES commonQuest, int craftsmanPoints)
    {
        Quest quest = new Quest();

        quest.SetPersonalCalculator(personalQuest);
        quest.SetCommonCalculator(commonQuest);

        return quest.RunEvaluation(diceArray, craftsmanPoints);
    }
    public boolean ruleCheck(){
        return ruleHandler.CheckRules();
    }

    public void setDiceArray(Dice[] dices){
        adaptArray(dices);
    }
    public void setSlotArray(Slot[] slots){
        adaptArray(slots);
    }
    public void assignToRuleHandler(){
        ruleHandler.assignArray(diceArray);
        ruleHandler.assignArray(slotArray);
    }

    private void adaptArray(Dice[] dices){
        int index = 0;
        for(int row = 0; row < rows; row++){
            for(int col = 0; col < columns; col++){
                Dice dice;

                dice = (index > dices.length - 1) ? null : dices[index];

                if(dice != null && dice.row == row && dice.col == col){
                    //Assign dice to array and increment index for new dice
                    diceArray[row][col] = dice;
                    index++;
                }
                else{
                    diceArray[row][col] = new Dice("NONE", 0, row, col);
                }
            }
        }
    }
    private void adaptArray(Slot[] slots){
        int index = 0;
        for(int row = 0; row < rows; row++){
            for(int col = 0; col < columns; col++){
                Dice dice;
                Slot slot;

                slot = (index > slots.length - 1) ? null : slots[index];

                if(slot != null && slot.row == row && slot.col == col){
                    //Assign slot to array and increment index for new slot
                    slotArray[row][col] = slot;
                    index++;
                }
                else{
                    slotArray[row][col] = new Slot("NONE", row, col);
                }
            }
        }
    }
}
