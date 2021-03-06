package MKAgent.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class deals with moves on a Kalah board.
 */
public class Kalah implements Cloneable {
    /**
     * The board to play on.
     */
    private final Board board;
    private Side sideToMove;
    private boolean northMoved;
    private Side ourSide;

    public Kalah(int holes, int seeds) {
        this.board = new Board(holes, seeds);
        this.sideToMove = Side.SOUTH;
        this.northMoved = false;
        this.ourSide = Side.SOUTH;
    }

    /**
     * @param board The board to play on.
     * @throws NullPointerException if "board" is null.
     */
    public Kalah(Board board, Side sideToMove, boolean north_moved, Side ourSide) throws NullPointerException {
        if (board == null)
            throw new NullPointerException();
        this.board = board;
        this.sideToMove = sideToMove;
        this.northMoved = north_moved;
        this.ourSide = ourSide;
    }

    public Kalah(Kalah other) throws NullPointerException, CloneNotSupportedException {
        if (other == null) {
            throw new NullPointerException();
        }
        this.board = other.getBoard().clone();
        this.sideToMove = other.getSideToMove();
        this.northMoved = other.northMoved;
        this.ourSide = other.getOurSide();
    }

    public Kalah clone() {
        try {
            return new Kalah(this);
        } catch (CloneNotSupportedException e) {
            // This should not happen
            System.err.print("Cloning of kalah failed");
            return null;
        }
    }

    public void setOurSide(Side ourSide) {
        this.ourSide = ourSide;
    }

    public List<Move> getLegalMoves() {
        return Kalah.getLegalMoves(this.board, this.sideToMove, this.northMoved);
    }

    /**
     * @return The board this object operates on.
     */
    public Board getBoard() {
        return board;
    }


    /**
     * @return the side that represent our point of view
     */
    public Side getOurSide() {
        return ourSide;
    }


    /**
     * Checks whether a given move is legal on the underlying board. The move
     * is not actually made.
     *
     * @param move The move to check.
     * @return true if the move is legal, false if not.
     */
    public boolean isLegalMove(Move move) {
        return isLegalMove(this.board, move, this.northMoved);
    }

    /**
     * Performs a move on the underlying board. The move must be legal. If
     * the move terminates the game, the remaining seeds of the opponent are
     * collected into their store as well (so that all holes are empty).<BR>
     * The "notifyObservers()" method of the board is called with the "move"
     * as argument.
     *
     * @param move The move to make.
     * @return The side who's turn it is after the move. Arbitrary if the
     * game is over.
     * @see #isLegalMove(Move)
     * @see #gameOver()
     * @see java.util.Observable#notifyObservers(Object)
     */
    public Side makeMove(Move move) {
        if (isLegalMove(board, move, this.northMoved)) {
            this.sideToMove = makeMove(board, move, this.northMoved);
            if (move.getIndex() == 0) {

                this.ourSide = this.ourSide.opposite();
            }
            if (move.getSide() == Side.NORTH) {
                this.northMoved = true;
            }
            return this.sideToMove;
        } else {
            throw new IllegalArgumentException("Illegal move attempted");
        }
    }

    public Side makeMove(int moveIndex) {
        return makeMove(new Move(this.sideToMove, moveIndex));
    }

    /**
     * Checks whether the game is over (based on the board).
     *
     * @return "true" if the game is over, "false" otherwise.
     */
    public boolean gameOver() {
        return gameOver(board);
    }

    /**
     * Checks whether a given move is legal on a given board. The move
     * is not actually made.
     *
     * @param board The board to check the move for.
     * @param move  The move to check.
     * @return true if the move is legal, false if not.
     */
    public static boolean isLegalMove(Board board, Move move, boolean northMoved) {
        if (move.getIndex() == 0) {
            return !northMoved && move.getSide() == Side.NORTH;
        }
        // check if the hole is existent and non-empty:
        return (move.getIndex() <= board.getNoOfHoles())
                && (board.getSeeds(move.getSide(), move.getIndex()) != 0);
    }

    public Side getSideToMove() {
        return this.sideToMove;
    }

