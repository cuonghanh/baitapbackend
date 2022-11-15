var Tictactoe = function (connector, tableId, playerId) {
    var xBoard = 0;
    var oBoard = 0;
    var begin = false;
    var context;
    var width, height;
    var score_x = 0;
    var score_o = 0;

    // this.get_score = function () {
    //     document.getElementById("score_x").innerText = "X: " + score_x;
    //     document.getElementById("score_o").innerText = "O: " + score_o;
    // };

    this.paintBoard = function () {
        // this.get_score();

        var board = document.getElementById('board');

        width = board.width;
        height = board.height;
        context = board.getContext('2d');

        context.beginPath();
        context.strokeStyle = '#000';
        context.lineWidth = 4;

        context.moveTo((width / 3), 0);
        context.lineTo((width / 3), height);

        context.moveTo((width / 3) * 2, 0);
        context.lineTo((width / 3) * 2, height);

        context.moveTo(0, (height / 3));
        context.lineTo(width, (height / 3));

        context.moveTo(0, (height / 3) * 2);
        context.lineTo(width, (height / 3) * 2);

        context.stroke();
        context.closePath();

        if (begin) {
            var ini = Math.abs(Math.floor(Math.random() * 9 - 0.1));
            this.markBit(1 << ini, 'O');
            begin = false;
        } else {
            //begin = true;
        }


    };


    this.checkWinner = function (board) {

        var result = false;

        if (((board | 0x1C0) == board) || ((board | 0x38) == board) ||
            ((board | 0x7) == board) || ((board | 0x124) == board) ||
            ((board | 0x92) == board) || ((board | 0x49) == board) ||
            ((board | 0x111) == board) || ((board | 0x54) == board)) {

            result = true;
        }
        return result;
    };

    this.paintX = function (x, y) {

        context.beginPath();

        context.strokeStyle = '#ff0000';
        context.lineWidth = 4;

        var offsetX = (width / 3) * 0.1;
        var offsetY = (height / 3) * 0.1;

        var beginX = x * (width / 3) + offsetX;
        var beginY = y * (height / 3) + offsetY;

        var endX = (x + 1) * (width / 3) - offsetX * 2;
        var endY = (y + 1) * (height / 3) - offsetY * 2;

        context.moveTo(beginX, beginY);
        context.lineTo(endX, endY);

        context.moveTo(beginX, endY);
        context.lineTo(endX, beginY);

        context.stroke();
        context.closePath();
    };

    this.paintO = function (x, y) {

        context.beginPath();

        context.strokeStyle = '#0000ff';
        context.lineWidth = 4;

        var offsetX = (width / 3) * 0.1;
        var offsetY = (height / 3) * 0.1;

        var beginX = x * (width / 3) + offsetX;
        var beginY = y * (height / 3) + offsetY;

        var endX = (x + 1) * (width / 3) - offsetX * 2;
        var endY = (y + 1) * (height / 3) - offsetY * 2;

        context.arc(beginX + ((endX - beginX) / 2), beginY + ((endY - beginY) / 2), (endX - beginX) / 2, 0, Math.PI * 2, true);

        context.stroke();
        context.closePath();
    };

    var play_turn = 'X';

    var myTurn = false;

    Tictactoe.prototype.clickHandler = function (e) {

        // if (!myTurn) return;

        // We need to calculate the position with 0, 0 as the top left corner of the canvas.
        var relativeX = e.pageX - $("#board").offset().left;
        var relativeY = e.pageY - $("#board").offset().top;

        var x = Math.floor(relativeX / (width / 3));
        var y = Math.floor(relativeY / (height / 3));

        // The server counts the cells as this:
        // 0 1 2
        // 3 4 5
        // 6 7 8
        // So, to get the cell number, given the row and column, we multiply the row by 3 and add the column.
        var cell = y * 3 + x;

        console.log(cell)
        connector.sendStringGameData(0, tableId, "" + cell);

        // var y = Math.floor(e.clientY / (height / 3));    
        // var x =  Math.floor(e.clientX / (width/ 3)); 



        // var bit =  (1 << x + ( y * 3 ));


        // if (this.isEmpty(xBoard, oBoard, bit)) {
        //     if (play_turn === 'X') {
        //         this.markBit(bit, 'X');
        //         play_turn = 'O';
        //     } else if ( play_turn === 'O') {
        //         this.markBit(bit, 'O');
        //         play_turn = 'X';
        //     }    
        // }



        // if (this.checkWinner(xBoard)) {
        //     alert('X Won!');
        //     score_x++;
        //     this.restart();
        // } else if (this.checkWinner(oBoard)) {
        //     alert('O Won!');
        //     score_o++;
        //     this.restart();
        // } else if (this.checkNobody()) {
        //     alert('Nobody won!!');
        //     this.restart();
        // }
    };

    Tictactoe.prototype.action = (message) => {
        json = JSON.parse(message)

        if (json.action == "act") {
            if (json.pid == playerId) {
                gameMessage("My turn to act.");
                $('#turn').text("It's your turn!");
                myTurn = true;
            } else {
                $('#turn').text("It's not your turn.");
                myTurn = false;
            }
        }
        if (json.action == "update") {
            for(var i = 0 ; i < json.board.length; i++){
                var p = json.board.charAt(i)
                if (p != "Z"){
                    var x = i %3;
                    var y = Math.floor(i / 3);
                    console.log(x, y)

                    if(p == "X"){
                        this.paintX(x, y);
                    }else{
                        this.paintO(x, y);
                    }
                }
            }
        }

        if (json.action == "win"){
            if(json.pid == playerId){
                alert("You Won");
            }else{
                alert("You Lost")
            }

            this.restart()
        }
        if (json.action == "tie"){
            alert("Tie")

            this.restart()
        }
    }

    this.checkNobody = function () {
        if ((xBoard | oBoard) == 0x1FF) {
            return true;
        }
        return false;
    };

    this.restart = function () {
        //alert("REstarting");
        context.clearRect(0, 0, width, height);
        xBoard = 0;
        oBoard = 0;
        this.paintBoard();
        //alert("REstarted");
    };

    this.isEmpty = function (xBoard, oBoard, bit) {
        return (((xBoard & bit) == 0) && ((oBoard & bit) == 0));
    };


    this.markBit = function (markBit, player) {

        var bit = 1;
        var posX = 0, posY = 0;

        while ((markBit & bit) == 0) {
            bit = bit << 1;
            posX++;
            if (posX > 2) {
                posX = 0;
                posY++;
            }
        }

        if (player == 'O') {
            oBoard = oBoard | bit;
            this.paintO(posX, posY);
        } else {
            xBoard = xBoard | bit;
            this.paintX(posX, posY);
        }
    };

};