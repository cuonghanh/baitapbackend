var _tableId = -1;
var _playerId = -1;
var lobbyData = new Array();
var tictactoe;

function lobbyCallback(protocolObject) {
    console.log("Received lobby packet: " + protocolObject);
    switch (protocolObject.classId) {
        case FB_PROTOCOL.TableSnapshotListPacket.CLASSID:
            handleTableSnapshotList(protocolObject.snapshots);
            break;
        case FB_PROTOCOL.TableUpdateListPacket.CLASSID:
            console.log("Handle update, left as exercise");
            break;
        case FB_PROTOCOL.TableRemovedPacket.CLASSID:
            console.log("Handle removed, left as exercise");
            break;
    }
}

function packetCallback(packet) {
    console.log("Received packet: " + packet + " classId: " + packet.classId);

    switch (packet.classId) {
        case FB_PROTOCOL.NotifyJoinPacket.CLASSID:
            gameMessage("Player " + packet.pid + " joined");
            break;
        case FB_PROTOCOL.NotifyLeavePacket.CLASSID:
            gameMessage("Player " + packet.pid + " left");
            break;
        case FB_PROTOCOL.SeatInfoPacket.CLASSID:
            gameMessage("Player " + packet.player.pid + " is seated in seat " + packet.seat);
            break;
        case FB_PROTOCOL.JoinResponsePacket.CLASSID:
            gameMessage("Join response: " + packet.status);
            joinTable()
            break;
        case FB_PROTOCOL.GameTransportPacket.CLASSID:
            handleGameTransport(packet);
            break;
        case FB_PROTOCOL.WatchResponsePacket.CLASSID:
            gameMessage("Watch response for table: " + packet.tableid);
            break;
        case FB_PROTOCOL.LeaveResponsePacket.CLASSID:
            gameMessage("Leave response received for table: " + packet.tableid);
            break;
        case FB_PROTOCOL.TableChatPacket.CLASSID:
            gameMessage("Received chat from " + packet.pid + ": " + packet.message);
            break;
    }
}

function loginCallback(status, playerId, name) {
    console.log("Login status " + status + " playerId " + playerId + " name " + name);
    message("Login " + status + '</p>');
    if (status === 'OK') {
        console.log('Login OK id: ' + playerId + ', subscribing to lobby.');
        _playerId = playerId
        createGrid();
        connector.lobbySubscribe(888, "");
        message("Subscribing to lobby ");
    }
}

function statusCallback(status) {
    console.log("Status received: " + status);
    if (status === FIREBASE.ConnectionStatus.CONNECTED) {
        console.log("Connected");
        message('Connected to Firebase');
    } else if (status === FIREBASE.ConnectionStatus.DISCONNECTED) {
        console.log("Disconnected");
        message('Disconnected from Firebase');
    }
}

function handleGameTransport(packet) {
    // We need to first base64 decode the message.
    var byteArray = FIREBASE.ByteArray.fromBase64String(packet.gamedata);
    // And then convert the byte array to a string.
    var message = utf8.fromByteArray(byteArray);
    console.log("Received message: " + message);
    gameMessage("Player " + packet.pid + " sent: " + message);

    gameAction(message)
}

function handleTableSnapshotList(tableSnapshotList) {
    for (var i = 0; i < tableSnapshotList.length; i++) {
        handleTableSnapshot(tableSnapshotList[i]);
    }
    jQuery("#lobbyList").trigger("reloadGrid");
}

function handleTableSnapshot(tableSnapshot) {
    if (findTable(tableSnapshot.tableid) === null) {
        var i = lobbyData.push({ id: tableSnapshot.tableid, name: tableSnapshot.name, capacity: tableSnapshot.capacity, seated: tableSnapshot.seated });
        console.debug("tableId: " + tableSnapshot.tableid);
        jQuery("#lobbyList").jqGrid('addRowData', tableSnapshot.tableid, lobbyData[i - 1]);
    }
}

function findTable(tableid) {
    for (var i = 0; i < lobbyData.length; i++) {
        var object = lobbyData[i];
        if (object.id == tableid) {
            return object;
        }
    }
    return null;
}

function message(msg) {
    $('#eventLog').append('<p class="event">' + msg + '</p>');
}

function gameMessage(msg) {
    $('#gameMessages').append('<p class="event">' + msg + '</p>');
}

function doLogin() {
    $('#result').empty();
    sendLogin();
}

function sendLogin() {
    var username = $('#username').val();
    var password = $('#password').val();
    if (username == "" || password == "") {
        message('Please enter a user name and a password');
        return;
    }

    console.log("Logging in with username " + username + " and password " + password);
    connector.login(username, password);
}

function doSendMessage() {
    var textToSend = $('#message').val();
    message("Sending " + textToSend);

    connector.sendStringGameData(0, _tableId, textToSend);
}

function createGrid() {
    $("#lobbyList").jqGrid({
        datatype: "local",
        data: lobbyData,
        height: 204,
        colNames: ['Name', 'Capacity', 'Seated', ''],
        colModel: [
            { name: 'name', index: 'name', width: 250, sorttype: "string" },
            { name: 'capacity', index: 'capacity', width: 110, sorttype: "int" },
            { name: 'seated', index: 'seated', width: 110, sorttype: "int" },
            { name: 'act', index: 'act', width: 100 }
        ],
        caption: "Lobby",
        scroll: true,
        multiselect: false,
        gridComplete: function () {
            var ids = jQuery("#lobbyList").jqGrid('getDataIDs');
            for (var i = 0; i < ids.length; i++) {
                var tableId = ids[i];
                playButton = "<input class='ui-button' type='button' value='Open' onclick='openTable(" + tableId + ");'/>";
                jQuery("#lobbyList").jqGrid('setRowData', ids[i], { act: playButton });
            }

        },
        cellSelect: function () {
        }

    });
    console.debug("grid created");
}

function openTable(tableId) {
    console.log("Opening table for tableId " + tableId);
    connector.joinTable(tableId, -1);
    _tableId = tableId;
    
}

function joinTable(){
    $('#lobby').hide();
    $('#table').show();

    setupBoard();
}

function setupBoard() {
    tictactoe = new Tictactoe(connector, _tableId, _playerId);
    tictactoe.paintBoard();

    var game_board = document.getElementById("board");

    game_board.addEventListener('click', function () {
        tictactoe.clickHandler(event);
    }, true);
}

function  gameAction(message){
    tictactoe.action(message)
}

