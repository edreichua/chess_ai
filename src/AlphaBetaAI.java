import chesspresso.position.Position;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by edreichua on 2/1/16.
 */
public class AlphaBetaAI implements ChessAI {

    private final static boolean transpose = false;
    private int nodesExplored = 0;
    private int depthExplored = 0;
    private final static int WIN_UTILITY = Integer.MAX_VALUE;
    private final static int LOSS_UTILITY = Integer.MIN_VALUE;
    private final static int DRAW_UTILITY = 0;
    private final static int MAX_DEPTH = 7;
    private static int CURR_DEPTH = 1;
    private static int aiPlayer;
    private Map<Long,tableNode> transposition = new HashMap<>();

    private class tableNode{
        protected int score;
        protected int depth;
        protected int type; // 0 is exact, 1 is upper bound and -1 is lower bound
        public tableNode(int score, int depth, int type){
            this.score = score;
            this.depth = depth;
            this.type = type;
        }
    }

    // create wrapper class so alphaBetaSearch can return two variables
    private class node{
        protected short move;
        protected int utility;

        public node(short move, int utility){
            this.move = move;
            this.utility = utility;
        }

        public node(){
            this.move = Short.MIN_VALUE;
            this.utility = LOSS_UTILITY;
        }
    }

    public short getMove(Position position) {
        aiPlayer = position.getToPlay();
        node bestMove = new node();

        for(int i=0; i<MAX_DEPTH; i++){

            try {
                CURR_DEPTH = i;
                bestMove = alphaBetaSearch(position);

                printStats();
                resetStats();

                // short circuit for best move found
                if(bestMove.utility == WIN_UTILITY){
                    System.out.println("Utilty function yield score: "+bestMove.utility);
                    return bestMove.move;
                }

            }catch(Exception e){
                e.printStackTrace();
            }

        }
        System.out.println("Utility function yield score: "+bestMove.utility);
        return bestMove.move;
    }

    // alpha beta search algorithm (pp 170 of book)
    private node alphaBetaSearch(Position pos) throws Exception{

        short bestMove = Short.MIN_VALUE;
        int bestMoveUtility = LOSS_UTILITY;

        for(short move: pos.getAllMoves()){
            pos.doMove(move);
            int currMoveUtility = minValue(pos, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if(currMoveUtility > bestMoveUtility){
                bestMoveUtility = currMoveUtility;
                bestMove = move;
            }
            pos.undoMove();
        }
        return new node(bestMove,bestMoveUtility);
    }

    // calculate min value for alpha beta algorithm (pp 170 of book)
    private int minValue(Position pos, int depth, int alpha, int beta) throws Exception {

        int min = WIN_UTILITY;
        incrementNodeCount();
        updateDepth(depth);

        if(transpose && transposition.containsKey(pos.getHashCode()) && transposition.get(pos.getHashCode()).depth > CURR_DEPTH){
            int score = transposition.get(pos.getHashCode()).score;
            if(transposition.get(pos.getHashCode()).type == 0){
                return score;
            }else if(transposition.get(pos.getHashCode()).type < 0 && score >= beta){
                return score;
            }else if(transposition.get(pos.getHashCode()).type > 0 && score <= alpha){
                return score;
            }else if(beta <= alpha){
                return score;
            }
        }
        if(cutoffTest(pos,depth)){
            int score = utilityFun(pos);
            if(transpose && score <= alpha){
                transposition.put(pos.getHashCode(),new tableNode(score,depth,1));
            }else if(transpose && score >= beta){
                transposition.put(pos.getHashCode(),new tableNode(score,depth,-1));
            }else if(transpose){
                transposition.put(pos.getHashCode(),new tableNode(score,depth,0));
            }
            return score;
        }

        for(short move: pos.getAllMoves()){
            pos.doMove(move);
            min = Math.min(min,maxValue(pos,depth+1,alpha,beta));
            pos.undoMove();

            //pruning
            if(min <= alpha){
                return min;
            }
            beta = Math.min(beta,min);

        }
        return min;
    }

    // calculate max value for alpha beta algorithm (pp 166 of book)
    private int maxValue(Position pos, int depth, int alpha, int beta) throws Exception{

        int max = LOSS_UTILITY;
        incrementNodeCount();
        updateDepth(depth);

        if(transpose && transposition.containsKey(pos.getHashCode()) && transposition.get(pos.getHashCode()).depth > CURR_DEPTH){
            int score = transposition.get(pos.getHashCode()).score;
            if(transposition.get(pos.getHashCode()).type == 0){
                return score;
            }else if(transposition.get(pos.getHashCode()).type < 0 && score >= beta){
                return score;
            }else if(transposition.get(pos.getHashCode()).type > 0 && score <= alpha){
                return score;
            }else if(beta <= alpha){
                return score;
            }
        }
        if(cutoffTest(pos,depth)){
            int score = utilityFun(pos);
            if(transpose && score <= alpha){
                transposition.put(pos.getHashCode(),new tableNode(score,depth,1));
            }else if(transpose && score >= beta){
                transposition.put(pos.getHashCode(),new tableNode(score,depth,-1));
            }else if(transpose){
                transposition.put(pos.getHashCode(),new tableNode(score,depth,0));
            }
            return score;
        }

        for(short move: pos.getAllMoves()){
            pos.doMove(move);
            max = Math.max(max, minValue(pos, depth + 1, alpha, beta));
            pos.undoMove();

            //pruning
            if(max >= beta){
                return max;
            }
            alpha = Math.max(alpha, max);

        }
        return max;
    }


    // return the utility value of the terminating position
    private int utilityFun(Position pos){

        if(pos.isTerminal()){
            if(pos.isMate()) {
                if (pos.getToPlay() == aiPlayer){
                    return LOSS_UTILITY; // aiPlayer has lost
                }else{
                    return WIN_UTILITY; // aiPlayer won
                }
            }else{
                return DRAW_UTILITY; // either draw or stalemate
            }
        }else{
            return evalFun(pos);
        }
    }

    // evaluate the utility value if the game has not ended
    private int evalFun(Position pos){
        if(pos.getToPlay() == aiPlayer){
            return pos.getMaterial();
        }else{
            return -1*pos.getMaterial();
        }
    }

    // return true if game has ended or the maximum depth has been searched
    private boolean cutoffTest(Position pos, int depth){
        return (depth >= CURR_DEPTH || pos.isTerminal());
    }

    protected void resetStats() {
        nodesExplored = 0;
        depthExplored = 0;
    }

    protected void printStats() {
        System.out.println("Nodes explored during last search:  " + nodesExplored);
        System.out.println("Depth explored during last search " + depthExplored);
    }

    protected void updateDepth(int depth) {
        depthExplored = Math.max(depth, depthExplored);
    }

    protected void incrementNodeCount() {
        nodesExplored++;
    }



}