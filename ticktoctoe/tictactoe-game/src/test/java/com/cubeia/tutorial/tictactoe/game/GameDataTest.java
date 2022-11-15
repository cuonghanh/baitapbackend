package com.cubeia.tutorial.tictactoe.game;

import net.sf.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class GameDataTest {

    @Test
    public void testJson() {
        GameData data = new GameData();
        data.action = "act";
        data.pid = 3;
        data.board = new Board().toString();
        JSONObject json = JSONObject.fromObject(data);
        assertEquals("{\"action\":\"act\",\"board\":\"ZZZZZZZZZ\",\"pid\":3}", json.toString());
    }

}
