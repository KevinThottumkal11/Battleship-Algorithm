# Battleship Strategy Algorithm

## Project Overview
This project implements an AI-driven strategy for playing the classic Battleship game. The implementation uses probability-based targeting algorithms to efficiently locate and sink ships with a minimal number of shots.

## Performance Metrics
- **Average shots required:** 67.06 shots per game
- **Test scale:** 10,000 game simulations
- **Execution time:** 3,623 milliseconds (under 4 seconds)

## Game Configuration
- **Board size:** 12×12 grid
- **Ships:** 6 ships with sizes [5, 5, 4, 4, 2, 2]
- **Ship placement rule:** Ships cannot touch horizontally or vertically (diagonally is allowed)

## Algorithm Features

### Probability-Based Targeting
The algorithm calculates probability values for each cell on the grid by:
- Tracking possible ship placements for each remaining ship
- Updating probabilities after each shot
- Prioritizing cells with the highest likelihood of containing a ship

### Parity-Based Search Strategy
- Implements an optimization that recognizes large ships (size ≥ 4) must occupy both even and odd parity cells
- Assigns higher priority to even-parity cells when large ships remain
- Reduces the effective search space by focusing on a mathematical pattern in valid ship placements

### Directional Targeting Logic
Once a ship is hit, the algorithm:
1. Adds adjacent cells as high-priority targets
2. Determines ship orientation (horizontal/vertical) after a second hit
3. Focuses subsequent shots along the determined axis
4. Resets targeting data when a ship is completely sunk

### Data Structures
- **2D array:** Primary game grid representation
- **Cell class:** Encapsulates cell state and probability information
- **Stack:** Manages priority targets after hits
- **ArrayList:** Tracks consecutive hits on a single ship
- **Custom enums:** Represents cell states (Empty, Hit, Miss)

## Implementation Details

### Key Methods
- `fireShot()`: Main method that selects and executes each shot
- `selectTarget()`: Chooses the optimal target based on current game state
- `updateCellProbabilities()`: Recalculates probability values after each shot
- `manageShipHit()`: Handles targeting logic after successful hits
- `hasAdjacentShip()`: Enforces ship placement rules in probability calculations

### Optimization Techniques
- Efficient tracking of sunk ships to avoid redundant calculations
- Direction-aware targeting to minimize wasted shots
- Probabilistic analysis that adapts to the remaining unsunk ships

## How to Run
1. Ensure you have Java installed on your system
2. Compile the project: `javac A5.java`
3. Run the simulation: `java A5`

## Author
Kevin Binu Thottumkal
