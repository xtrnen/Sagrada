package Model.Rules;

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
    /*
    * POINTS:
    * Group assigns:
    *   each set of dices that fit the assign counts
    * Personal assign:
    *   each card has specified counting
    * CraftsmanPoints stones:
    *   each not used stone counts for 1P
    * Empty slots:
    *   for each empty slot lose 1P
    * */
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
    public int CalculatePoints()
    {
        int points = 0;
        return points;
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
    //SET of functions for each group/personal goal card
    //Functions for point counting
}
