import unittest
from magent.mancala import MancalaEnv
from magent.board import Board
from magent.mcts.mcts import mcts_factory


class TestBoard(unittest.TestCase):

    def test_mcts_doesnt_mutate_state(self):
        state = MancalaEnv()
        initial_board = Board.clone(state.board)
        mcts = mcts_factory('test-mcts')
        mcts.search(state)

        self.assertEqual(initial_board.board, state.board.board, "Expect MCTS doesn't mutate the initial board")

    def test_mcts_generate_legal_move(self):
        state = MancalaEnv()
        mcts = mcts_factory('test-mcts')
        move = mcts.search(state)

        self.assertTrue(state.is_legal(move), "Expect move generated by MCTS is legal move")