package Model.Rules;

import android.util.Log;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;

public class RuleHandler {
    /*
    * RULES:
    * One dice has to be in corner or edge position
    * Each dice has at least one neighbor in any direction
    * Dice has to fullfill condition of given slot (color / number)
    * Edge of dice cannot has neighbor of the same color / number
    * */
    private Dice[][] diceArray;
    private Slot[][] slotArray;

    public RuleHandler(Dice[][] dices, Slot[][] slots)
    {
        diceArray = dices;
        slotArray = slots;
    }
    public RuleHandler(){}

    public boolean CheckRules()
    {
        boolean edgeRule = false;
        for(int row = 0; row < 4; row++){
            for(int col = 0; col < 5; col++){
                if(diceArray[row][col].number == 0){
                    continue;
                }
                if(IsEdgeDiceRule(row, col)){
                    edgeRule = true;
                }
                if(!SlotConditionRule(row, col)){
                    Log.println(Log.ERROR, "Slot Cond", row+" | "+col);
                    return false;
                }
                if(!IsNeighborRule(row, col)){
                    Log.println(Log.ERROR, "Neighbor rule", row+" | "+col);
                    return false;
                }
                if(!IsDiffDice(row, col)){
                    Log.println(Log.ERROR, "Diff dice rule", row+" | "+col);
                    return false;
                }
            }
        }
        return edgeRule;
    }

    private Boolean SlotConditionRule(int row, int col)
    {
        return SlotColorRule(diceArray[row][col], slotArray[row][col]) && SlotNumberRule(diceArray[row][col], slotArray[row][col]);
    }
    private Boolean IsNeighborRule(int row, int col)
    {
        if(diceArray.length == 1){
            return true;
        }
        //LT
        boolean leftTop = true;
        if(diceArray[row][col].row - 1 >= 0 && diceArray[row][col].col - 1 >= 0){
            leftTop = diceArray[row - 1][col - 1].number != 0;
        }
        //Up
        boolean up = true;
        if(diceArray[row][col].row - 1 >= 0){
            up = diceArray[row - 1][col].number != 0;
        }
        //RT
        boolean rightTop = true;
        if(diceArray[row][col].row - 1 >= 0 && diceArray[row][col].col + 1 < 5){
            rightTop = diceArray[row - 1][col + 1].number != 0;
        }
        //L
        boolean left = true;
        if(diceArray[row][col].col - 1 >= 0){
            left = diceArray[row][col - 1].number != 0;
        }
        //R
        boolean right = true;
        if(diceArray[row][col].col + 1 < 5){
            right = diceArray[row][col + 1].number != 0;
        }
        //LB
        boolean leftBottom = true;
        if(diceArray[row][col].row + 1 < 4 && diceArray[row][col].col - 1 >= 0){
            leftBottom = diceArray[row + 1][col - 1].number != 0;
        }
        //Down
        boolean down = true;
        if(diceArray[row][col].row + 1 < 4){
            down = diceArray[row + 1][col].number != 0;
        }
        //RB
        boolean rightBottom = true;
        if(diceArray[row][col].row + 1 < 4 && diceArray[row][col].col + 1 < 5){
            rightBottom = diceArray[row + 1][col + 1].number != 0;
        }
        return leftTop || up || rightTop || left || right || leftBottom || down || rightBottom;
    }
    private Boolean IsEdgeDiceRule(int row, int col)
    {
        if(diceArray[row][col].row == 0 || diceArray[row][col].row == 4){
            if(diceArray[row][col].col >= 0 && diceArray[row][col].col <= 5){
                return true;
            }
        }
        if(diceArray[row][col].row > 0 && diceArray[row][col].row < 4){
            if(diceArray[row][col].col == 0 || diceArray[row][col].col == 5){
                return true;
            }
        }
        return false;
    }
    private Boolean IsDiffDice(int row, int col)
    {
        if(diceArray.length == 1){
            return true;
        }
        //LT
        boolean leftTop = true;
        if(diceArray[row][col].row - 1 >= 0 && diceArray[row][col].col - 1 >= 0){
            if(diceArray[row - 1][col - 1].number != 0){
                leftTop = DiffColors(diceArray[row][col], diceArray[row - 1][col - 1]) && DiffNumbers(diceArray[row][col], diceArray[row - 1][col - 1]);
            }
        }
        //Up
        boolean up = true;
        if(diceArray[row][col].row - 1 >= 0){
            if(diceArray[row - 1][col].number != 0){
                up = DiffColors(diceArray[row][col], diceArray[row - 1][col]) && DiffNumbers(diceArray[row][col], diceArray[row - 1][col]);
            }
        }
        //RT
        boolean rightTop = true;
        if(diceArray[row][col].row - 1 >= 0 && diceArray[row][col].col + 1 < 5){
            if(diceArray[row - 1][col + 1].number != 0){
                rightTop = DiffColors(diceArray[row][col], diceArray[row - 1][col + 1]) && DiffNumbers(diceArray[row][col], diceArray[row - 1][col + 1]);
            }
        }
        //L
        boolean left = true;
        if(diceArray[row][col].col - 1 >= 0){
            if(diceArray[row][col - 1].number != 0){
                left = DiffColors(diceArray[row][col], diceArray[row][col - 1]) && DiffNumbers(diceArray[row][col], diceArray[row][col - 1]);
            }
        }
        //R
        boolean right = true;
        if(diceArray[row][col].col + 1 < 5){
            if(diceArray[row][col + 1].number != 0){
                right = DiffColors(diceArray[row][col], diceArray[row][col + 1]) && DiffNumbers(diceArray[row][col], diceArray[row][col + 1]);
            }
        }
        //LB
        boolean leftBottom = true;
        if(diceArray[row][col].row + 1 < 4 && diceArray[row][col].col - 1 >= 0){
            if(diceArray[row + 1][col - 1].number != 0){
                leftBottom = DiffColors(diceArray[row][col], diceArray[row + 1][col - 1]) && DiffNumbers(diceArray[row][col], diceArray[row + 1][col - 1]);
            }
        }
        //Down
        boolean down = true;
        if(diceArray[row][col].row + 1 < 4){
            if(diceArray[row + 1][col].number != 0){
                down = DiffColors(diceArray[row][col], diceArray[row + 1][col]) && DiffNumbers(diceArray[row][col], diceArray[row + 1][col]);
            }
        }
        //RB
        boolean rightBottom = true;
        if(diceArray[row][col].row + 1 < 4 && diceArray[row][col].col + 1 < 5){
            if(diceArray[row + 1][col + 1].number != 0){
                rightBottom = DiffColors(diceArray[row][col], diceArray[row + 1][col + 1]) && DiffNumbers(diceArray[row][col], diceArray[row + 1][col + 1]);
            }
        }
        return leftTop && up && rightTop && left && right && leftBottom && down && rightBottom;
    }

    private Boolean SlotColorRule(Dice dice, Slot slot)
    {
        return (slot.infoType.equals("COLOR")) ? (dice.color.equals(slot.info.name())) : true;
    }
    private Boolean SlotNumberRule(Dice dice, Slot slot)
    {
        return (slot.infoType.equals("NUMBER")) ? (dice.number == slot.info.StrToNum()) : true;
    }
    private Boolean DiffColors(Dice dice1, Dice dice2)
    {
        return !dice1.color.equals(dice2.color);
    }
    private Boolean DiffNumbers(Dice dice1, Dice dice2)
    {
        return dice1.number != dice2.number;
    }

    public void assignArray(Dice[][] dices){ diceArray = dices; }
    public void assignArray(Slot[][] slots){ slotArray = slots; }
}
