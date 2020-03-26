package ViewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import Model.GameBoard.Player;

public class GameViewModel extends ViewModel {
    private MutableLiveData<List<Player>> players;

    public GameViewModel(){
        players = new MutableLiveData<List<Player>>();
    }

    public MutableLiveData<List<Player>> getPlayers(){
        if(players == null){
            players = new MutableLiveData<List<Player>>();
        }
        return players;
    }

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

}
