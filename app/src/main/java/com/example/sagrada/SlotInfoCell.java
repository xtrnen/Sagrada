package com.example.sagrada;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.PopupMenu;

import java.util.Arrays;
import java.util.List;

import Model.GameBoard.Structs.Slot;
import Model.GameBoard.Structs.SlotInfo;

public class SlotInfoCell extends androidx.appcompat.widget.AppCompatButton {
    private Slot slot;

    public SlotInfoCell(Context context, Slot _slot) {
        super(context);
        slot = _slot;

        this.setTextSize(20);
        setContent();
        addBorder();

        setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(context, v);
            menu.getMenuInflater().inflate(R.menu.slot_menu_layout, menu.getMenu());
            menu.getMenu().clear();

            int order = 0;

            order = addItemsToMenu(menu.getMenu(), Arrays.asList(getResources().getStringArray(R.array.numberMenuItems)), order);
            addItemsToMenu(menu.getMenu(), Arrays.asList(getResources().getStringArray(R.array.colorMenuItems)), order);

            menu.setOnMenuItemClickListener(item -> {
                slot.info = SlotInfo.values()[item.getItemId() + 1];
                slot.infoType = slot.SetInfoType(slot.info.toString());
                setContent();
                addBorder();
                return true;
            });
            menu.show();
        });
    }

    public Slot getSlot(){
        return slot;
    }

    private int addItemsToMenu(Menu menu, List<String> titles, int orderBegin){
        int outOrder = orderBegin;
        for (int order = 0; order < titles.size(); order++){
            menu.add(0, order + orderBegin, 0, titles.get(order));
            outOrder = order;
        }
        outOrder += orderBegin + 1;
        return outOrder;
    }

    private void setContent(){
        switch (slot.info){
            case ONE:
            case TWO:
            case THREE:
            case FOUR:
            case FIVE:
            case SIX:
                setNumberContent();
                return;
            default:
                setColorContent();
        }
    }
    private void setColorContent(){
        this.setText("");
        this.setTextColor(Color.BLACK);
    }
    private void setNumberContent(){
        this.setText(Integer.toString(slot.info.StrToNum()));
    }
    private int chooseColor(SlotInfo info){
        switch (info){
            case RED:
                return Color.RED;
            case GREEN:
                return Color.GREEN;
            case BLUE:
                return Color.BLUE;
            case YELLOW:
                return Color.YELLOW;
            case VIOLET:
                return Color.rgb(148, 0, 211);
            case WHITE:
                return Color.WHITE;
            default:
                return Color.LTGRAY;
        }
    }
    private void addBorder(){
        GradientDrawable grad = new GradientDrawable();
        grad.setColor(this.chooseColor(slot.info));
        grad.setStroke(2, Color.BLACK);
        this.setBackground(grad);
    }
}
