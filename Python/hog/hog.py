"""CS 61A Presents The Game of Hog."""

from dice import four_sided, six_sided, make_test_dice
from ucb import main, trace, log_current_line, interact

GOAL_SCORE = 100  # The goal of Hog is to score 100 points.


######################
# Phase 1: Simulator #
######################

def roll_dice(num_rolls, dice=six_sided):
    """Simulate rolling the DICE exactly NUM_ROLLS>0 times. Return the sum of
    the outcomes unless any of the outcomes is 1. In that case, return the
    number of 1's rolled.
    """
    # These assert statements ensure that num_rolls is a positive integer.
    assert type(num_rolls) == int, 'num_rolls must be an integer.'
    assert num_rolls > 0, 'Must roll at least once.'
    # BEGIN PROBLEM 1
    k = 1
    score = 0
    pig_out = 0
    while k <= num_rolls: 
        m = dice()
        if m == 1:
            pig_out = pig_out + 1
            k = k + 1  
        else:
            score = score + m
            k = k + 1    
    if pig_out > 0:
        return pig_out
    else:
        return score
    
        
        
    # END PROBLEM 1


def free_bacon(opponent_score):
    """Return the points scored from rolling 0 dice (Free Bacon)."""
    # BEGIN PROBLEM 2
    if opponent_score % 10 > opponent_score // 10:
        return opponent_score % 10 + 1
    else:
        return opponent_score // 10 + 1
    # END PROBLEM 2


# Write your prime functions here!


def take_turn(num_rolls, opponent_score, dice=six_sided):
    """Simulate a turn rolling NUM_ROLLS dice, which may be 0 (Free Bacon).
    Return the points scored for the turn by the current player. Also
    implements the Hogtimus Prime and When Pigs Fly rules.

    num_rolls:       The number of dice rolls that will be made.
    opponent_score:  The total score of the opponent.
    dice:            A function of no args that returns an integer outcome.
    """
    # Leave these assert statements here; they help check for errors.
    assert type(num_rolls) == int, 'num_rolls must be an integer.'
    assert num_rolls >= 0, 'Cannot roll a negative number of dice in take_turn.'
    assert num_rolls <= 10, 'Cannot roll more than 10 dice.'
    assert opponent_score < 100, 'The game should be over.'
    # BEGIN PROBLEM 2
    if num_rolls == 0:
        score = free_bacon(opponent_score)
    else:
        score = roll_dice(num_rolls, dice)
    
    def is_prime(n):
        if n < 2:
            return False
        k = 2
        while k < n:
            if n % k == 0:
                return False
            else:
                k = k + 1
        return True
    
    def next_prime(n):
        k = 1
        while not is_prime(n + k):
            k = k + 1
        return n + k
    
    if is_prime(score):
        score = next_prime(score)
    else:
        score = score  
    
    if score > 25 - num_rolls:
        return 25 - num_rolls
    else:
        return score
    
    
    # END PROBLEM 2


def reroll(dice):
    """Return dice that return even outcomes and reroll odd outcomes of DICE."""
    def rerolled():
        # BEGIN PROBLEM 3
        m = dice()
        if m % 2 == 0:
            return m
        return dice()
        # END PROBLEM 3
    return rerolled


def select_dice(score, opponent_score, dice_swapped):
    """Return the dice used for a turn, which may be re-rolled (Hog Wild) and/or
    swapped for four-sided dice (Pork Chop).

    DICE_SWAPPED is True if and only if four-sided dice are being used.
    """
    # BEGIN PROBLEM 4
    if dice_swapped:
        dice = four_sided
    else:
        dice = six_sided
    
    # END PROBLEM 4
    if (score + opponent_score) % 7 == 0:
        dice = reroll(dice)
    return dice


def other(player):
    """Return the other player, for a player PLAYER numbered 0 or 1.

    >>> other(0)
    1
    >>> other(1)
    0
    """
    return 1 - player


def play(strategy0, strategy1, score0=0, score1=0, goal=GOAL_SCORE):
    """Simulate a game and return the final scores of both players, with
    Player 0's score first, and Player 1's score second.

    A strategy is a function that takes two total scores as arguments
    (the current player's score, and the opponent's score), and returns a
    number of dice that the current player will roll this turn.

    strategy0:  The strategy function for Player 0, who plays first
    strategy1:  The strategy function for Player 1, who plays second
    score0   :  The starting score for Player 0
    score1   :  The starting score for Player 1
    """
    player = 0  # Which player is about to take a turn, 0 (first) or 1 (second)
    dice_swapped = False  # Whether 4-sided dice have been swapped for 6-sided
    # BEGIN PROBLEM 5
    
    while score0 < goal and score1 < goal:
        
        
        if player == 0:
            if strategy0(score0, score1) == -1:
                if dice_swapped:
                    dice = six_sided
                    score0 = score0 + 1
                    dice_swapped = False
                else:
                    dice = four_sided
                    score0 = score0 + 1
                    dice_swapped = True
            else:
                dice = select_dice(score0, score1, dice_swapped)
                score0 = score0 + take_turn(strategy0(score0, score1), score1, dice)
            player = other(player)
       
        else:  
             
            if strategy1(score1, score0) == -1:
                if dice_swapped:
                    dice = six_sided
                    score1 = score1 + 1
                    dice_swapped = False
                else:
                    dice = four_sided
                    score1 = score1 + 1
                    dice_swapped = True
            
            else: 
                dice2 = select_dice(score1, score0, dice_swapped)     
                score1 = score1 + take_turn(strategy1(score1, score0), score0, dice2)
            player = other(player)
        if score0 == 2 * score1 and score0 != 0 and score1 != 0:
                score0, score1 = score1, 2 * score1
                
        elif score1 == score0 * 2 and score0 != 0 and score1 != 0:
                score0, score1 = 2 * score0, score0
                 
                 
    # END PROBLEM 5
    return score0, score1 

