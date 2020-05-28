package com.example.sagrada;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.sagrada.Components.DiceInfoCell;
import com.example.sagrada.Components.SlotInfoCell;
import com.example.sagrada.Fragments.Dialogs.CraftsmanPointsDialogFragment;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import java.util.ArrayList;
import java.util.Arrays;

import Model.GameBoard.GameBoard;
import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import Model.Rules.RULE_ERR;
import Model.Rules.RuleHandler;

public class InformationActivity extends AppCompatActivity implements CraftsmanPointsDialogFragment.ICraftsmanCards {
    private ArrayList<Slot> slots;
    private ArrayList<Dice> dices;
    private Button confirmBtn;
    private ArrayList<SlotInfoCell> slotBtns = new ArrayList<>(20);
    private ArrayList<DiceInfoCell> diceBtns = new ArrayList<>(20);
    private Drawable defaultBackground;
    private boolean ruleCheck = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout);

        Toolbar toolbar = findViewById(R.id.InfoToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        defaultBackground = findViewById(R.id.diceTextInfoID).getBackground();

        ArrayList<LinearLayout> slotViews = new ArrayList<LinearLayout>(Arrays.asList(findViewById(R.id.slotGrid1), findViewById(R.id.slotGrid2), findViewById(R.id.slotGrid3), findViewById(R.id.slotGrid4)));
        ArrayList<LinearLayout> diceViews = new ArrayList<LinearLayout>(Arrays.asList(findViewById(R.id.diceGrid1), findViewById(R.id.diceGrid2), findViewById(R.id.diceGrid3), findViewById(R.id.diceGrid4)));

        slots = getIntent().getParcelableArrayListExtra(GameActivity.DATA_SLOTS);
        dices = getIntent().getParcelableArrayListExtra(GameActivity.DATA_DICES);

        setSlotBtns(slotViews);
        setDiceBtns(diceViews);

        confirmBtn = findViewById(R.id.confirmInfoBtnID);
        Button rulesBtn = findViewById(R.id.rulesInfoBtnID);

        confirmBtn.setOnClickListener(v -> {
            Intent ret = new Intent();
            ret.putParcelableArrayListExtra(GameActivity.DATA_SLOTS, getSlotsFromCells());
            ret.putParcelableArrayListExtra(GameActivity.DATA_DICES, getDicesFromCells());
            if(ruleCheck){
                setResult(PlayerFragment.REQUEST_INFO_VALID, ret);
            } else {
                setResult(PlayerFragment.REQUEST_INFO_INVALID, ret);
            }
            finish();
        });

        rulesBtn.setOnClickListener(v -> createCraftsmanUseDialog());
    }

    private void createCraftsmanUseDialog(){
        DialogFragment craftsmanPointsDialogFragment = new CraftsmanPointsDialogFragment();
        craftsmanPointsDialogFragment.show(getSupportFragmentManager(), "CraftsmanDialog");
    }
    private void setSlotBtns(ArrayList<LinearLayout> linearViews){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(linearViews.get(0).getLayoutParams());
        params.weight = 1;
        params.gravity = Gravity.CENTER;
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;

        int counter = 0;
        int iter = 0;
        LinearLayout layout = linearViews.get(iter);

        int slotCount = 0;
        Slot slot = (!slots.isEmpty()) ? slots.get(slotCount) : null;
        for(int row = 0; row < 4; row++){
            layout = linearViews.get(row);
            for(int col = 0; col < 5; col++){
                SlotInfoCell infoCell;
                if(slots.isEmpty()){
                    infoCell = new SlotInfoCell(this, new Slot("WHITE", row, col));
                } else if(slot.row == row && slot.col == col && slotCount < slots.size()){
                    infoCell = new SlotInfoCell(this, slot);
                    slotCount++;
                    if(slotCount < slots.size()){
                        slot = slots.get(slotCount);
                    }
                } else {
                    infoCell = new SlotInfoCell(this, new Slot("NONE", row, col));
                }
                infoCell.setLayoutParams(params);
                layout.addView(infoCell);
                slotBtns.add(infoCell);
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
        Dice dice = (!dices.isEmpty()) ? dices.get(diceCount) : null;
        for(int row = 0; row < 4; row++){
            layout = linearViews.get(row);
            for(int col = 0; col < 5; col++){
                DiceInfoCell diceCell;
                if(dices.isEmpty()){
                    diceCell = new DiceInfoCell(this, new Dice("None", 0, row, col));
                } else if(dice.row == row && dice.col == col && diceCount < dices.size()){
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

    @Override
    public void getCraftValues(int sandpaper, int eglomise, boolean sandpaperCheck, boolean eglomiseCheck) {
        RuleHandler ruleHandler = new RuleHandler(GameBoard.adaptArray(getDicesFromCells().toArray(new Dice[0])), GameBoard.adaptArray(getSlotsFromCells().toArray(new Slot[0])));
        int eglomiseValue = 0;
        int sandpaperValue = 0;
        if(eglomiseCheck){
            eglomiseValue = eglomise;
        }
        if(sandpaperCheck){
            sandpaperValue = sandpaper;
        }

        if(!ruleHandler.CheckRules(eglomiseValue, sandpaperValue)){
            //Check edgeRule
            if(!ruleHandler.edgeRule){
                //Set whole card in red border
                //Drawable gradient for setting border
                GradientDrawable grad = new GradientDrawable();
                grad.setStroke(2, Color.RED);
                TextView layout = findViewById(R.id.diceTextInfoID);
                layout.setBackground(grad);
                //Add card tooltip
                ToolTipsManager toolTipsManager = new ToolTipsManager();
                ToolTip.Builder builder = new ToolTip.Builder(this, layout, findViewById(R.id.RelativeLayID), getResources().getStringArray(R.array.errorMessageArray)[RULE_ERR.EDGE_ERR.ordinal() -1], ToolTip.POSITION_BELOW);
                layout.setOnLongClickListener(e -> {
                    toolTipsManager.show(builder.build());
                    return true;
                });
            } else {
                findViewById(R.id.diceTextInfoID).setBackground(defaultBackground);
            }
            //Set dices
            recastDices(ruleHandler.getDices());
            ruleCheck = false;
        } else {
            recastDices(ruleHandler.getDices());
            //set card to default color
            findViewById(R.id.diceTextInfoID).setBackground(defaultBackground);
            ruleCheck = true;
            showSuccessDialog();
        }
    }

    private void recastDices(Dice[][] ruleDices){
        for(int row = 0; row < 4; row++){
            for(int col = 0; col < 5; col++){
                for(DiceInfoCell diceCell : diceBtns){
                    Dice dice = diceCell.getDice();
                    if(ruleDices[row][col].row == dice.row && ruleDices[row][col].col == dice.col){
                        diceCell.setDice(ruleDices[row][col]);
                        diceCell.createTooltip(this, findViewById(R.id.RelativeLayID));
                    }
                }
            }
        }
    }

    private void showSuccessDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layoutView = getLayoutInflater().inflate(R.layout.positive_rule_layout, null);
        builder.setView(layoutView);
        AlertDialog dialog = builder.create();
        dialog.show();

        new CountDownTimer(2000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {}
            @Override
            public void onFinish() {
                dialog.dismiss();
            }
        }.start();
    }
}
