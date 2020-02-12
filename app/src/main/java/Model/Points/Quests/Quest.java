package Model.Points.Quests;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import Model.Structs.Dice;

public class Quest {
    IQuestCalculate personalQCalculator;
    IQuestCalculate commonQCalculator;
    public int columns = 5;
    public int rows = 4;

    private String[] colorsArray = {"RED", "GREEN", "BLUE", "YELLOW", "VIOLET"};

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
                    String[] colors = {};

                    for(int col = 0; col < columns; col++){
                        for(int row = 0; row < rows; row++){

                        }
                    }
                    //for each column
                        //for each row
                            //take dice[row][col]
                            //if not exist -> break
                            //Check if dice color is not already in array
                            //if not add the color to array
                            //else break
                        //compare array and refArray -> points += 5;
                        //reset bools
                    return points;
                };
                break;
            case DIFF_COLUMN:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;
                    int[] numbers;
                    //for each column
                        //for each row
                            //take dice[row][col]
                            //if not exist -> break
                            //check if dice number is not already in array
                            //if not add number to array
                            //else break
                        //if array size == MAX_ROW_SIZE -> points += 4
                        //reset array
                    return points;
                };
                break;
            case RAINBOW_ROW:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;
                    String[] colors = {};
                    //for each row
                        //for each column
                            //take dice
                            //if not exist -> break
                            //check color in not in array else break
                            //add to array
                        //compare array and ref array -> points += 6;
                        //reset array

                    return points;
                };
                break;
            case DIFF_ROW:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;
                    int[] numbers;
                    //for each row
                        //for each col
                            //take dice[row][col]
                            //if not exist -> break
                            //check if dice number is not already in array
                            //if not add number to array
                            //else break
                        //if array size == MAX_ROW_SIZE -> points += 5
                        //reset array
                    return points;
                };
                break;
            case LIGHT_PAIR:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Test
                    int points = 0;

                    int occurence1 = FindNumberOccurence(1, dices);
                    int occurence2 = FindNumberOccurence(2, dices);

                    points += (occurence1 <= occurence2) ? occurence1 * 2 : occurence2 * 2;

                    return points;
                };
                break;
            case MIDDLE_PAIR:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Test
                    int points = 0;

                    int occurence1 = FindNumberOccurence(3, dices);
                    int occurence2 = FindNumberOccurence(4, dices);

                    points += (occurence1 <= occurence2) ? occurence1 * 2 : occurence2 * 2;

                    return points;
                };
                break;
            case DARK_PAIR:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Test
                    int points = 0;

                    int occurence1 = FindNumberOccurence(5, dices);
                    int occurence2 = FindNumberOccurence(6, dices);

                    points += (occurence1 <= occurence2) ? occurence1 * 2 : occurence2 * 2;

                    return points;
                };
                break;
            case ALL_COLOR:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Test
                    int points = 0;
                    int[] colors = new int[5];

                    colors[0] = FindColorOccurence("RED", dices);
                    colors[1] = FindColorOccurence("GREEN", dices);
                    colors[2] = FindColorOccurence("BLUE", dices);
                    colors[3] = FindColorOccurence("YELLOW", dices);
                    colors[4] = FindColorOccurence("VIOLET", dices);

                    if(colors[0] == 0 || colors[1] == 0 || colors[2] == 0 || colors[3] == 0 || colors[4] ==0)
                        return points;

                    Arrays.sort(colors);

                    points += colors[0] * 4;

                    return points;
                };
                break;
            case ALL_NUMBER:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Test
                    int points = 0;
                    int[] number = new int[6];

                    number[0] = FindNumberOccurence(1, dices);
                    number[1] = FindNumberOccurence(2, dices);
                    number[2] = FindNumberOccurence(3, dices);
                    number[3] = FindNumberOccurence(4, dices);
                    number[4] = FindNumberOccurence(5, dices);
                    number[5] = FindNumberOccurence(6, dices);

                    if(number[0] == 0 || number[1] == 0 || number[2] == 0 || number[3] == 0 || number[4] == 0 || number[5] == 0)
                        return points;

                    Arrays.sort(number);

                    points += number[0] * 5;

                    return points;
                };
                break;
            case SAME_DIAGONAL:
                iCalculate = (Dice[] dices) -> {
                    //TODO: Implement
                    int points = 0;

                    //for each dice


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

    private int FindNumberOccurence(int number, Dice[] dices)
    {
        int occurence = 0;
        for(Dice dice : dices){
            if(dice.number == number)
                occurence++;
        }
        return occurence;
    }

    private int FindColorOccurence(String color, Dice[] dices)
    {
        int occurence = 0;
        for(Dice dice : dices){
            if(dice.color.equals(color.toUpperCase()))
                occurence++;
        }
        return occurence;
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
