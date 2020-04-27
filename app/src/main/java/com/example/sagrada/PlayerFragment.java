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
import android.widget.EditText;
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
import Model.Rules.RULE_ERR;
import Model.Rules.RuleMsg;
import ViewModel.GameViewModel;
import ViewModel.PlayerViewModel;

public class PlayerFragment extends Fragment implements IPlayerPointsCallback, CraftsmanPointsDialogFragment.ICraftsmanCards {
    private PlayerViewModel player;
    private static final String FRAGMENT_POSITION = "Pos";
    private static final String FRAGMENT_NAME = "Name";
    private static final String FRAGMENT_PQ = "PQ";
    private Integer counter;
    private Button playerPointsButton;
    private GameViewModel gameViewModel;
    IPlayerPointsCallback pointsCallback;
    private boolean isEglomise;
    private boolean isSandpaper;
    private int eglomiseValue;
    private int sandpaperValue;

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
            assert slots != null;
            for (Slot slot : slots){
                Log.d("SLOT", slot.infoType);
            }
            player.setSlots(slots);
        }
        if(resultCode == GameActivity.REQUEST_DICES){
            assert data != null;
            ArrayList<Dice> dices = data.getParcelableArrayListExtra(GameActivity.DATA_DICES);
            assert dices != null;
            for(Dice dice : dices){
                Log.println(Log.INFO, "DICES", dice.color + "|" + dice.number + "( " + dice.row + "|" + dice.col + ")");
            }
            player.setDices(dices);
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
            if(player.isPlayerSet()){
                //pointsCallback.callbackPoints(player.getSlots().getValue(), player.getDices().getValue());
                if(isOnlyPlayer()){
                    createCraftsmanUseDialog();
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
        playerInfoButton.setOnClickListener( v -> {
            ArrayList<Slot> slots = testSlots();
            ArrayList<Dice> dices = testDices();
            player.setSlots(slots);
            player.setDices(dices);
        });
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
    private void createCraftsmanUseDialog(){
        //TODO: refactor to use for question if 2 specific craftsman cards were used
        //TODO: Dialog pro potvrzení jedné ze dvou karet pro porušení pravidla "Rydlo na eglomisé - lze porušit barvu políčka", "Brusný papír - lze porušit číslo pole"
        CraftsmanPointsDialogFragment craftsmanPointsDialogFragment = new CraftsmanPointsDialogFragment();
        craftsmanPointsDialogFragment.setTargetFragment(this, 0);
        craftsmanPointsDialogFragment.show(getActivity().getSupportFragmentManager(), "craftsmanDialog");
    }
    private void createCalculationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.calculateDialogTitle);
        builder.setMessage(R.string.calculateDialogMsg);
        builder.setNeutralButton(R.string.cancelString, (dialog, which) -> {});
        builder.setPositiveButton(R.string.calculateDialogPlayer, (dialog, which) -> {
            calculateThis();
        });
        builder.setNegativeButton(R.string.calculateDialogAll, (dialog, which) -> {
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
        final Observer<Integer> craftsmanObserver = integer -> {
            if(player.isPlayerSet()){
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

    private void playerReady(){ playerPointsButton.setBackgroundResource(R.drawable.points_btn_background_green);}
    private boolean isOnlyPlayer(){
        return gameViewModel.getPlayersCount() == 1;
    }
    private void calculateThis(){
        ArrayList<Dice> dicesArray = player.getDices().getValue();
        ArrayList<Slot> slotsArray = player.getSlots().getValue();
        gameViewModel.gameBoard.setDiceArray(dicesArray.toArray(new Dice[dicesArray.size()]));
        gameViewModel.gameBoard.setSlotArray(slotsArray.toArray(new Slot[slotsArray.size()]));
        gameViewModel.gameBoard.assignToRuleHandler();
        int points = 0;
        if(isEglomise){
            Log.println(Log.INFO, "Calculate", "isEglomise card " + eglomiseValue);
        }
        if(isSandpaper){
            Log.println(Log.INFO, "Calculate", "isSandpaper card " + sandpaperValue);
        }
        if(gameViewModel.gameBoard.ruleCheck()){
            points = gameViewModel.gameBoard.Evaluation(PQ_TYPES.values()[player.getPQIndex().getValue()], CQ_TYPES.values()[gameViewModel.getCommonQuest().getValue()], player.getCraftsman().getValue());
        } else {
            playerPointsButton.setBackgroundResource(R.drawable.points_btn_background_red);
            //Check error types
            ArrayList<RuleMsg> list = gameViewModel.gameBoard.getLogList();
        }
        player.setPoints(points);
        String text;
        text = pointStringLocale();
        playerPointsButton.setText(text + points);
    }
    private ArrayList<Dice> testDices(){
        ArrayList<Dice> array = new ArrayList<Dice>();

        array.add(new Dice("GREEN", 6, 0,2));
        array.add(new Dice("BLUE", 2, 1,2));
        array.add(new Dice("YELLOW", 3, 2,2));
        array.add(new Dice("RED", 1, 3,2));

        return array;
    }
    private ArrayList<Slot> testSlots(){
        ArrayList<Slot> array = new ArrayList<Slot>();

        array.add(new Slot("WHITE",0,0));
        array.add(new Slot("WHITE", 0,1));
        array.add(new Slot("SIX", 0,2));
        array.add(new Slot("WHITE",0 ,3));
        array.add(new Slot("WHITE", 0,4));

        array.add(new Slot("WHITE", 1,0));
        array.add(new Slot("FIVE", 1,1));
        array.add(new Slot("BLUE", 1,2));
        array.add(new Slot("FOUR", 1,3));
        array.add(new Slot("WHITE", 1,4));

        array.add(new Slot("THREE", 2,0));
        array.add(new Slot("GREEN", 2,1));
        array.add(new Slot("YELLOW", 2,2));
        array.add(new Slot("VIOLET", 2,3));
        array.add(new Slot("TWO", 2,4));

        array.add(new Slot("ONE", 3,0));
        array.add(new Slot("FOUR", 3,1));
        array.add(new Slot("RED", 3,2));
        array.add(new Slot("FIVE", 3,3));
        array.add(new Slot("THREE", 3,4));

        return array;
    }
    private String pointStringLocale() {
        String text;
            if (ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0).getDisplayLanguage().equals("cs")) {
            text = "Body: ";
        } else {
            text = "Points: ";

        }
        return text;
    }

    @Override
    public void getCraftValues(int sandpaper, int eglomise, boolean sandpaperCheck, boolean eglomiseCheck) {
        isEglomise = eglomiseCheck;
        isSandpaper = sandpaperCheck;
        eglomiseValue = eglomise;
        sandpaperValue = sandpaper;
        calculateThis();
    }
}
