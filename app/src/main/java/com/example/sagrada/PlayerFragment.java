package com.example.sagrada;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.sagrada.databinding.PlayerLayoutBinding;

import java.util.ArrayList;

import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import ViewModel.PlayerViewModel;

public class PlayerFragment extends Fragment {
    private PlayerViewModel player;
    private static final String FRAGMENT_POSITION = "Pos";
    private static final String FRAGMENT_NAME = "Name";
    private Integer counter;
    static final int REQUEST_SLOTS = 1;
    static final int REQUEST_DICES = 2;

    public PlayerFragment(){}

    public static PlayerFragment newInstance(Integer position, String username){
        PlayerFragment pf = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_POSITION, position);
        args.putString(FRAGMENT_NAME, username);
        pf.setArguments(args);
        return  pf;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SLOTS){
            assert data != null;
            ArrayList<Slot> slots = data.getParcelableArrayListExtra("Slots");
            assert slots != null;
            for (Slot slot : slots){
                Log.d("SLOT", slot.infoType);
            }
        }
        if(requestCode == REQUEST_DICES){
            assert data != null;
            ArrayList<Dice> dices = data.getParcelableArrayListExtra("Dices");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            counter = getArguments().getInt(FRAGMENT_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PlayerLayoutBinding binding = PlayerLayoutBinding.inflate(inflater, container, false);
        player = new PlayerViewModel(getArguments().getString(FRAGMENT_NAME));
        binding.setPlayer(player);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        ImageButton playerImageInfoButton = view.findViewById(R.id.playerToolbarInfoButton);
        ImageButton playerCameraButton = view.findViewById(R.id.playerToolbarCameraButton);
        Button playerPointsButton = view.findViewById(R.id.playerPointsBtnID);
        playerImageInfoButton.setOnClickListener(v -> Log.println(Log.INFO, "Toolbar", "Player info"));
        playerCameraButton.setOnClickListener(v -> {
            Log.println(Log.INFO, "MenuOption", "Take picture");
            createCaptureModeDialog();
            //Intent cameraIntent = new Intent(getActivity(), CamActivity.class);
            //startActivityForResult(cameraIntent, REQUEST_SLOTS);
        });
        playerPointsButton.setOnClickListener(v -> {
            Log.println(Log.INFO, "PointsButton", "Points clicked");
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /*Capture Mode dialog*/

    private void createCaptureModeDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setMessage("Do you want to capture Pattern card or Dices?");
        dialogBuilder.setNeutralButton("Cancel", (dialog, which) -> {
            //Nothing
        });
        dialogBuilder.setNegativeButton("Pattern", (dialog, which) -> {
           callCameraActivity(REQUEST_SLOTS);
        });
        dialogBuilder.setPositiveButton("Dices", (dialog, which) -> {
           callCameraActivity(REQUEST_DICES);
        });
        dialogBuilder.show();
    }

    private void callCameraActivity(int requestCode){
        Intent cameraIntent = new Intent(getActivity(), CamActivity.class);
        startActivityForResult(cameraIntent, requestCode);
    }
}
