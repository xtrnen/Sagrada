package ViewModel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sagrada.R;

import java.util.ArrayList;
import java.util.Arrays;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;

public class PlayerViewModel extends ViewModel {
    private MutableLiveData<String> name;
    public MutableLiveData<Integer> points;
    private MutableLiveData<Integer> cqIndex;
    private MutableLiveData<Integer> pqIndex;
    private MutableLiveData<ArrayList<Slot>> slots;
    private MutableLiveData<ArrayList<Dice>> dices;
    public MutableLiveData<String> personalQ;
    public MutableLiveData<Integer> craftsmanPoints;
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

        craftsmanPoints = new MutableLiveData<Integer>();
        craftsmanPoints.setValue(0);

        context = _context;
    }

    public String getName(){
        return name.getValue();
    }
    public Integer getPoints(){
        return points.getValue();
    }
    public MutableLiveData<ArrayList<Slot>> getSlots(){
        if(slots.getValue() == null){
            slots.setValue(new ArrayList<Slot>());
        }
        return slots;
    }
    public MutableLiveData<ArrayList<Dice>> getDices(){
        if(dices.getValue() == null){
            dices.setValue(new ArrayList<Dice>());
        }
        return dices;
    }
    public MutableLiveData<Integer> getPQIndex(){ return pqIndex; }

    public void setPoints(int newPoints){ points.setValue(newPoints); }
    public void setName(String newName){ name.setValue(newName); }
    public void setSlots(ArrayList<Slot> newSlots){ slots.setValue(newSlots); }
    public void setDices(ArrayList<Dice> newDices){ dices.setValue(newDices); }
    public void setPqIndex(int index){ pqIndex.setValue(index); setPersonalQ();}
    private void setPersonalQ(){
        if(pqIndex.getValue() == -42) {
            return;
        }
        personalQ.setValue(Arrays.asList(context.getResources().getStringArray(R.array.personalQuestStrings)).get(pqIndex.getValue()));
    }
    public boolean isPlayerSet(){ return isDiceSet() && isSlotSet() && isCraftsmanSet() && areCardsSet(); }
    public void setCraftsmanPoints(int points){
        craftsmanPoints.setValue(points);
    }
    public void addCraftsmanPoint(){
        int points = craftsmanPoints.getValue();
        craftsmanPoints.setValue(++points);
    }
    public void subCraftsmanPoint(){
        int points = craftsmanPoints.getValue();
        if(points == 0){
            craftsmanPoints.setValue(0);
        } else{
            craftsmanPoints.setValue(--points);
        }
    }
    public MutableLiveData<Integer> getCraftsman(){
        if(craftsmanPoints.getValue() == null){
            craftsmanPoints.setValue(0);
        }
        return craftsmanPoints;
    }

    /*CHECK PLAYER STATS*/
    private boolean isDiceSet(){ return dices.getValue() != null && dices.getValue().size() > 0; }
    private boolean isSlotSet(){ return slots.getValue() != null && slots.getValue().size() > 0; }
    private boolean isCraftsmanSet(){ return craftsmanPoints.getValue() != null && craftsmanPoints.getValue() >= 0; }
    private boolean areCardsSet(){ return (cqIndex.getValue() != null && pqIndex.getValue() != null); }
}