#######################
# Phase 2: Strategies #
#######################

def always_roll(n):
    """Return a strategy that always rolls N dice.

    A strategy is a function that takes two total scores as arguments
    (the current player's score, and the opponent's score), and returns a
    number of dice that the current player will roll this turn.

    >>> strategy = always_roll(5)
    >>> strategy(0, 0)
    5
    >>> strategy(99, 99)
    5
    """
    def strategy(score, opponent_score):
        return n
    return strategy


def check_strategy_roll(score, opponent_score, num_rolls):
    """Raises an error with a helpful message if NUM_ROLLS is an invalid
    strategy output. All strategy outputs must be integers from -1 to 10.

    >>> check_strategy_roll(10, 20, num_rolls=100)
    Traceback (most recent call last):
     ...
    AssertionError: strategy(10, 20) returned 100 (invalid number of rolls)

    >>> check_strategy_roll(20, 10, num_rolls=0.1)
    Traceback (most recent call last):
     ...
    AssertionError: strategy(20, 10) returned 0.1 (not an integer)

    >>> check_strategy_roll(0, 0, num_rolls=None)
    Traceback (most recent call last):
     ...
    AssertionError: strategy(0, 0) returned None (not an integer)
    """
    msg = 'strategy({}, {}) returned {}'.format(
        score, opponent_score, num_rolls)
    assert type(num_rolls) == int, msg + ' (not an integer)'
    assert -1 <= num_rolls <= 10, msg + ' (invalid number of rolls)'


def check_strategy(strategy, goal=GOAL_SCORE):
    """Checks the strategy with all valid inputs and verifies that the
    strategy returns a valid input. Use `check_strategy_roll` to raise
    an error with a helpful message if the strategy returns an invalid
    output.

    >>> def fail_15_20(score, opponent_score):
    ...     if score != 15 or opponent_score != 20:
    ...         return 5
    ...
    >>> check_strategy(fail_15_20)
    Traceback (most recent call last):
     ...
    AssertionError: strategy(15, 20) returned None (not an integer)
    >>> def fail_102_115(score, opponent_score):
    ...     if score == 102 and opponent_score == 115:
    ...         return 100
    ...     return 5
    ...
    >>> check_strategy(fail_102_115)
    >>> fail_102_115 == check_strategy(fail_102_115, 120)
    Traceback (most recent call last):
     ...
    AssertionError: strategy(102, 115) returned 100 (invalid number of rolls)
    """
    # BEGIN PROBLEM 6
    k = 0 
    m = 0
    while k <= goal: 
        check_strategy_roll(k,m,strategy(k, m))
        while m <= goal:
            check_strategy_roll(k,m,strategy(k, m))
            m = m + 1
        m = 0    
        k = k + 1
    return None
    # END PROBLEM 6


# Experiments

def make_averaged(fn, num_samples=1000):
    """Return a function that returns the average_value of FN when called.

    To implement this function, you will have to use *args syntax, a new Python
    feature introduced in this project.  See the project description.

    >>> dice = make_test_dice(3, 1, 5, 6)
    >>> averaged_dice = make_averaged(dice, 1000)
    >>> averaged_dice()
    3.75
    """
    # BEGIN PROBLEM 7
    def result_score(*args):
        
        k = 1
        m = 0
        while k <= num_samples:
            m = m + fn(*args)
            k = k + 1
        return m / num_samples 
    
    return result_score
    # END PROBLEM 7


def max_scoring_num_rolls(dice=six_sided, num_samples=1000):
    """Return the number of dice (1 to 10) that gives the highest average turn
    score by calling roll_dice with the provided DICE over NUM_SAMPLES times.
    Assume that the dice always return positive outcomes.

    >>> dice = make_test_dice(3)
    >>> max_scoring_num_rolls(dice)
    10
    """
    # BEGIN PROBLEM 8
    a = 1
    max_score = 0
    while a <= 10:
        s = make_averaged(roll_dice, num_samples)(a, dice)
        if max_score < s:
            max_score = s
            max_roll = a
        a = a + 1
        
    return max_roll
            
         
    
        
   
    # END PROBLEM 8


