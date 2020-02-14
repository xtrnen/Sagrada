package Model.Points.Quests;

import java.util.ArrayList;
import java.util.Arrays;

import Model.GameBoard.Structs.Dice;

public class Quest {
    private IQuestCalculate personalQCalculator;
    private IQuestCalculate commonQCalculator;
    public int columns = 5;
    public int rows = 4;

    public void SetPersonalCalculator(PQ_TYPES qType)
    {
        IQuestCalculate iCalculate;
        switch (qType){
            case RUBY:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    for(int row = 0; row < rows; row++){
                        for(int col = 0; col < columns; col++){
                            if(dices[row][col].color.equals("RED")){
                                points += dices[row][col].number;
                            }
                        }
                    }
                    return points;
                };
                break;
            case TOPAZ:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    for(int row = 0; row < rows; row++){
                        for(int col = 0; col < columns; col++){
                            if(dices[row][col].color.equals("YELLOW")){
                                points += dices[row][col].number;
                            }
                        }
                    }
                    return points;
                };
                break;
            case EMERALD:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    for(int row = 0; row < rows; row++){
                        for(int col = 0; col < columns; col++){
                            if(dices[row][col].color.equals("GREEN")){
                                points += dices[row][col].number;
                            }
                        }
                    }
                    return points;
                };
                break;
            case AMETHYST:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    for(int row = 0; row < rows; row++){
                        for(int col = 0; col < columns; col++){
                            if(dices[row][col].color.equals("VIOLET")){
                                points += dices[row][col].number;
                            }
                        }
                    }
                    return points;
                };
                break;
            case TURQUOISE:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    for(int row = 0; row < rows; row++){
                        for(int col = 0; col < columns; col++){
                            if(dices[row][col].color.equals("BLUE")){
                                points += dices[row][col].number;
                            }
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
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    ArrayList<String> colors = new ArrayList<>();

                    for(int col = 0; col < columns; col++){
                        for(int row = 0; row < rows; row++){
                            if(dices[row][col].color.equals("NONE")){
                                break;
                            }
                            if(colors.contains(dices[row][col].color)){
                                break;
                            }
                            colors.add(dices[row][col].color);
                        }
                        if(CompareColors(colors, rows)){
                            points += 5;
                        }
                        colors.clear();
                    }
                    return points;
                };
                break;
            case DIFF_COLUMN:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    ArrayList<Integer> numbers = new ArrayList<>();

                    for(int col = 0; col < columns; col++){
                        for(int row = 0; row < rows; row++){
                            if(dices[row][col].number == 0){
                                break;
                            }
                            if(numbers.contains(dices[row][col].number)){
                                break;
                            }
                            numbers.add(dices[row][col].number);
                        }
                        if(CompareNumbers(numbers, rows)){
                            points += 4;
                        }
                        numbers.clear();
                    }
                    return points;
                };
                break;
            case RAINBOW_ROW:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    ArrayList<String> colors = new ArrayList<>();

                    for(int row = 0; row < rows; row++){
                        for(int col = 0; col < columns; col++){
                            if(dices[row][col].color.equals("NONE")){
                                break;
                            }
                            if(colors.contains(dices[row][col].color)){
                                break;
                            }
                            colors.add(dices[row][col].color);
                        }
                        if(CompareColors(colors, columns)){
                            points += 6;
                        }
                        colors.clear();
                    }
                    return points;
                };
                break;
            case DIFF_ROW:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;
                    ArrayList<Integer> numbers = new ArrayList<>();

                    for(int row = 0; row < rows; row++){
                        for(int col = 0; col < columns; col++){
                            if(dices[row][col].number == 0){
                                break;
                            }
                            if(numbers.contains(dices[row][col].number)){
                                break;
                            }
                            numbers.add(dices[row][col].number);
                        }
                        if(CompareNumbers(numbers, columns)){
                            points += 5;
                        }
                        numbers.clear();
                    }

                    return points;
                };
                break;
            case LIGHT_PAIR:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;

                    int occurence1 = FindNumberOccurence(1, dices);
                    int occurence2 = FindNumberOccurence(2, dices);

                    points += (occurence1 <= occurence2) ? occurence1 * 2 : occurence2 * 2;

                    return points;
                };
                break;
            case MIDDLE_PAIR:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;

                    int occurence1 = FindNumberOccurence(3, dices);
                    int occurence2 = FindNumberOccurence(4, dices);

                    points += (occurence1 <= occurence2) ? occurence1 * 2 : occurence2 * 2;

                    return points;
                };
                break;
            case DARK_PAIR:
                iCalculate = (Dice[][] dices) -> {
                    int points = 0;

                    int occurence1 = FindNumberOccurence(5, dices);
                    int occurence2 = FindNumberOccurence(6, dices);

                    points += (occurence1 <= occurence2) ? occurence1 * 2 : occurence2 * 2;

                    return points;
                };
                break;
            case ALL_COLOR:
                iCalculate = (Dice[][] dices) -> {
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
                iCalculate = (Dice[][] dices) -> {
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
                iCalculate = (Dice[][] dices) -> {
                    //TODO: Rule definition???
                    int points = 0;

                    //for each dice
                    for(int row = 0; row < rows; row++){
                        for(int col = 0; col < columns; col++){
                            //Check if dice with color is first on possible diagonal
                            //Main diagonal
                            int count;
                            if(!CheckAboveDiagonal(dices, row, col, 0)){
                                //diagonal recursion
                                if(row + 1 < rows && col + 1 < columns){
                                    count = MainDiagonalRecursion(dices, row + 1, col + 1, dices[row][col].color) + 1;
                                    points += (count > 1) ? count : 0;
                                }
                            }
                            //Secondary diagonal
                            if(!CheckAboveDiagonal(dices, row, col, 1)){
                                //diagonal recursion
                                if(row + 1 < rows && col - 1 >= 0){
                                    count = MinorDiagonalRecursion(dices, row + 1, col - 1, dices[row][col].color) + 1;
                                    points += (count > 1) ? count : 0;
                                }
                            }
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
        this.commonQCalculator = iCalculate;
    }

    private int FindNumberOccurence(int number, Dice[][] dices)
    {
        int occurence = 0;
        for(int row = 0; row < rows; row++){
            for(int col = 0; col < columns; col++){
                if(dices[row][col].number == number){
                    occurence++;
                }
            }
        }
        return occurence;
    }

    private int FindColorOccurence(String color, Dice[][] dices)
    {
        int occurence = 0;
        for(int row = 0; row < rows; row++){
            for(int col = 0; col < columns; col++){
                if(dices[row][col].color.equals(color.toUpperCase())){
                    occurence++;
                }
            }
        }
        return occurence;
    }

    private boolean CheckAboveDiagonal(Dice[][] dices, int row, int col, int mode)
    {
        //main diagonal
        if(mode == 0){
            if(row > 0 && col > 0){
                return (dices[row][col].color.equals(dices[row - 1][col - 1].color));
            }
            else{
                return false;
            }
        }
        else{
            if(row > 0 && col < columns - 1){
                return (dices[row][col].color.equals(dices[row - 1][col + 1].color));
            }
            else{
                return false;
            }
        }
    }

    private int MainDiagonalRecursion(Dice[][] dices, int row, int col, String color)
    {
        if(dices[row][col].color.equals(color)){
            if(row + 1 < rows && col + 1 < columns){
                return MainDiagonalRecursion(dices, row + 1, col + 1, color) + 1;
            }
            return 1;
        }
        return 0;
    }

    private int MinorDiagonalRecursion(Dice[][] dices, int row, int col, String color)
    {
        if(dices[row][col].color.equals(color)){
            if(row + 1 < rows && col - 1 >= 0)
            {
                return MinorDiagonalRecursion(dices, row + 1, col - 1, color) + 1;
            }
            return 1;
        }
        return 0;
    }

    private boolean CompareColors(ArrayList<String> colors, int prefSize)
    {
        boolean red = false;
        boolean blue = false;
        boolean green = false;
        boolean yellow = false;
        boolean violet = false;
        int count = 0;

        if(colors.size() != prefSize){
            return false;
        }

        for(String str : colors){
            if(str.equals("RED") && !red){
                red = true;
                count++;
            }
            if(str.equals("BLUE") && !blue){
                blue = true;
                count++;
            }
            if(str.equals("GREEN") && !green){
                green = true;
                count++;
            }
            if(str.equals("YELLOW") && !yellow){
                yellow = true;
                count++;
            }
            if(str.equals("VIOLET") && !violet){
                violet = true;
                count++;
            }
        }

        return (count == prefSize);
    }

    private boolean CompareNumbers(ArrayList<Integer> numbers, int prefSize)
    {
        boolean one = false;
        boolean two = false;
        boolean three = false;
        boolean four = false;
        boolean five = false;
        boolean six = false;
        int count = 0;

        if(numbers.size() != prefSize){
            return false;
        }

        for(Integer num : numbers){
            if(num == 1 && !one){
                one = true;
                count++;
            }
            if(num == 2 && !two){
                two = true;
                count++;
            }
            if(num == 3 && !three){
                three = true;
                count++;
            }
            if(num == 4 && !four){
                four = true;
                count++;
            }
            if(num == 5 && !five){
                five = true;
                count++;
            }
            if(num == 6 && !six){
                six = true;
                count++;
            }
        }

        return (count == prefSize);
    }

    public int RunEvaluation(Dice[][] dices)
    {
        int points = 0;

        if(personalQCalculator != null){
            points += personalQCalculator.Calculate(dices);
        }
        if(commonQCalculator != null){
            points += commonQCalculator.Calculate(dices);
        }

        return points;
    }

}
