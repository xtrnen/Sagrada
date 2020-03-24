package Model.GameBoard;

public class Player {
    public String name;
    public int points;
    private GameBoard gameBoard;

    public Player(String _name){
        name = _name;
        points = 0;
    }

    public void ChangeName(String newName){
        name = newName;
    }

    public void ChangePoints(int newPoints){
        points = newPoints;
    }

}
