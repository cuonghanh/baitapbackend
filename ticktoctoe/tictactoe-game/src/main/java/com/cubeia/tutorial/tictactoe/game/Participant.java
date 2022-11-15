package com.cubeia.tutorial.tictactoe.game;

import com.cubeia.firebase.api.game.activator.DefaultCreationParticipant;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.Table;

public class Participant extends DefaultCreationParticipant {

    @Override
    public void tableCreated(Table table, LobbyTableAttributeAccessor acc) {
        super.tableCreated(table, acc);
        System.out.println("create game"+ table.getId());
        table.getGameState().setState(new Board());
    }

}
