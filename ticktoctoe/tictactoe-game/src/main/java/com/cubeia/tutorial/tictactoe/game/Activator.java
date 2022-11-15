package com.cubeia.tutorial.tictactoe.game;

import com.cubeia.firebase.api.game.activator.DefaultActivator;
import com.cubeia.firebase.api.game.activator.MttAwareActivator;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import org.apache.log4j.Logger;

public class Activator extends DefaultActivator implements MttAwareActivator {

    private static final Logger log = Logger.getLogger(Activator.class);

    public Activator() {
        setCreationParticipant(new Participant());
    }

    @Override
    public void mttTableCreated(Table table, int mttId, Object commandAttachment, LobbyAttributeAccessor acc) {
        log.info("Tournament table created.");
        table.getGameState().setState(new Board());
    }
}
