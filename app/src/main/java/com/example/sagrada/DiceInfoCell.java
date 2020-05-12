package com.example.sagrada;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;
import java.util.Arrays;
import java.util.List;
import Model.GameBoard.Structs.Dice;
import Model.Rules.RULE_ERR;

public class DiceInfoCell extends androidx.appcompat.widget.AppCompatButton {
    private Dice dice;
    private PopupMenu btnMenu;

    public DiceInfoCell(Context context, Dice _dice) {
        super(context);
        dice = _dice;

        setBtnDesign();

        setOnClickListener(v -> {
            btnMenu = new PopupMenu(context, v);
            btnMenu.getMenuInflater().inflate(R.menu.slot_menu_layout, btnMenu.getMenu());
            btnMenu.getMenu().clear();

            addItemMenuToMenu(btnMenu.getMenu(), Arrays.asList(getResources().getStringArray(R.array.colorMenuItems)));

            btnMenu.setOnMenuItemClickListener(item -> {
                dice.color = setColorInfo(item.getItemId());
                if(item.getItemId() == Arrays.asList(getResources().getStringArray(R.array.colorMenuItems)).size() - 1){
                    dice.number = 0;
                    setBtnDesign();
                }
                return true;
            });
            btnMenu.show();
        });
    }

    public Dice getDice(){
        return dice;
    }
    public void setDice(Dice newDice) {
        dice = newDice;
        setBtnDesign();
    }

    private void addItemMenuToMenu(Menu menu, List<String> titles){
        for (int order = 0; order < titles.size(); order++){
            if(order == titles.size() - 1){
                menu.add(0, order, 0, getResources().getString(R.string.menuItemNone));
            } else {
                SubMenu subMenu = menu.addSubMenu(0, order, 0, titles.get(order));
                addItemsToMenu(subMenu, Arrays.asList(getResources().getStringArray(R.array.numberMenuItems)));
            }
        }
    }

    private void addItemsToMenu(Menu menu, List<String> titles){
        for (int order = 0; order < titles.size(); order++){
            MenuItem item = menu.add(0, order, 0, titles.get(order));
            item.setOnMenuItemClickListener(ac -> {
                dice.number = Integer.parseInt(item.getTitle().toString());
                dice.errType = RULE_ERR.NO_ERR;
                setBtnDesign();
                return true;
            });
        }
    }

    private void setBtnDesign(){
        if(dice.number == 0){
            this.setText("");
        } else {
            this.setText(Integer.toString(dice.number));
        }
        this.setTextSize(26);
        setBtnResource();
    }

    private int chooseColor(String color){
        switch (color){
            case "RED":
                return getResources().getColor(R.color.redDice, getContext().getTheme());
            case "GREEN":
                return getResources().getColor(R.color.greenDice, getContext().getTheme());
            case "BLUE":
                return getResources().getColor(R.color.blueDice, getContext().getTheme());
            case "YELLOW":
                return getResources().getColor(R.color.yellowDice, getContext().getTheme());
            case "VIOLET":
                return getResources().getColor(R.color.violetDice, getContext().getTheme());
            default:
                return getResources().getColor(R.color.whiteDice, getContext().getTheme());
        }
    }
    private String setColorInfo(int colorPosition){
        switch (colorPosition){
            case 0:
                return "RED";
            case 1:
                return "GREEN";
            case 2:
                return "BLUE";
            case 3:
                return "VIOLET";
            case 4:
                return "YELLOW";
            default:
                return "NONE";
        }
    }
    private void setBtnResource(){
        LayerDrawable diceDrawable;
        if(dice.errType != RULE_ERR.NO_ERR){
            diceDrawable = (LayerDrawable)getResources().getDrawable(R.drawable.dice_invalid, getContext().getTheme()).mutate();
        } else {
            diceDrawable = (LayerDrawable)getResources().getDrawable(R.drawable.dice_valid, getContext().getTheme()).mutate();
        }
        diceDrawable.findDrawableByLayerId(R.id.diceDrawableColorID).setColorFilter(chooseColor(dice.color), PorterDuff.Mode.SRC);
        this.setBackground(diceDrawable);
    }

    public void createTooltip(Context context, RelativeLayout layout){
        if(dice.errType != RULE_ERR.NO_ERR){
            ToolTipsManager toolTipsManager = new ToolTipsManager();
            ToolTip.Builder builder = new ToolTip.Builder(context, this, layout, getErrResource(), setTipPosition());
            this.setOnLongClickListener(e -> {
                if(dice.errType != RULE_ERR.NO_ERR){
                    toolTipsManager.show(builder.build());
                }
                return true;
            });
        }
    }
    private String getErrResource(){
        if(dice.errType != RULE_ERR.NO_ERR){
            return getResources().getStringArray(R.array.errorMessageArray)[dice.errType.ordinal() -1];
        } else {
            return "";
        }
    }

    private int setTipPosition(){
        switch (dice.row){
            case 0:
            case 1:
                switch (dice.col){
                    case 0:
                    case 1:
                        return ToolTip.POSITION_RIGHT_TO;
                    case 2:
                        return ToolTip.POSITION_BELOW;
                    default:
                        return ToolTip.POSITION_LEFT_TO;
                }
            case 2:
            case 3:
            default:
                switch (dice.col){
                    case 0:
                    case 1:
                        return ToolTip.POSITION_RIGHT_TO;
                    case 2:
                        return ToolTip.POSITION_ABOVE;
                    default:
                        return ToolTip.POSITION_LEFT_TO;
                }
        }
    }
}
