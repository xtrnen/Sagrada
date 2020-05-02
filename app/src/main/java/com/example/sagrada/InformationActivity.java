package com.example.sagrada;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Arrays;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;

public class InformationActivity extends AppCompatActivity {

    private ArrayList<Slot> slots;
    private ArrayList<Dice> dices;
    private ArrayList<SlotInfoCell> slotBtns = new ArrayList<>(20);
    private ArrayList<DiceInfoCell> diceBtns = new ArrayList<>(20);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout);

        Toolbar toolbar = findViewById(R.id.InfoToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ArrayList<LinearLayout> slotViews = new ArrayList<LinearLayout>(Arrays.asList(findViewById(R.id.slotGrid1), findViewById(R.id.slotGrid2), findViewById(R.id.slotGrid3), findViewById(R.id.slotGrid4)));
        ArrayList<LinearLayout> diceViews = new ArrayList<LinearLayout>(Arrays.asList(findViewById(R.id.diceGrid1), findViewById(R.id.diceGrid2), findViewById(R.id.diceGrid3), findViewById(R.id.diceGrid4)));

        slots = getIntent().getParcelableArrayListExtra(GameActivity.DATA_SLOTS);
        dices = getIntent().getParcelableArrayListExtra(GameActivity.DATA_DICES);

        setSlotBtns(slotViews);
        setDiceBtns(diceViews);

        Button confirmBtn = findViewById(R.id.confirmInfoBtnID);

        confirmBtn.setOnClickListener(v -> {
            Intent ret = new Intent();
            ret.putParcelableArrayListExtra(GameActivity.DATA_SLOTS, getSlotsFromCells());
            ret.putParcelableArrayListExtra(GameActivity.DATA_DICES, getDicesFromCells());
            setResult(PlayerFragment.REQUEST_INFO, ret);
            finish();
        });
    }

    private void setSlotBtns(ArrayList<LinearLayout> linearViews){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(linearViews.get(0).getLayoutParams());
        params.weight = 1;
        params.gravity = Gravity.CENTER;
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;

        int counter = 0;
        int iter = 0;
        LinearLayout layout = linearViews.get(iter);

        for(Slot slot : slots){
            SlotInfoCell btnView = new SlotInfoCell(this, slot);
            btnView.setLayoutParams(params);
            slotBtns.add(btnView);
            layout.addView(btnView);
            if(counter == 4){
                counter = 0;
                iter++;
                if(iter < linearViews.size()){
                    layout = linearViews.get(iter);
                }
            } else {
                counter++;
            }
        }
    }
    private void setDiceBtns(ArrayList<LinearLayout> linearViews){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(linearViews.get(0).getLayoutParams());
        params.weight = 1;
        params.gravity = Gravity.FILL;
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;

        LinearLayout layout;
        int diceCount = 0;
        Dice dice = dices.get(diceCount);
        for(int row = 0; row < 4; row++){
            layout = linearViews.get(row);
            for(int col = 0; col < 5; col++){
                DiceInfoCell diceCell;
                if(dice.row == row && dice.col == col && diceCount < dices.size()){
                    diceCell = new DiceInfoCell(this, dice);
                    diceCount++;
                    if(diceCount < dices.size()){
                        dice = dices.get(diceCount);
                    }
                } else {
                    diceCell = new DiceInfoCell(this, new Dice("None", 0, row, col));
                }
                diceCell.setLayoutParams(params);
                layout.addView(diceCell);
                diceCell.createTooltip(this, findViewById(R.id.RelativeLayID));
                diceBtns.add(diceCell);
            }
        }
    }
    private ArrayList<Slot> getSlotsFromCells(){
        ArrayList<Slot> retArray = new ArrayList<>();
        for(SlotInfoCell cell : slotBtns){
            retArray.add(cell.getSlot());
        }
        return retArray;
    }
    private ArrayList<Dice> getDicesFromCells(){
        ArrayList<Dice> retArray = new ArrayList<>();
        for (DiceInfoCell cell : diceBtns){
            if(cell.getDice().number != 0){
                retArray.add(cell.getDice());
            }
        }
        return retArray;
    }
}
