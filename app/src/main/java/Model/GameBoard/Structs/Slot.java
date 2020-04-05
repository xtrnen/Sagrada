package Model.GameBoard.Structs;

import android.os.Parcel;
import android.os.Parcelable;

public class Slot implements Parcelable {
    public int row;
    public int col;
    public SlotInfo info;
    public String infoType;

    public static final Parcelable.Creator<Slot> CREATOR = new Parcelable.Creator<Slot>() {

        @Override
        public Slot createFromParcel(Parcel source) {
            return new Slot(source);
        }

        @Override
        public Slot[] newArray(int size) {
            return new Slot[size];
        }
    };
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

    public Slot(Parcel in){
        this.row = in.readInt();
        this.col = in.readInt();
        this.info = SlotInfo.values()[in.readInt()];
        this.infoType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.row);
        dest.writeInt(this.col);
        dest.writeInt(this.info.ordinal());
        dest.writeString(this.infoType);
    }
}
