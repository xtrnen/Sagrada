package ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;

public class PlayerViewModel extends ViewModel {
    private MutableLiveData<String> name;
    private MutableLiveData<Integer> points;
    private MutableLiveData<ArrayList<Slot>> slots;
    private MutableLiveData<ArrayList<Dice>> dices;

    public PlayerViewModel(String username){
        name = new MutableLiveData<String>();
        name.setValue(username);

        points = new MutableLiveData<Integer>();
        points.setValue(0);

        slots = new MutableLiveData<ArrayList<Slot>>();

        dices = new MutableLiveData<ArrayList<Dice>>();
    }

    public String getName(){
        return name.getValue();
    }
    public Integer getPoints(){
        return points.getValue();
    }
    public String getPointsString() { return "Points:" + getPoints().toString(); }
    public MutableLiveData<ArrayList<Slot>> getSlots(){ return slots; }
    public MutableLiveData<ArrayList<Dice>> getDices(){ return dices; }
    public int getSlotArraySize(){ return getSlots().getValue().size(); }
    public int getDiceArraySize(){ return getDices().getValue().size(); }

    public void setPoints(Integer newPoints){ points.setValue(newPoints); }
    public void setName(String newName){ name.setValue(newName); }
    public void setSlots(ArrayList<Slot> newSlots){ slots.setValue(newSlots); }
    public void setDices(ArrayList<Dice> newDices){ dices.setValue(newDices); }
}
