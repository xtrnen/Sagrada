package Model.Structs;

public class Dice{
    public int number;
    public int row;
    public int col;
    public String color;

    public Dice(String _color, int _number, int _row, int _col)
    {
        this.number = _number;
        this.color = _color;
        this.row = _row;
        this.col = _col;
    }
}
