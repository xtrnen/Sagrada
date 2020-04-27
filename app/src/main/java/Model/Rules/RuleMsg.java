package Model.Rules;

public class RuleMsg {
    public int row;
    public int col;
    public RULE_ERR errType;

    RuleMsg(int _row, int _col, RULE_ERR _errType){
        row = _row;
        col = _col;
        errType = _errType;
    }
}
