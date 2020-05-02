package Model.GameBoard.Structs;

import android.os.Parcel;
import android.os.Parcelable;

import Model.Rules.RULE_ERR;

public class Dice implements Parcelable {
    public int number;
    public int row;
    public int col;
    public String color;
    public RULE_ERR errType;

    public static final Parcelable.Creator<Dice> CREATOR = new Parcelable.Creator<Dice>() {

        @Override
        public Dice createFromParcel(Parcel source) {
            return new Dice(source);
        }

        @Override
        public Dice[] newArray(int size) {
            return new Dice[size];
        }
    };

    public Dice(String _color, int _number, int _row, int _col)
    {
        this.number = _number;
        this.color = _color;
        this.row = _row;
        this.col = _col;
        this.errType = RULE_ERR.NO_ERR;
    }
    /*Parceling methods*/
    public Dice(Parcel in){
        this.number = in.readInt();
        this.row = in.readInt();
        this.col = in.readInt();
        this.color = in.readString();
        this.errType = RULE_ERR.values()[in.readInt()];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.number);
        dest.writeInt(this.row);
        dest.writeInt(this.col);
        dest.writeString(this.color);
        dest.writeInt(this.errType.ordinal());
    }

    public void setErrType(RULE_ERR type){
        this.errType = type;
    }
}
