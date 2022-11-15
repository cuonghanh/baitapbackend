package com.cubeia.tutorial.tictactoe.game;

import static com.cubeia.tutorial.tictactoe.game.Winner.NONE;
import static java.nio.ByteBuffer.wrap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.TournamentProcessor;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableType;

import net.sf.json.JSONObject;

public class Processor implements GameProcessor, TournamentProcessor {
	private static final Logger log = Logger.getLogger(Processor.class);
	static ArrayList<Integer> mang1 = new ArrayList<>();

	public void handle(GameDataAction action, Table table) {
		// gửi từ clien lên sr
		Board board = (Board) table.getGameState().getState();
		log.info("client send data " + new String(action.getData().array()));

		if (action.getPlayerId() == board.getPlayerToAct()) {
			int cell = Integer.parseInt(new String(action.getData().array()));
			mang1.add(cell);
			Winner winner = board.play(cell, action.getPlayerId());
			notifyAllPlayers(table, createGameData(board, "update"));
			progress(table, board, winner, action.getPlayerId());
			if (winner.equals(Winner.TIE) || (winner.equals(Winner.NAUGHT)) || (winner.equals(Winner.CROSS)))
				mang1.clear();
			if (winner.equals(Winner.NONE))
				scheduleupdate(table);
		}
	}

	private void progress(Table table, Board board, Winner winner, int playerId) {
		GameData data;
		if (winner == NONE) {
			data = createGameData(board, "act");
			data.pid = board.getPlayerToAct();
		} else if (winner == Winner.TIE) {
			data = createGameData(board, "tie");
			scheduleNewGame(table);
		} else {
			data = createGameData(board, "win");
			data.pid = playerId;
			if (isTournamentTable(table)) {
				sendRoundReport(table, playerId);
			} else {
				scheduleNewGame(table);
			}
		}
		notifyAllPlayers(table, data);
	}

	// thời gian tạo game mới khi vừa chơi xong ván
	private void scheduleNewGame(Table table) {
		GameObjectAction action = new GameObjectAction(table.getId());
		action.setAttachment("start");
		table.getScheduler().scheduleAction(action, 3000);
	}

	private void scheduleupdate(Table table) {
		GameObjectAction action = new GameObjectAction(table.getId());
		action.setAttachment("update");
		table.getScheduler().scheduleAction(action, 3000);
	}

	private void schedulestop(Table table, Board board) {
		GameObjectAction action = new GameObjectAction(table.getId());
		action.setAttachment("update");
		board.schedule = table.getScheduler().scheduleAction(action, 3000);
		table.getScheduler().cancelScheduledAction(board.schedule);
	}

	private void sendRoundReport(Table table, int winner) {
		MttRoundReportAction roundReport = new MttRoundReportAction(table.getMetaData().getMttId(), table.getId());
		roundReport.setAttachment(winner);
		log.info("Sending round report where winner is " + winner + " mttId is " + roundReport.getMttId()
				+ " and tableId is " + roundReport.getTableId());
		table.getTournamentNotifier().sendToTournament(roundReport);
	}

	protected boolean isTournamentTable(Table table) {
		return table.getMetaData().getType() == TableType.MULTI_TABLE_TOURNAMENT;
	}

	public void handle(GameObjectAction action, Table table) {
		// log.info("da tao ban moi");
		// chỉ tạo bàn và start game mới
		if ("start".equals(action.getAttachment())) {
			TablePlayerSet players = table.getPlayerSet();
			Board board = (Board) table.getGameState().getState();
			board.clear();
			// ai là 1 ,2 ai là x o . nếu ko có thì sẽ ko hiển thị ở lần đâu tiền.
			Iterator<GenericPlayer> iterator = players.getPlayers().iterator();
			board.setPlayers(iterator.next().getPlayerId(), iterator.next().getPlayerId());
			GameData data = createGameData(board, "start");
			notifyAllPlayers(table, data);
		}

		if ("update".equals(action.getAttachment())) {
			Board board = (Board) table.getGameState().getState();
			int cell;
			if (checkXdanhO(table) != 10) {
				cell = checkXdanh(table);
				mang1.add(cell);
			} else {
				cell = randomcell(mang1);
			}
			Winner winner = board.play(cell, board.getPlayerToAct());
			notifyAllPlayers(table, createGameData(board, "update"));
			progress(table, board, winner, board.getPlayerToAct());
			if (winner.equals(Winner.NONE))
				schedulestop(table, board);
			if (winner.equals(Winner.TIE) || (winner.equals(Winner.NAUGHT)) || (winner.equals(Winner.CROSS)))
				mang1.clear();
		}
	}

