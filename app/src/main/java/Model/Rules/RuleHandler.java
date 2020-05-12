package Model.Rules;

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
    private int current_eglomise;
    private int current_sandpaper;
    public boolean edgeRule;

    public RuleHandler(Dice[][] dices, Slot[][] slots)
    {
        diceArray = dices;
        slotArray = slots;
        edgeRule = false;
    }
    public RuleHandler(){
    }

    public boolean CheckRules(int eglomiseCount, int sandpaperCount)
    {
        current_eglomise = eglomiseCount;
        current_sandpaper = sandpaperCount;

        edgeRule = IsEdgeDiceRule();

        for(int row = 0; row < 4; row++){
            for(int col = 0; col < 5; col++){
                if(diceArray[row][col].number == 0){
                    continue;
                }
                /*if(IsEdgeDiceRule(row, col)){
                    edgeRule = true;
                } {
                    diceArray[row][col].errType = RULE_ERR.EDGE_ERR;
                }*/
                if(!SlotConditionRule(row, col)){
                    diceArray[row][col].errType = RULE_ERR.SLOT_ERR;
                    return false;
                }
                if(!IsNeighborRule(row, col)){
                    diceArray[row][col].errType = RULE_ERR.NEIGHBOR_ERR;
                    return false;
                }
                if(!IsDiffDice(row, col)){
                    diceArray[row][col].errType = RULE_ERR.DIFF_ERR;
                    return false;
                }
                diceArray[row][col].errType = RULE_ERR.NO_ERR;
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
    private Boolean IsEdgeDiceRule(/*int row, int col*/)
    {
        for(Dice[] row : diceArray){
            for(Dice dice : row){
                if(dice.number != 0){
                    if(dice.row == 0 || dice.row == 4){
                        if(dice.col >= 0 && dice.col <= 5){
                            return true;
                        }
                    }
                    if(dice.row > 0 && dice.row < 4){
                        if(dice.col == 0 || dice.col == 5){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
        /*if(diceArray[row][col].row == 0 || diceArray[row][col].row == 4){
            if(diceArray[row][col].col >= 0 && diceArray[row][col].col <= 5){
                return true;
            }
        }
        if(diceArray[row][col].row > 0 && diceArray[row][col].row < 4){
            if(diceArray[row][col].col == 0 || diceArray[row][col].col == 5){
                return true;
            }
        }
        return false;*/
    }
    private Boolean IsDiffDice(int row, int col)
    {
        if(diceArray.length == 1){
            return true;
        }
        //Up
        boolean up = true;
        if(diceArray[row][col].row - 1 >= 0){
            if(diceArray[row - 1][col].number != 0){
                up = DiffColors(diceArray[row][col], diceArray[row - 1][col]) && DiffNumbers(diceArray[row][col], diceArray[row - 1][col]);
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
        //Down
        boolean down = true;
        if(diceArray[row][col].row + 1 < 4){
            if(diceArray[row + 1][col].number != 0){
                down = DiffColors(diceArray[row][col], diceArray[row + 1][col]) && DiffNumbers(diceArray[row][col], diceArray[row + 1][col]);
            }
        }
        return up && left && right && down;
    }
    private Boolean SlotColorRule(Dice dice, Slot slot)
    {
        if(slot.infoType.equals("COLOR")){
            if(dice.color.equals(slot.info.name())){
                return true;
            } else if(current_eglomise > 0){
                current_eglomise--;
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
        //return (slot.infoType.equals("COLOR")) ? (dice.color.equals(slot.info.name())) : true;
    }
    private Boolean SlotNumberRule(Dice dice, Slot slot)
    {
        if(slot.infoType.equals("NUMBER")){
            if(dice.number == slot.info.StrToNum()){
                return true;
            } else if(current_sandpaper > 0){
                current_sandpaper--;
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
        //return (slot.infoType.equals("NUMBER")) ? (dice.number == slot.info.StrToNum()) : true;
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
    public Dice[][] getDices(){ return diceArray; }
}
