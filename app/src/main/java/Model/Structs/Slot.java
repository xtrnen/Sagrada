package Model.Structs;

enum SlotInfo {
    NONE,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    RED,
    GREEN,
    BLUE,
    VIOLET,
    YELLOW,
    WHITE
}

public class Slot {
    public int row;
    public int col;
    public SlotInfo info;

    Slot(int _row, int _col, SlotInfo _info)
    {
        this.row = _row;
        this.col = _col;
        this.info = _info;
    }
}
