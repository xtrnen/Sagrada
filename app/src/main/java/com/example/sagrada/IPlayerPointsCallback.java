package com.example.sagrada;

import java.util.ArrayList;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;

public interface IPlayerPointsCallback {
    int callbackPoints(ArrayList<Slot> slots, ArrayList<Dice> dices);
}
