package Model.GameBoard.Structs;

public enum SlotInfo {
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
    WHITE;

    public int StrToNum(){
        switch (this){
            case ONE:
                return 1;
            case TWO:
                return 2;
            case THREE:
                return 3;
            case FOUR:
                return 4;
            case FIVE:
                return 5;
            case SIX:
                return 6;
            default:
                return 0;
        }
    }
}
