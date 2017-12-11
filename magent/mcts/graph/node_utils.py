from math import sqrt, log
from magent.mcts.graph.node import Node


# select_best_child returns the child that maximise upper confidence interval (UCT applied to trees)
def select_best_child(node: Node) -> Node:
    if node.is_terminal():
        raise ValueError('Terminal node; there are no children to select from.')
    elif len(node.children) == 1:
        return node.children[0]

    return max(node.children, key=lambda child: _uct_reward(node, child))


# select_max_child returns the child with highest average reward
def select_max_child(node: Node) -> Node:
    if node.is_terminal():
        raise ValueError('Terminal node; there are no children to select from.')
    if len(node.children) == 0:
        raise ValueError('Selecting max child from unexpanded node')
    elif len(node.children) == 1:
        return node.children[0]
    return max(node.children, key=lambda child: child.reward / child.visits)


# select_robust_child returns the child that is most visited
def select_robust_child(node: Node) -> Node:
    if node.is_terminal():
        raise ValueError('Terminal node; there are no children to select from.')
    elif len(node.children) == 1:
        return node.children[0]
    return max(node.children, key=lambda child: child.visits)


# select_secure_child returns child which maximises a lower confidence interval (LCT applied to trees)
def select_secure_child(node: Node) -> Node:
    if node.is_terminal():
        raise ValueError('Terminal node; there are no children to select from.')
    elif len(node.children) == 1:
        return node.children[0]

    return max(node.children, key=lambda child: _lower_confidence_interval(node, child))


def _uct_reward(root: Node, child: Node, exploration_constant: float = 1 / sqrt(2)) -> float:
    return (child.reward / child.visits) + (exploration_constant * sqrt(2 * log(root.visits) / child.visits))


def _lower_confidence_interval(root: Node, child: Node, exploration_constant: float = 1 / sqrt(2)) -> float:
    return (child.reward / child.visits) - (exploration_constant * sqrt(2 * log(root.visits) / child.visits))
