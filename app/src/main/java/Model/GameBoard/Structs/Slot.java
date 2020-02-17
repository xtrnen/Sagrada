package Model.GameBoard.Structs;

public class Slot {
    public int row;
    public int col;
    public SlotInfo info;
    public String infoType;

    public Slot(String _info, int _row, int _col)
    {
        this.row = _row;
        this.col = _col;
        this.info = SlotInfo.valueOf(_info);
        this.infoType = SetInfoType(_info);
    }

    private String SetInfoType(String _info){
        switch (_info){
            case "NONE":
            case "WHITE":
                return "NONE";
            case "ONE":
            case "TWO":
            case "THREE":
            case "FOUR":
            case "FIVE":
            case "SIX":
                return "NUMBER";
            case "RED":
            case "BLUE":
            case "GREEN":
            case "YELLOW":
            case "VIOLET":
                return "COLOR";
            default:
                //TODO: Handle Error maybe with NONE value
                return "ERROR";
        }
    }
}
