package Model;

public class RuleHandler {
    /*
    * RULES:
    * One dice has to be in corner or edge position
    * Each dice has at least one neighbor in any direction
    * Dice has to fullfill condition of given slot (color / number)
    * Edge of dice cannot has neighbor of the same color / number
    * */
    /*
    * private array of pattern
    * private array of dices*/
    public Boolean EdgeNeighborRule(/*row and col of suggested dice*/)
    {
        return true;
    }
    public Boolean SlotConditionRule(/*row & col*/)
    {
        return true;
    }
    public Boolean IsNeighborRule(/*row & col*/)
    {
        return true;
    }
    public Boolean IsEdgeDiceRule()
    {
        return true;
    }
    private Boolean SlotColorRule()
    {
        return true;
    }
    private Boolean SlotNumberRule()
    {
        return true;
    }
    //private array GetNeighbors()
}
