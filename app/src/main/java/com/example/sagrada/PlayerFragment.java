package com.example.sagrada;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.sagrada.databinding.PlayerLayoutBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Model.GameBoard.Player;
import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import ViewModel.GameViewModel;
import ViewModel.PlayerViewModel;

public class PlayerFragment extends Fragment implements IPlayerPointsCallback{
    private PlayerViewModel player;
    private static final String FRAGMENT_POSITION = "Pos";
    private static final String FRAGMENT_NAME = "Name";
    private static final String FRAGMENT_PQ = "PQ";
    private Integer counter;
    private Button playerPointsButton;
    IPlayerPointsCallback pointsCallback;

    public PlayerFragment(){}

    public static PlayerFragment newInstance(Integer position, String username, int pqIndex){
        PlayerFragment pf = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_POSITION, position);
        args.putString(FRAGMENT_NAME, username);
        args.putInt(FRAGMENT_PQ, pqIndex);
        pf.setArguments(args);
        return  pf;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            pointsCallback = (IPlayerPointsCallback) context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "No IPlayerPointsCallback implementation");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GameActivity.REQUEST_SLOTS){
            assert data != null;
            ArrayList<Slot> slots = data.getParcelableArrayListExtra("slots");
            assert slots != null;
            for (Slot slot : slots){
                Log.d("SLOT", slot.infoType);
            }
        }
        if(requestCode == GameActivity.REQUEST_DICES){
            assert data != null;
            ArrayList<Dice> dices = data.getParcelableArrayListExtra("dices");
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
        player = new PlayerViewModel(getArguments().getString(FRAGMENT_NAME), getContext());
        GameActivity gameActivity = (GameActivity) getActivity();
        player.setPqIndex(getArguments().getInt(FRAGMENT_PQ));

        binding.setPlayer(player);
        binding.setGame(gameActivity.gameViewModel);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        ImageButton playerImageInfoButton = view.findViewById(R.id.playerToolbarInfoButton);
        ImageButton playerCameraButton = view.findViewById(R.id.playerToolbarCameraButton);
        ImageButton playerPersonalQuestButton = view.findViewById(R.id.personalQuestBtnID);
        ImageButton playerCraftsmanButton = view.findViewById(R.id.craftsmanBtnID);
        playerPointsButton = view.findViewById(R.id.playerPointsBtnID);
        playerImageInfoButton.setOnClickListener(v -> Log.println(Log.INFO, "Toolbar", "Player info"));
        playerCameraButton.setOnClickListener(v -> createCaptureModeDialog());
        playerPointsButton.setOnClickListener(v -> {
            if(player.isPlayerSet()){
                //pointsCallback.callbackPoints(player.getSlots().getValue(), player.getDices().getValue());
                if(isOnlyPlayer()){
                    //TODO: Calculate Points
                } else {
                    createCalculationDialog();
                }
            }
        });
        playerPersonalQuestButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.quest_personal_layout, popupMenu.getMenu());
            popupMenu.getMenu().clear();
            addItemsToMenu(popupMenu.getMenu(), Arrays.asList(getResources().getStringArray(R.array.personalQuestStrings)));
            popupMenu.setOnMenuItemClickListener(item -> {
                player.setPqIndex(item.getItemId());
                return true;
            });
            popupMenu.show();
        });
        playerCraftsmanButton.setOnClickListener( v -> createCraftsmanPointsDialog());
        setVariableObservers();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /*Dialogs*/
    private void createCaptureModeDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setMessage("Do you want to capture Pattern card or Dices?");
        dialogBuilder.setNeutralButton("Cancel", (dialog, which) -> {
            //Nothing
        });
        dialogBuilder.setNegativeButton("Pattern", (dialog, which) -> {
           callCameraActivity(GameActivity.REQUEST_SLOTS);
        });
        dialogBuilder.setPositiveButton("Dices", (dialog, which) -> {
           callCameraActivity(GameActivity.REQUEST_DICES);
        });
        dialogBuilder.show();
    }
    private void createCraftsmanPointsDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.craftsman_point_layout, null);

        dialog.setView(view);
        dialog.setTitle("Konečný počet bodů řemeslníků");

        EditText editText = view.findViewById(R.id.craftsmanEditTextID);

        dialog.setNeutralButton("Zrušit", (dialogV, which) -> {});
        dialog.setPositiveButton("Potvrdit", (dialogV, which) -> {
            ArrayList<Slot> slots = new ArrayList<Slot>();
            ArrayList<Dice> dices = new ArrayList<Dice>();
            slots.add(new Slot("RED", 1,1));
            dices.add(new Dice("RED", 5, 1,1));
            player.setSlots(slots);
            player.setDices(dices);
            player.setCraftsmanPoints(Integer.parseInt(editText.getText().toString()));
        });
        dialog.show();
    }
    private void createCalculationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Vyhodnocení hry");
        builder.setMessage("Chcete vyhodnotit body pro všechny hráče, nebo pouze aktuálního hráče?");
        builder.setNeutralButton("Zrušit", (dialog, which) -> {});
        builder.setPositiveButton("Aktuálního hráče", (dialog, which) -> {
            //TODO: Calculate Points
        });
        builder.setNegativeButton("Všechny hráče", (dialog, which) -> {
            //TODO: Send mesage to GameActivity to Calculate game!
        });
        builder.show();
    }

    private void callCameraActivity(int requestCode){
        Intent cameraIntent = new Intent(getActivity(), CamActivity.class);
        cameraIntent.putExtra("Data", requestCode);
        startActivityForResult(cameraIntent, requestCode);
    }

    private void addItemsToMenu(Menu menu, List<String> titles){
        for (int order = 0; order < titles.size(); order++){
            menu.add(0,order, 0, titles.get(order));
        }
    }

    @Override
    public int callbackPoints(ArrayList<Slot> slots, ArrayList<Dice> dices) {
        return player.getPoints();
    }

    private void slotsObserver(){
        final Observer<ArrayList<Slot>> slotsObserver = slots -> {
            if(player.isPlayerSet()){
                playerReady();
            }
        };
        player.getSlots().observe(this, slotsObserver);
    }
    private void dicesObserver(){
        final Observer<ArrayList<Dice>> diceObserver = new Observer<ArrayList<Dice>>() {
            @Override
            public void onChanged(ArrayList<Dice> dice) {
                if(player.isPlayerSet()){
                    playerReady();
                }
            }
        };
        player.getDices().observe(this, diceObserver);
    }
    private void cardsObserver(){
        final Observer<String> pqCardObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(player.isPlayerSet()){
                    playerReady();
                }
            }
        };
        player.personalQ.observe(this, pqCardObserver);
    }
    private void craftsmanObserver(){
        final Observer<Integer> craftsmanObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(player.isPlayerSet()){
                    playerReady();
                }
            }
        };
        player.getCraftsman().observe(this, craftsmanObserver);
    }
    private void setVariableObservers(){
        slotsObserver();
        dicesObserver();
        cardsObserver();
        craftsmanObserver();
    }

    private void playerReady(){ playerPointsButton.setBackgroundResource(R.drawable.points_btn_background_green);}
    private boolean isOnlyPlayer(){
        GameViewModel gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        return gameViewModel.getPlayersCount() == 1;
    }
}