    /**
     * Performs a move on a given board. The move must be legal. If
     * the move terminates the game, the remaining seeds of the opponent are
     * collected into their store as well (so that all holes are empty).<BR>
     * The "notifyObservers()" method of the board is called with the "move"
     * as argument.
     *
     * @param board The board to make the move on.
     * @param move  The move to make.
     * @return The side who's turn it is after the move. Arbitrary if the
     * game is over.
     * @see #isLegalMove(Board, Move, boolean)
     * @see #gameOver(Board)
     * @see java.util.Observable#notifyObservers(Object)
     */
    public static Side makeMove(Board board, Move move, boolean northMoved) {
        /* from the documentation:
		  "1. The counters are lifted from this hole and sown in anti-clockwise direction, starting
		      with the next hole. The player's own kalahah is included in the sowing, but the
		      opponent's kalahah is skipped.
		   2. outcome:
		    	1. if the last counter is put into the player's kalahah, the player is allowed to
		    	   move again (such a move is called a Kalah-move);
		    	2. if the last counter is put in an empty hole on the player's side of the board
		    	   and the opposite hole is non-empty,
		    	   a capture takes place: all stones in the opposite opponents pit and the last
		    	   stone of the sowing are put into the player's store and the turn is over;
		    	3. if the last counter is put anywhere else, the turn is over directly.
		   3. game end:
		    	The game ends whenever a move leaves no counters on one player's side, in
		    	which case the other player captures all remaining counters. The player who
		    	collects the most counters is the winner."
		*/
        // This is a move to swap sides
        if (move.getIndex() == 0) {
            int seedsInSouthStore = board.getSeedsInStore(Side.SOUTH);
            int seedsInNorthStore = board.getSeedsInStore(Side.NORTH);
            board.setSeedsInStore(Side.SOUTH, seedsInNorthStore);
            board.setSeedsInStore(Side.NORTH, seedsInSouthStore);
            for (int i = 1; i <= board.getNoOfHoles(); i++) {
                int tmp = board.getSeeds(Side.SOUTH, i);
                board.setSeeds(Side.SOUTH, i, board.getSeeds(Side.NORTH, i));
                board.setSeeds(Side.NORTH, i, tmp);
            }
            return move.getSide().opposite();
        }


        // pick seeds:
        int seedsToSow = board.getSeeds(move.getSide(), move.getIndex());
        board.setSeeds(move.getSide(), move.getIndex(), 0);

        int holes = board.getNoOfHoles();
        int receivingPits = 2 * holes + 1;  // sow into: all holes + 1 store
        int rounds = seedsToSow / receivingPits;  // sowing rounds
        int extra = seedsToSow % receivingPits;  // seeds for the last partial round
    	/* the first "extra" number of holes get "rounds"+1 seeds, the
    	   remaining ones get "rounds" seeds */

        // sow the seeds of the full rounds (if any):
        if (rounds != 0) {
            for (int hole = 1; hole <= holes; hole++) {
                board.addSeeds(Side.NORTH, hole, rounds);
                board.addSeeds(Side.SOUTH, hole, rounds);
            }
            board.addSeedsToStore(move.getSide(), rounds);
        }

        // sow the extra seeds (last round):
        Side sowSide = move.getSide();
        int sowHole = move.getIndex();  // 0 means store
        for (; extra > 0; extra--) {
            // go to next pit:
            sowHole++;
            if (sowHole == 1)  // last pit was a store
                sowSide = sowSide.opposite();
            if (sowHole > holes) {
                if (sowSide == move.getSide()) {
                    sowHole = 0;  // sow to the store now
                    board.addSeedsToStore(sowSide, 1);
                    continue;
                } else {
                    sowSide = sowSide.opposite();
                    sowHole = 1;
                }
            }
            // sow to hole:
            board.addSeeds(sowSide, sowHole, 1);
        }

        // capture:
        if ((sowSide == move.getSide())  // last seed was sown on the moving player's side ...
                && (sowHole > 0)  // ... not into the store ...
                && (board.getSeeds(sowSide, sowHole) == 1)  // ... but into an empty hole (so now there's 1 seed) ...
                && (board.getSeedsOp(sowSide, sowHole) > 0))  // ... and the opposite hole is non-empty
        {
            board.addSeedsToStore(move.getSide(), 1 + board.getSeedsOp(move.getSide(), sowHole));
            board.setSeeds(move.getSide(), sowHole, 0);
            board.setSeedsOp(move.getSide(), sowHole, 0);
        }

        // game over?
        Side finishedSide = null;
        if (holesEmpty(board, move.getSide()))
            finishedSide = move.getSide();
        else if (holesEmpty(board, move.getSide().opposite()))
            finishedSide = move.getSide().opposite();
    		/* note: it is possible that both sides are finished, but then
    		   there are no seeds to collect anyway */
        if (finishedSide != null) {
            // collect the remaining seeds:
            int seeds = 0;
            Side collectingSide = finishedSide.opposite();
            for (int hole = 1; hole <= holes; hole++) {
                seeds += board.getSeeds(collectingSide, hole);
                board.setSeeds(collectingSide, hole, 0);
            }
            board.addSeedsToStore(collectingSide, seeds);
        }

        board.notifyObservers(move);

        // who's turn is it?
        if (sowHole == 0 && (move.getSide() == Side.NORTH || northMoved))  // the store (implies (sowSide == move.getSide()))
            return move.getSide();  // move again
        else
            return move.getSide().opposite();
    }

