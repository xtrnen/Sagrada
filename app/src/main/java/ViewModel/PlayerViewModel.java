package ViewModel;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerViewModel extends ViewModel {
    private MutableLiveData<String> name;
    private MutableLiveData<Integer> points;
    private MutableLiveData<Bitmap> keyBitmap;
    private MutableLiveData<Bitmap> diceBitmap;

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
    public String getPointsString() { return getPoints().toString(); }
    public Bitmap getKeyBitmap() { return (keyBitmap != null) ? keyBitmap.getValue() : null; }
    public Bitmap getDiceBitmap() { return (diceBitmap != null) ? diceBitmap.getValue() : null; }

    public void setPoints(Integer newPoints){ points.setValue(newPoints); }
    public void setName(String newName){ name.setValue(newName); }
    public void setKeyBitmap(Bitmap newKeyBitmap){ keyBitmap.setValue(newKeyBitmap); }
    public void setDiceBitmap(Bitmap newDiceBitmap){ diceBitmap.setValue(newDiceBitmap); }
}
