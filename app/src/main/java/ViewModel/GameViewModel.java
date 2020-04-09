package ViewModel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sagrada.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Model.GameBoard.Player;

public class GameViewModel extends ViewModel {
    private MutableLiveData<List<Player>> players;
    private MutableLiveData<Integer> commonQuest;
    private Context context;
    public MutableLiveData<String> cqString;

    public GameViewModel(){
        players = new MutableLiveData<List<Player>>();
        commonQuest = new MutableLiveData<Integer>();
        cqString = new MutableLiveData<String>();
    }

    public MutableLiveData<List<Player>> getPlayers(){
        if(players == null){
            players = new MutableLiveData<List<Player>>();
        }
        return players;
    }
    public MutableLiveData<Integer> getCommonQuest(){
        if(commonQuest == null){
            commonQuest = new MutableLiveData<Integer>();
        }
        return commonQuest;
    }
    public int getPlayersCount(){ return players.getValue().size(); }

    public void addPlayer(Player player){
        List<Player> currentPlayers = players.getValue();

        ArrayList<Player> newPlayers;
        if(currentPlayers == null){
            newPlayers = new ArrayList<>();
        } else {
            newPlayers = new ArrayList<>(currentPlayers.size());
            newPlayers.addAll(currentPlayers);
        }
        newPlayers.add(player);
        players.setValue(newPlayers);
    }
    public void addCommonQuest(int commonQuestIndex){
        if(commonQuest == null){
            commonQuest = new MutableLiveData<Integer>();
        }
        commonQuest.setValue(commonQuestIndex);
        cqString.setValue(getCQString());
    }
    public void addContext(Context _context){ context = _context; }

    public void removePlayer(String name){
        List<Player> currentPlayers = players.getValue();

        ArrayList<Player> newPlayerList = new ArrayList<>(currentPlayers.size());
        newPlayerList.addAll(currentPlayers);

        int index = -1;
        for(int i = 0; i < newPlayerList.size(); i++){
            Player player = newPlayerList.get(i);
            if(player.name.equals(name.toLowerCase())){
                index = i;
            }
        }
        if(index != -1){
            newPlayerList.remove(index);
        }
        players.setValue(newPlayerList);
    }

    private String getCQString(){
        if(commonQuest == null){
            return "Nevybráno";
        }
        return Arrays.asList(context.getResources().getStringArray(R.array.groupQuestStrings)).get(commonQuest.getValue());
    }

    public void output(){
        if(players.getValue() != null){
            for(Player player : players.getValue()){
                Log.println(Log.INFO, "HOSE", Integer.toString(player.points));
            }
        }
    }
}
