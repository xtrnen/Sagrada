package com.example.sagrada;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.List;

import Model.GameBoard.Player;
import ViewModel.GameViewModel;


public class CreatePlayerDialogFragment extends DialogFragment {
    private boolean cqFlag;
    private int cqIndex;
    private int pqIndex;

    public interface ICreatePlayerDialogListener{
        public void onCreatePlayerSubmit(String username, int cqIndex, int pqIndex);
        public void onCreatePlayerCanceled();
    }
    //TODO: On empty username string don't close dialog

    public CreatePlayerDialogFragment(boolean flag){
        super();
        cqFlag = flag;
        cqIndex = -42;
        pqIndex = -42;
    }

    ICreatePlayerDialogListener listener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            listener = (ICreatePlayerDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "No ICreatePlayerDialogListener implementation");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.create_player_layout, null);

        builder.setView(view);

        builder.setTitle(R.string.createPlayerDialog_title);

        Button cqButton = view.findViewById(R.id.cqChooseBtnID);
        Button pqButton = view.findViewById(R.id.pqChooseBtnID);
        TextView cqTextView = view.findViewById(R.id.chooseCQID);
        TextView pqTextView = view.findViewById(R.id.choosePQID);

        if(cqFlag){
            LinearLayout linearLayout = view.findViewById(R.id.cqLayoutID);
            linearLayout.setVisibility(View.VISIBLE);
        }

        cqButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.cq_menu_layout, popupMenu.getMenu());
            popupMenu.getMenu().clear();
            addItemsToMenu(popupMenu.getMenu(), Arrays.asList(getResources().getStringArray(R.array.groupQuestStrings)));
            popupMenu.setOnMenuItemClickListener(item -> {
                cqTextView.setText(item.getTitle());
                cqIndex = item.getOrder();
                return true;
            });
            popupMenu.show();
        });
        pqButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.quest_personal_layout, popupMenu.getMenu());
            popupMenu.getMenu().clear();
            addItemsToMenu(popupMenu.getMenu(), Arrays.asList(getResources().getStringArray(R.array.personalQuestStrings)));
            popupMenu.setOnMenuItemClickListener(item -> {
                pqTextView.setText(item.getTitle());
                pqIndex = item.getItemId();
                return true;
            });
            popupMenu.show();
        });

        builder.setNegativeButton(R.string.gameModeDialogNeutral_title, (dialog, which) -> {
            listener.onCreatePlayerCanceled();
        });

        builder.setPositiveButton(R.string.gameModeDialogPositive_title, (dialog, which) -> {
            EditText username = (EditText)getDialog().findViewById(R.id.createUsernameID);
            String playerUsername = username.getText().toString();

            GameViewModel gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);

            List<Player> players = gameViewModel.getPlayers().getValue();

            boolean userExist = false;
            if(players != null){
                for (Player player:players) {
                    if(player.name.equalsIgnoreCase(playerUsername)){
                        userExist = true;
                        break;
                    }
                }
            }

            if(!userExist && !usernameIsEmpty(playerUsername)){
                listener.onCreatePlayerSubmit(playerUsername, cqIndex, pqIndex);
                this.dismiss();
            }
        });

        return builder.create();
    }

    boolean usernameIsEmpty(String username){
            return (username.trim().isEmpty() || username.equals(""));
    }

    private void addItemsToMenu(Menu menu, List<String>titles){
        for (int order = 0; order < titles.size(); order++){
            menu.add(0,order, 0, titles.get(order));
        }
    }

}
