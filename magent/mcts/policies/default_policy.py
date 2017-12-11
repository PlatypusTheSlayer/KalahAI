import logging
from random import choice

from magent.mcts.graph.node import AlphaNode, Node
from magent.move import Move
from magent.side import Side


class DefaultPolicy(object):
    """ DefaultPolicy plays out the domain from a given non-terminal state to produce a value estimate (simulation). """

    def simulate(self, root: Node, our_side: Side) -> float:
        """ simulate run the game from given node and saves the reward for taking actions. """
        raise NotImplementedError("Simulate method is not implemented")


class MonteCarloDefaultPolicy(DefaultPolicy):
    """MonteCarloDefaultPolicy plays the domain randomly from a given non-terminal state."""

    def simulate(self, root: Node, our_side: Side) -> float:
        node = Node.clone(root)
        while not node.is_terminal():
            legal_move = choice(node.state.get_legal_moves())
            node.state.perform_move(legal_move)

        return node.state.compute_end_game_reward(our_side)


class AlphaGoDefaultPolicy(DefaultPolicy):
    """plays the domain based on prior probability provided by a neuron network. Starting at non-terminal state."""

    def __init__(self, network):
        super(AlphaGoDefaultPolicy, self).__init__()
        self.network = network

    def simulate(self, root: AlphaNode, our_side: Side, lmbd=1) -> float:
        """
            runs a simulation from the root to the end of the game
            :param our_side: Our side in the game
            :param root: the starting node for the simulation
            :param lmbd: a parameter to control the weight of the value network
            :return: the rollout policy; reward for taking this path combining value network with game's winner
        """
        node: AlphaNode = AlphaNode.clone(root)
        value = 0
        while not node.is_terminal():
            best_move, _, value = self.network.get_best_move(node.state)
            best_legal_move = Move(node.state.side_to_move, best_move)
            node.state.perform_move(best_legal_move)

        side_final_reward = node.state.compute_end_game_reward(our_side)
        reward = (1 - lmbd) * value + (lmbd * side_final_reward)
        logging.debug("Reward: %f; side final reward: %f; Value: %f" % (reward, side_final_reward, value))
        return reward  # (move reward + value network reward)
