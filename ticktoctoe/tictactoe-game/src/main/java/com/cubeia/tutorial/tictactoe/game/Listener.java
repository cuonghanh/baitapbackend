package com.cubeia.tutorial.tictactoe.game;

import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.game.table.TournamentTableListener;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class Listener implements TableListener, TournamentTableListener {

    private static final Logger log = Logger.getLogger(Listener.class);

    @Override
    
    
    //thời gian tạo game đầu tiên
    public void playerJoined(Table table, GenericPlayer player) {
        log.info("Player joined!");
        if (table.getPlayerSet().getPlayerCount() == 2) {
            log.info("We got two players, starting game!");
            GameObjectAction action = new GameObjectAction(table.getId());
            action.setAttachment("start");
            table.getScheduler().scheduleAction(action, 3000);
        }
    }

    @Override
    public void playerLeft(Table table, int playerId) {

    }

    @Override
    public void watcherJoined(Table table, int playerId) {

    }

    @Override
    public void watcherLeft(Table table, int playerId) {

    }

    @Override
    public void playerStatusChanged(Table table, int playerId, PlayerStatus status) {

    }

    @Override
    public void seatReserved(Table table, GenericPlayer player) {

    }

    @Override
    public void tournamentPlayerJoined(Table table, GenericPlayer player, Serializable serializable) {

    }

    @Override
    public void tournamentPlayerRemoved(Table table, int playerId, UnseatPlayersMttAction.Reason reason) {

    }

    @Override
    public void tournamentPlayerRejoined(Table table, GenericPlayer player) {

    }
}
