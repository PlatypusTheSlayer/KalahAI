from numpy.ma import sqrt

from magent.mancala import MancalaEnv
from magent.move import Move
from copy import deepcopy


# TODO(samialab@): Separate the Node class to MCTS and AlphaGo
class Node(object):
    def __init__(self, state: MancalaEnv, move: Move = None, parent=None):
        self.visits = 0
        self.reward = 0
        self.state = state
        self.children = []
        self.parent = parent
        self.move = move
        self.unexplored_moves = set(state.get_legal_moves())

    @staticmethod
    def clone(other_node):
        return deepcopy(other_node)

    def put_child(self, child):
        self.children.append(child)
        self.unexplored_moves.remove(child.move)

    def update(self, reward):
        self.reward += reward
        self.visits += 1

    # is_fully_expanded returns true if there are no more moves to explore
    def is_fully_expanded(self) -> bool:
        return len(self.unexplored_moves) == 0

    # is_terminal returns true if the node is leaf node
    def is_terminal(self) -> bool:
        return len(self.state.get_legal_moves()) == 0

    # backpropgate pushes the reward (pay/visits) to the parents node up to the root
    def backpropagate(self, reward):
        parent = self.parent
        # propagate node reward to parents'
        while parent is not None:
            parent.update(reward)
            parent = parent.parent

    def __str__(self):
        return "Node; Move %s, number of children: %d; visits: %d; reward: %f" % (
            self.move, len(self.children), self.visits, self.reward)


class AlphaNode(Node):
    def __init__(self, state: MancalaEnv, prior, q=0, move: Move = None, parent=None):
        super(AlphaNode, self).__init__(state, move, parent)
        # u(s,a) - probational to prior probability but decays with repeated visits to encourage exploration.
        self.u = prior / (1 + self.visits)  # u(s,a) exploration bonus
        self.prior = prior  # P(s,a) prior probability
        self.q = q  # Q(s, a) - action

    def update(self, reward, c_puct=10):
        """
            :param reward: leaf reward
            :param c_puct: a constant determining the level of exploration (PUCT algorithm)
        """
        super(AlphaNode, self).update(reward)
        self.q += (reward - self.q) / self.visits
        if self.parent is not None:
            self.q += c_puct * self.prior * sqrt(self.parent.visits) / (1 + self.visits)

    # backpropgate pushes the reward (pay/visits) to the parents node up to the root
    def backpropagate(self, reward):
        parent = self.parent
        parents_path_stack = []
        # propagate node reward to parents'
        while parent is not None:
            parents_path_stack.append(parent)
            parent = parent.parent
        # Update from root downward so the exploration bonus calculation is correct
        while len(parents_path_stack) > 0:
            node = parents_path_stack.pop()
            node.update(reward)

    def calculate_action_value(self) -> float:
        return self.q + self.u

    def __str__(self):
        return "Node; Move %s, number of children: %d; visits: %d; reward: %f; Q: %f; U: %f; P: %f" % (
            self.move, len(self.children), self.visits, self.reward, self.q, self.u, self.prior)
