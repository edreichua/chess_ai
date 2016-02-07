import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import chesspresso.Chess;
import chesspresso.position.Position;

public class ChessClient extends Application {

	private static final int PIXELS_PER_SQUARE = 64;
	private static final String welcomeMessage = 
			"Welcome to CS 76 chess.  Moves can be made using algebraic notation;"
			+ " for example the command c2c3 would move the piece at c2 to c3.  \n";
		
	
	TextField commandField;
	TextArea logArea;
	
	BoardView boardView;
	ChessGame game;
	// RandomMoveSource[] playerMoveSources;

	MoveMaker[] moveMaker;

	public static void main(String[] args) {

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("CS 76 Chess");

		game = new ChessGame();

		// build the board
		boardView = new BoardView(game, PIXELS_PER_SQUARE);

		// build the text area for giving log info to user
		logArea = new TextArea();
	 	//logArea.setPrefColumnCount(50);
	 	logArea.setPrefRowCount(5);
	 	logArea.setEditable(false);
	 	logArea.setWrapText(true);
	 	log(welcomeMessage);
		
		// build the command entry text field
		commandField = new TextField();
		
		// request focus on the command field after the ui is built,
		//  to get a blinking cursor
		Platform.runLater(new Runnable() {
		    public void run() {
		        commandField.requestFocus();
		    }
		});

		// set up the movemakers for black and white players.
		// Movemakers handle getting input from an AI, from the keyboard, or
		// from a server, depending on which type is used.
		moveMaker = new MoveMaker[2];
		moveMaker[Chess.BLACK] = new AIMoveMaker(new AlphaBetaAI()); // to change
		moveMaker[Chess.WHITE] = new AIMoveMaker(new RandomAI()); // to change
		//moveMaker[Chess.WHITE] = new TextFieldMoveMaker();

		VBox vb = new VBox();
		vb.getChildren().addAll(boardView, logArea, commandField);
		vb.setSpacing(10);
		vb.setPadding(new Insets(10, 10, 10, 10));

		// add everything to a root stackpane, and then to the main window
		StackPane root = new StackPane();
		root.getChildren().add(vb);
		primaryStage.setScene(new Scene(root)); //, boardView.getPreferredWidth(), 600));
		primaryStage.show();

		// sets the game world's game loop (Timeline)
		Timeline timeline = new Timeline(1.0);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.seconds(.05), new GameHandler()));
		timeline.playFromStart();
		timeline.playFromStart();

		// moveMaker = new AIMoveMaker(new RandomAI());

	}

	private void log(String logText) {
		logArea.appendText(logText + "\n");
		
	}

	private void checkGame(Position pos){
		if(pos.isMate()){
			if(pos.getToPlay() == 0){
				log("Game Over: Black wins!");
			}else{
				log("Game Over: White wins!");
			}
		}else if(pos.isStaleMate() || pos.isTerminal()){
			log("Game Over: Black and White draws");
		}
	}
	
	// As time passes, handle the state of the game
	private class GameHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			// System.out.println("timer fired");

			// System.out.println(boardView.ready());
			// setting activeMoveSource to null will cause a new one to be
			// created:

			MoveMaker mover = moveMaker[game.position.getToPlay()];

			if (mover.getState() == State.READY) {
				mover.start(game.position);
			} else if (mover.getState() == State.SUCCEEDED
					&& boardView.ready()) {
				short move = mover.getMove();
				boardView.doMove(move);
				checkGame(game.position);
				mover.reset();
			}

			// System.out.println("activeMoveSource state " +
			// activeMoveSource.getState());

			// short move =
			// playerMoveSources[0].getMove(game.position.toString());
			// System.out.println(move);
			// boardView.doMove(move);
		}

	}

	private class TextFieldMoveMaker implements MoveMaker,
			EventHandler<ActionEvent> {

		private State state;
		short move;

		public TextFieldMoveMaker() {
			this.state = State.READY;
			commandField.setOnAction(this);
			move = 0;
		}

		@Override
		public void start(Position position) {
			//String[] players = {"WHITE", "BLACK"};
			//commandField.setPromptText("Your move," + players[position.getToPlay()] + ".");
		}

		@Override
		public void reset() {
			commandField.setText("");
			this.state = State.READY;

		}

		@Override
		public State getState() {
			return state;
		}

		@Override
		public short getMove() {
			return move;
		}

		@Override
		public void handle(ActionEvent e) {
			String text = commandField.getText();
			if (text != null & text != "") {
				int fromSqi = Chess.strToSqi(text.charAt(0), text.charAt(1));
				int toSqi = Chess.strToSqi(text.charAt(2), text.charAt(3));

				move = game.findMove(fromSqi, toSqi);
				this.state = State.SUCCEEDED;

			}

		}

	}

	private class AIMoveMaker implements MoveMaker {
		ChessAI ai;
		AIMoveTask moveTask;

		public AIMoveMaker(ChessAI ai) {
			super();
			this.ai = ai;
			this.moveTask = null;
		}

		public void start(Position position) {
			moveTask = new AIMoveTask(ai, position);
			new Thread(moveTask).start();
		}

		public State getState() {
			// short circuit if moveTask hasn't been initalized
			//  (threading bug fix by Yu-Han Lyu:)
			if (moveTask == null)
				return State.READY;
			if (moveTask.getState() == State.SUCCEEDED)
				return State.SUCCEEDED;
			return State.RUNNING;
		}

		public short getMove() {
			return moveTask.getValue();

		}

		public void reset() {
			this.moveTask = null;
		}

	
	}

	
}