package ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerViewModel extends ViewModel {
    private MutableLiveData<String> name;
    private MutableLiveData<Integer> points;
    //bitmaps

    public PlayerViewModel(String username){
        name = new MutableLiveData<String>();
        name.setValue(username);

        points = new MutableLiveData<Integer>();
        points.setValue(0);
    }

    public String getName(){
        return name.getValue();
    }
    public Integer getPoints(){
        return points.getValue();
    }

    public void setPoints(Integer newPoints){
        points.setValue(newPoints);
    }
    public void setName(String newName){
        name.setValue(newName);
    }
}
