package com.cubeia.tutorial.tictactoe.game;

import com.cubeia.firebase.api.game.*;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.table.Table;

public class GameImpl implements Game, TableListenerProvider, TournamentGame {

    @Override
    public void init(GameContext con) {
    }

    @Override
    public GameProcessor getGameProcessor() {
        return new Processor();
    }

    @Override
    public TournamentProcessor getTournamentProcessor() {
        return new Processor();
    }

    @Override
    public com.cubeia.firebase.api.game.table.TableListener getTableListener(Table table) {
        return new Listener();
    }

    @Override
    public void destroy() {
    }
}