    /**
     * Checks whether all holes on a given side are empty.
     *
     * @param board The board to check.
     * @param side  The side to check.
     * @return "true" iff all holes on side "side" are empty.
     */
    protected static boolean holesEmpty(Board board, Side side) {
        for (int hole = 1; hole <= board.getNoOfHoles(); hole++)
            if (board.getSeeds(side, hole) != 0)
                return false;
        return true;
    }

    /**
     * Checks whether the game is over (based on the board).
     *
     * @param board The board to check the game state for.
     * @return "true" if the game is over, "false" otherwise.
     */
    public static boolean gameOver(Board board) {
        // The game is over if one of the agents can't make another move.

        return holesEmpty(board, Side.NORTH) || holesEmpty(board, Side.SOUTH);
    }

    private static List<Move> getLegalMoves(Board board, Side side, boolean northMoved) {
        List<Move> legal_moves = new ArrayList<>();
        for (int index = 0; index <= board.getNoOfHoles(); index++) {
            Move move = new Move(side, index);
            if (Kalah.isLegalMove(board, move, northMoved))
                legal_moves.add(move);
        }
        return legal_moves;
    }

    public List<MoveStatePair> getNextMoveStatePairs() {
        List<MoveStatePair> moveStatePairs = new ArrayList<>();
        List<Move> legalMoves = this.getLegalMoves();
        for (Move move : legalMoves) {
            Kalah newState = this.clone();
            Side nextSide = newState.makeMove(move);
//            double value = Evaluation.evaluateState(newState);
//            if (nextSide != ourSide) {
//                value *= -1.0D;
//            }
            moveStatePairs.add(new MoveStatePair(move, newState, 0));
        }
        return moveStatePairs;
    }

    public static class MoveStatePair {
        private final Move move;
        private final Kalah state;

        private final double value;

        MoveStatePair(Move move, Kalah state, double value) {
            this.state = state;
            this.move = move;
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public Move getMove() {
            return this.move;
        }

        public Kalah getState() {
            return this.state;
        }
    }

    @Override
    public boolean equals(Object anotherState) {
        if (this == anotherState) return true;
        if (anotherState == null || getClass() != anotherState.getClass()) return false;
        Kalah anotherKalahState = (Kalah) anotherState;
        return northMoved == anotherKalahState.northMoved &&
                board.equals(anotherKalahState.board) &&
                sideToMove.equals(anotherKalahState.sideToMove) &&
                ourSide == anotherKalahState.ourSide;
    }

    @Override
    public int hashCode() {

        return Objects.hash(board, sideToMove, northMoved, ourSide);
    }
}

