package com.example.sagrada;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.sagrada.databinding.PlayerLayoutBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import Model.GameBoard.Structs.Dice;
import Model.GameBoard.Structs.Slot;
import Model.Points.Quests.CQ_TYPES;
import Model.Points.Quests.PQ_TYPES;
import ViewModel.GameViewModel;
import ViewModel.PlayerViewModel;

public class PlayerFragment extends Fragment implements IPlayerPointsCallback {
    private PlayerViewModel player;
    private static final String FRAGMENT_POSITION = "Pos";
    private static final String FRAGMENT_NAME = "Name";
    private static final String FRAGMENT_PQ = "PQ";
    final static int REQUEST_INFO = 41;
    final static int REQUEST_INFO_VALID = 42;
    final static int REQUEST_INFO_INVALID = -42;
    private Integer counter;
    private Button playerPointsButton;
    private GameViewModel gameViewModel;
    IPlayerPointsCallback pointsCallback;
    private int eglomiseValue;
    private int sandpaperValue;
    private boolean ruleOK = false;

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
        if(resultCode == GameActivity.REQUEST_SLOTS){
            assert data != null;
            ArrayList<Slot> slots = data.getParcelableArrayListExtra(GameActivity.DATA_SLOTS);
            player.setSlots(slots);
        }
        if(resultCode == GameActivity.REQUEST_DICES){
            assert data != null;
            ArrayList<Dice> dices = data.getParcelableArrayListExtra(GameActivity.DATA_DICES);
            player.setDices(dices);
        }
        if(resultCode == REQUEST_INFO_VALID){
            assert data != null;
            player.setDices(data.getParcelableArrayListExtra(GameActivity.DATA_DICES));
            player.setSlots(data.getParcelableArrayListExtra(GameActivity.DATA_SLOTS));
            playerPointsButton.setBackgroundResource(R.drawable.points_btn_background_green);
            ruleOK = true;
        }
        if(resultCode == REQUEST_INFO_INVALID){
            assert data != null;
            player.setDices(data.getParcelableArrayListExtra(GameActivity.DATA_DICES));
            player.setSlots(data.getParcelableArrayListExtra(GameActivity.DATA_SLOTS));
            playerPointsButton.setBackgroundResource(R.drawable.points_btn_background_red);
        }
        if(resultCode == GameActivity.REQUEST_INFO_ACTIVITY){
            if(requestCode == GameActivity.REQUEST_SLOTS){
                assert data != null;
                player.setSlots(data.getParcelableArrayListExtra(GameActivity.DATA_SLOTS));
            }
            if(requestCode == GameActivity.REQUEST_DICES){
                assert data != null;
                player.setDices(data.getParcelableArrayListExtra(GameActivity.DATA_DICES));
            }
            callInformationActivity();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            counter = getArguments().getInt(FRAGMENT_POSITION);
        }
        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
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
        ImageButton playerCameraButton = view.findViewById(R.id.playerToolbarCameraButton);
        ImageButton playerPersonalQuestButton = view.findViewById(R.id.personalQuestBtnID);
        ImageButton playerCraftsmanPlusButton = view.findViewById(R.id.craftsmanPlusBtnID);
        ImageButton playerCraftsmanSubButton = view.findViewById(R.id.craftsmanSubBtnID);
        ImageButton playerInfoButton = view.findViewById(R.id.playerToolbarInfoButton);
        ImageButton addCQButton = view.findViewById(R.id.gameToolbarAddCQID);
        playerPointsButton = view.findViewById(R.id.playerPointsBtnID);
        playerPointsButton.setText(pointStringLocale() + "0");
        playerCameraButton.setOnClickListener(v -> createCaptureModeDialog());
        playerPointsButton.setOnClickListener(v -> {
            if(player.isPlayerSet() && ruleOK){
                calculateThis();
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
        addCQButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.cq_menu_layout, popupMenu.getMenu());
            popupMenu.getMenu().clear();
            addItemsToMenu(popupMenu.getMenu(), Arrays.asList(getResources().getStringArray(R.array.groupQuestStrings)));
            popupMenu.setOnMenuItemClickListener(item -> {
                gameViewModel.addCommonQuest(item.getItemId());
                return true;
            });
            popupMenu.show();
        });
        playerInfoButton.setOnClickListener( v -> callInformationActivity());
        playerCraftsmanPlusButton.setOnClickListener( v -> player.addCraftsmanPoint());
        playerCraftsmanSubButton.setOnClickListener(v -> player.subCraftsmanPoint());
        setVariableObservers();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /*Dialogs*/
    private void createCaptureModeDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setMessage(R.string.captureDiceSlotString);
        dialogBuilder.setNeutralButton(R.string.cancelString, (dialog, which) -> {
            //Nothing
        });
        dialogBuilder.setNegativeButton(R.string.patternString, (dialog, which) -> {
           callCameraActivity(GameActivity.REQUEST_SLOTS);
        });
        dialogBuilder.setPositiveButton(R.string.diceString, (dialog, which) -> {
           callCameraActivity(GameActivity.REQUEST_DICES);
        });
        dialogBuilder.show();
    }

    private void callCameraActivity(int requestCode){
        Intent cameraIntent = new Intent(getActivity(), CamActivity.class);
        cameraIntent.putExtra("Data", requestCode);
        startActivityForResult(cameraIntent, requestCode);
    }
    private void callInformationActivity(){
        Intent intent = new Intent(getActivity(), InformationActivity.class);
        intent.putParcelableArrayListExtra(GameActivity.DATA_SLOTS, player.getSlots().getValue());
        intent.putParcelableArrayListExtra(GameActivity.DATA_DICES, player.getDices().getValue());
        startActivityForResult(intent, REQUEST_INFO);
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
            if(player.isPlayerSet() && ruleOK){
                playerReady();
            }
        };
        player.getSlots().observe(this, slotsObserver);
    }
    private void dicesObserver(){
        final Observer<ArrayList<Dice>> diceObserver = new Observer<ArrayList<Dice>>() {
            @Override
            public void onChanged(ArrayList<Dice> dice) {
                if(player.isPlayerSet() && ruleOK){
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
                if(player.isPlayerSet() && ruleOK){
                    playerReady();
                }
            }
        };
        player.personalQ.observe(this, pqCardObserver);
    }
    private void craftsmanObserver(){
        final Observer<Integer> craftsmanObserver = integer -> {
            if(player.isPlayerSet() && ruleOK){
                playerReady();
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

    private void playerReady(){ playerPointsButton.setBackgroundResource(R.drawable.points_btn_background_green); calculateThis();}
    private void calculateThis(){
        ArrayList<Dice> dicesArray = player.getDices().getValue();
        ArrayList<Slot> slotsArray = player.getSlots().getValue();
        gameViewModel.gameBoard.setDiceArray(dicesArray.toArray(new Dice[dicesArray.size()]));
        gameViewModel.gameBoard.setSlotArray(slotsArray.toArray(new Slot[slotsArray.size()]));
        int points = gameViewModel.gameBoard.Evaluation(PQ_TYPES.values()[player.getPQIndex().getValue()], CQ_TYPES.values()[gameViewModel.getCommonQuest().getValue()], player.getCraftsman().getValue());
        player.setPoints(points);
        String text;
        text = pointStringLocale();
        playerPointsButton.setText(text + points);
    }
    private String pointStringLocale() {
        String text;
        if (ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0).getDisplayLanguage().equals("čeština")) {
            text = "Body: ";
        } else {
            text = "Points: ";
        }
        return text;
    }
}
