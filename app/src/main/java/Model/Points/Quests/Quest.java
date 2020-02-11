package Model.Points.Quests;

import Model.Structs.Dice;

public class Quest {
    IQuestCalculate personalQCalculator;
    IQuestCalculate commonQCalculator;

    public void SetPersonalCalculator(PQ_TYPES qType)
    {
        IQuestCalculate iCalculate;
        switch (qType){
            case RUBY:
                iCalculate = (Dice[] dices) -> {
                    //TODO:lambda test
                    int points = 0;
                    for (Dice dice: dices) {
                        if(dice.color == "RED"){
                            points += dice.number;
                        }
                    }
                    return points;
                };
                break;
            case TOPAZ:
                iCalculate = (Dice[] dices) -> {
                    //TODO:lambda test
                    int points = 0;
                    for (Dice dice: dices) {
                        if(dice.color == "YELLOW"){
                            points += dice.number;
                        }
                    }
                    return points;
                };
                break;
            case EMERALD:
                iCalculate = (Dice[] dices) -> {
                    //TODO:lambda test
                    int points = 0;
                    for (Dice dice: dices) {
                        if(dice.color == "GREEN"){
                            points += dice.number;
                        }
                    }
                    return points;
                };
                break;
            case AMETHYST:
                iCalculate = (Dice[] dices) -> {
                    //TODO:lambda test
                    int points = 0;
                    for (Dice dice: dices) {
                        if(dice.color == "VIOLET"){
                            points += dice.number;
                        }
                    }
                    return points;
                };
                break;
            case TURQUOISE:
                iCalculate = (Dice[] dices) -> {
                    //TODO:lambda test
                    int points = 0;
                    for (Dice dice: dices) {
                        if(dice.color == "BLUE"){
                            points += dice.number;
                        }
                    }
                    return points;
                };
                break;
            default:
                //TODO: Error handle
                iCalculate = null;
                break;
        }
        this.personalQCalculator = iCalculate;
    }
    public void SetCommonCalculator(CQ_TYPES qType)
    {
        IQuestCalculate iCalculate;
        switch (qType){
            case RAINBOW_COLUMN:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case DIFF_COLUMN:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case RAINBOW_ROW:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case DIFF_ROW:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case LIGHT_PAIR:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case MIDDLE_PAIR:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case DARK_PAIR:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case ALL_COLOR:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case ALL_NUMBER:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            case SAME_DIAGONAL:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;



                    return 0;
                };
                break;
            default:
                //TODO: Error handle
                iCalculate = null;
                break;
        }
        this.commonQCalculator = iCalculate;
    }

    public int RunEvaluation(Dice[] dices)
    {
        int points = 0;

        if(personalQCalculator != null)
            points += personalQCalculator.Calculate(dices);
        if(commonQCalculator != null)
            points += commonQCalculator.Calculate(dices);

        return points;
    }

}