	// check xem có trùng hay ko
	public boolean check(int x, ArrayList<Integer> arr) {
		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(i) == x) {
				return false;
			}
		}
		return true;
	}

	// ramdom từ 0 đến 8
	public int randomcell(ArrayList<Integer> arr) {
		int i = 0;
		while (i < 9) {
			int cell = ThreadLocalRandom.current().nextInt(0, 9);
			if (check(cell, arr) == true) {
				arr.add(cell);
				return cell;
			}
		}
		return randomcell(arr);
	}

	private void notifyAllPlayers(Table table, GameData data) {
		GameDataAction action = createAction(table, data);
		table.getNotifier().notifyAllPlayers(action);
	}

	private GameDataAction createAction(Table table, GameData data) {
		GameDataAction action = new GameDataAction(-1, table.getId());
		JSONObject json = JSONObject.fromObject(data);
		action.setData(wrap(json.toString().getBytes()));
		return action;
	}

	private GameData createGameData(Board board, String action) {
		GameData data = new GameData();
		data.board = board.toString();
		data.action = action;
		return data;
	}

	public int checkXdanhO(Table table) {
		Board board = (Board) table.getGameState().getState();
		String s = board.toString();
		if (s.charAt(0) == 'X') {
			if (s.charAt(1) == 'X' & check(2, mang1)) {
				return 2;
			} else if (s.charAt(2) == 'X' & check(1, mang1)) {
				return 1;
			} else if (s.charAt(4) == 'X' & check(8, mang1)) {
				return 8;
			} else if (s.charAt(8) == 'X' & check(4, mang1)) {
				return 4;
			} else if (s.charAt(3) == 'X' & check(6, mang1)) {
				return 6;
			} else if (s.charAt(6) == 'X' & check(3, mang1)) {
				return 3;
			}
		}
		if (s.charAt(1) == 'X') {
			if (s.charAt(2) == 'X' && check(0, mang1)) {
				return 0;
			} else if (s.charAt(4) == 'X' && check(7, mang1)) {
				return 7;
			} else if (s.charAt(7) == 'X' && check(4, mang1)) {
				return 4;
			}
		}
		if (s.charAt(2) == 'X') {
			if (s.charAt(4) == 'X' && check(6, mang1)) {
				return 6;
			} else if (s.charAt(6) == 'X' && check(4, mang1)) {
				return 4;
			} else if (s.charAt(5) == 'X' && check(8, mang1)) {
				return 8;
			} else if (s.charAt(8) == 'X' && check(5, mang1)) {
				return 5;
			} else if (s.charAt(1) == 'X' && check(0, mang1)) {
				return 0;
			}
		}
		if (s.charAt(3) == 'X') {
			if (s.charAt(4) == 'X' && check(5, mang1)) {
				return 5;
			} else if (s.charAt(5) == 'X' && check(4, mang1)) {
				return 4;
			} else if (s.charAt(6) == 'X' && check(0, mang1)) {
				return 0;
			}
		}
		if (s.charAt(4) == 'X') {
			if (s.charAt(5) == 'X' && check(3, mang1)) {
				return 3;
			} else if (s.charAt(6) == 'X' && check(2, mang1)) {
				return 2;
			} else if (s.charAt(7) == 'X' && check(1, mang1)) {
				return 1;
			} else if (s.charAt(8) == 'X' && check(0, mang1)) {
				return 0;
			}
		}
		if (s.charAt(5) == 'X' && s.charAt(8) == 'X' && check(2, mang1)) {
			return 2;
		}
		if (s.charAt(5) == 'X' && s.charAt(4) == 'X' && check(3, mang1)) {
			return 3;
		}
		if (s.charAt(6) == 'X' && s.charAt(7) == 'X' && check(8, mang1)) {
			return 8;
		}
		if (s.charAt(6) == 'X' && s.charAt(3) == 'X' && check(0, mang1)) {
			return 0;
		}
		if (s.charAt(6) == 'X' && s.charAt(4) == 'X' && check(2, mang1)) {
			return 2;
		}
		if (s.charAt(6) == 'X' && s.charAt(8) == 'X' && check(7, mang1)) {
			return 7;
		}
		if (s.charAt(7) == 'X' && s.charAt(8) == 'X' && check(6, mang1)) {
			return 6;
		}
		if (s.charAt(8) == 'X' && s.charAt(7) == 'X' && check(6, mang1)) {
			return 6;
		}
		if (s.charAt(8) == 'X' && s.charAt(4) == 'X' && check(0, mang1)) {
			return 0;
		}
		if (s.charAt(8) == 'X' && s.charAt(5) == 'X' && check(2, mang1)) {
			return 2;
		}
		return 10;

	}

	public int checkXdanh(Table table) {
		Board board = (Board) table.getGameState().getState();
		String s = board.toString();
		// 0 3 6
		for (int i = 0; i < 9; i = i + 3) {
			if (s.charAt(i) == 'X' && s.charAt(i + 1) == 'X' && check((i + 2), mang1)) {
				return (i + 2);
			}
			if (s.charAt(i) == 'X' && s.charAt(i + 2) == 'X' && check((i + 1), mang1)) {
				return (i + 1);
			}
		}
		//0 1 2
		for (int i = 0; i < 3; i++) {
			if (s.charAt(i) == 'X' && s.charAt(i + 3) == 'X' && check((i + 6), mang1)) {
				return (i + 6);
			}
			if (s.charAt(i) == 'X' && s.charAt(i + 6) == 'X' && check((i + 3), mang1)) {
				return (i + 3);
			}
		}
		//8 7 6 
		for (int i = 8; i > 5; i--) {
			if (s.charAt(i) == 'X' && s.charAt(i - 3) == 'X' && check((i - 6), mang1)) {
				return (i - 6);
			}
			if (s.charAt(i) == 'X' && s.charAt(i - 6) == 'X' && check((i - 3), mang1)) {
				return (i - 3);
			}
		}
		 //8 5 2
		for (int i = 8; i > 0; i = i - 3) {
			if (s.charAt(i) == 'X' && s.charAt(i - 1) == 'X' && check((i - 2), mang1)) {
				return (i - 2);
			}
			if (s.charAt(i) == 'X' && s.charAt(i - 2) == 'X' && check((i - 1), mang1)) {
				return (i - 1);
			}
		}
		if(s.charAt(0)=='X'&& s.charAt(4)=='X' && check(8, mang1)) {
			return 8;
		}
		if(s.charAt(0)=='X'&& s.charAt(8)=='X' && check(4, mang1)) {
			return 4;
		}
		if(s.charAt(8)=='X'&& s.charAt(4)=='X' && check(0, mang1)) {
			return 0;
		}
		if(s.charAt(2)=='X'&& s.charAt(4)=='X' && check(6, mang1)) {
			return 6;
		}
		if(s.charAt(2)=='X'&& s.charAt(6)=='X' && check(4, mang1)) {
			return 4;
		}
		if(s.charAt(6)=='X'&& s.charAt(4)=='X' && check(2, mang1)) {
			return 2;
		}

		return 10;
	}

	@Override
	public void startRound(Table table) {
//		log.info("Starting round " + table.getId() + " in 3 seconds..");
//		GameObjectAction action = new GameObjectAction(table.getId());
//		action.setAttachment("start");
//		table.getScheduler().scheduleAction(action, 3000);
	}

	@Override
	public void stopRound(Table table) {

	}

}