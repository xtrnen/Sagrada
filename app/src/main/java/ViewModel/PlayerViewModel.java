package ViewModel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sagrada.R;

import java.util.ArrayList;
import java.util.Arrays;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import Model.Points.Quests.PQ_TYPES;

public class PlayerViewModel extends ViewModel {
    private MutableLiveData<String> name;
    private MutableLiveData<Integer> points;
    private MutableLiveData<Integer> cqIndex;
    private MutableLiveData<Integer> pqIndex;
    private MutableLiveData<ArrayList<Slot>> slots;
    private MutableLiveData<ArrayList<Dice>> dices;
    public MutableLiveData<String> personalQ;
    private Context context;

    public PlayerViewModel(String username, Context _context){
        name = new MutableLiveData<String>();
        name.setValue(username);

        points = new MutableLiveData<Integer>();
        points.setValue(0);

        slots = new MutableLiveData<ArrayList<Slot>>();

        dices = new MutableLiveData<ArrayList<Dice>>();

        cqIndex = new MutableLiveData<Integer>();
        pqIndex = new MutableLiveData<Integer>();
        cqIndex.setValue(-42);
        pqIndex.setValue(-42);

        personalQ = new MutableLiveData<String>();
        personalQ.setValue("Nevybráno");

        context = _context;
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
    public MutableLiveData<Integer> getPQIndex(){ return pqIndex; }

    public void setPoints(Integer newPoints){ points.setValue(newPoints); }
    public void setName(String newName){ name.setValue(newName); }
    public void setSlots(ArrayList<Slot> newSlots){ slots.setValue(newSlots); }
    public void setDices(ArrayList<Dice> newDices){ dices.setValue(newDices); }
    public void setPqIndex(int index){ pqIndex.setValue(index); setPersonalQ();}
    private void setPersonalQ(){
        if(pqIndex.getValue() == -42) {
            return;
        }
        personalQ.setValue(Arrays.asList(context.getResources().getStringArray(R.array.personalQuestStrings)).get(pqIndex.getValue())); }
}
