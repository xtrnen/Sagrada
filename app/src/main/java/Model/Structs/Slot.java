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

    Slot(String _info, int _row, int _col)
    {
        this.row = _row;
        this.col = _col;
        this.info = SlotInfo.valueOf(_info);
    }
}