def winner(strategy0, strategy1):
    """Return 0 if strategy0 wins against strategy1, and 1 otherwise."""
    score0, score1 = play(strategy0, strategy1)
    if score0 > score1:
        return 0
    else:
        return 1


def average_win_rate(strategy, baseline=always_roll(4)):
    """Return the average win rate of STRATEGY against BASELINE. Averages the
    winrate when starting the game as player 0 and as player 1.
    """
    win_rate_as_player_0 = 1 - make_averaged(winner)(strategy, baseline)
    win_rate_as_player_1 = make_averaged(winner)(baseline, strategy)

    return (win_rate_as_player_0 + win_rate_as_player_1) / 2


def run_experiments():
    """Run a series of strategy experiments and report results."""
    if True:  # Change to False when done finding max_scoring_num_rolls
        six_sided_max = max_scoring_num_rolls(six_sided)
        print('Max scoring num rolls for six-sided dice:', six_sided_max)
        rerolled_max = max_scoring_num_rolls(reroll(six_sided))
        print('Max scoring num rolls for re-rolled dice:', rerolled_max)

    if False:  # Change to True to test always_roll(8)
        print('always_roll(8) win rate:', average_win_rate(always_roll(8)))

    if False:  # Change to True to test bacon_strategy
        print('bacon_strategy win rate:', average_win_rate(bacon_strategy))

    if False:  # Change to True to test swap_strategy
        print('swap_strategy win rate:', average_win_rate(swap_strategy))

    "*** You may add additional experiments as you wish ***"


# Strategies

def bacon_strategy(score, opponent_score, margin=8, num_rolls=4):
    """This strategy rolls 0 dice if that gives at least MARGIN points,
    and rolls NUM_ROLLS otherwise.
    """
    # BEGIN PROBLEM 9
    s = free_bacon(opponent_score) 
    def is_prime(n):
        if n < 2:
            return False
        k = 2
        while k < n:
            if n % k == 0:
                return False
            else:
                k = k + 1
        return True
    
    def next_prime(n):
        k = 1
        while not is_prime(n + k):
            k = k + 1
        return n + k
    
    if is_prime(s):
        s = next_prime(s)
    
    if s >= margin:
        return 0
    return num_rolls  # Replace this statement
    # END PROBLEM 9
check_strategy(bacon_strategy)


def swap_strategy(score, opponent_score, margin=8, num_rolls=4):
    """This strategy rolls 0 dice when it triggers a beneficial swap. It also
    rolls 0 dice if it gives at least MARGIN points. Otherwise, it rolls
    NUM_ROLLS.
    """
    # BEGIN PROBLEM 10
    s = free_bacon(opponent_score) 
    def is_prime(n):
        if n < 2:
            return False
        k = 2
        while k < n:
            if n % k == 0:
                return False
            else:
                k = k + 1
        return True
    
    def next_prime(n):
        k = 1
        while not is_prime(n + k):
            k = k + 1
        return n + k
    
    if is_prime(s):
        s = next_prime(s)
    if (s + score) * 2 == opponent_score:
        return 0
    elif bacon_strategy(score, opponent_score, margin=8, num_rolls=4) == 0:
        return 0
    
    return num_rolls  # Replace this statement
    # END PROBLEM 10
check_strategy(swap_strategy)


def final_strategy(score, opponent_score):
    """Write a brief description of your final strategy.

    *** YOUR DESCRIPTION HERE ***
    """
    # BEGIN PROBLEM 11
    
    # PORK CHOP
    if score == 0:
        return -1
    
    #HOG WILD
    if (score + opponent_score) % 7 == 0:
        return swap_strategy(score, opponent_score, 12, 6)
 
           
    #SWAP
    if swap_strategy(score, opponent_score, 1, 5) == 0:
        return 0
    if 2 * (score + 1) == opponent_score:
        return swap_strategy(score, opponent_score, 10, 4)
    
    if opponent_score - 2 * score == 2:
        return -1 
    # IN LEAD
    if score > opponent_score > 13:
        return 0
   
    # IN LOSE
    if opponent_score - score > 10:
        return 0
    
    return 4
    

         # Replace this statement
    # END PROBLEM 11
check_strategy(final_strategy)


##########################
# Command Line Interface #
##########################

# NOTE: Functions in this section do not need to be changed. They use features
# of Python not yet covered in the course.

@main
def run(*args):
    """Read in the command-line argument and calls corresponding functions.

    This function uses Python syntax/techniques not yet covered in this course.
    """
    import argparse
    parser = argparse.ArgumentParser(description="Play Hog")
    parser.add_argument('--run_experiments', '-r', action='store_true',
                        help='Runs strategy experiments')

    args = parser.parse_args()

    if args.run_experiments:
        run_experiments()