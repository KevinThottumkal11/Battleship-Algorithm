import battleship.BattleShip3;
import battleship.BattleShipBot;

import java.awt.Point;
import java.util.*;

/**
 * This class is the implementation of a bot for the battleship game that will sink
 * all the ship in the game. The bot uses probability based method to hit targets
 * with minimum number of shots to enhance performance.
 * The BattleBot also contains a class representing each cell and
 * an enum to represent the cell states (same as BattleShip3 API CellState)
 *
 * *** BEST PERFORMANCE ***
 * The shot performance 10000 games    = 67.04
 * Time required to complete 10000 games = 3896 ms
 *
 * Kevin Binu Thottumkal, 000884769
 */
public class BattleBot implements BattleShipBot {
    private static final int GRIDSIZE = 12;
    private final Cell[][] gameGrid = new Cell[GRIDSIZE][GRIDSIZE];

    // Arraylist storing the coordinates of the hits on a ship
    private final ArrayList<Point> pointsHit = new ArrayList<>();

    // Stack that stores targets to hit
    private final Stack<Point> targetPoints = new Stack<>();

    // number of ships sunk
    private int shipsSunk = 0;

    // boolean to indicate the direction of the ship
    private boolean isShipHorizontal = false;

    // number of ships left
    private final int[] shipsLeft = new int[6];
    private final Random random = new Random();
    private BattleShip3 battleship;

    /**
     * Initializes the game and resets all the states related to the game
     * @param battleship battleship instance
     */
    @Override
    public void initialize(BattleShip3 battleship) {
        this.battleship = battleship;

        // Start game
        if (gameGrid[0][0] == null) {
            for (int x = 0; x < GRIDSIZE; x++) {
                for (int y = 0; y < GRIDSIZE; y++) {
                    gameGrid[x][y] = new Cell();
                }
            }
        } else {
            // Reset grid
            for (int x = 0; x < GRIDSIZE; x++) {
                for (int y = 0; y < GRIDSIZE; y++) {
                    gameGrid[x][y].reset();
                }
            }
        }

        // getting ship sizes
        System.arraycopy(battleship.getShipSizes(), 0, shipsLeft, 0, battleship.getShipSizes().length);

        // resetting everything for a new game
        pointsHit.clear();
        targetPoints.clear();
        shipsSunk = 0;
    }

    /**
     * this main method that shoots at the target based on the probabilities and
     * handles if there is a successful hit on a ship
     */
    @Override
    public void fireShot() {
        Point shot = selectTarget();
        boolean hit = battleship.shoot(shot);
        gameGrid[shot.x][shot.y].state = hit ? CellState.Hit : CellState.Miss;

        // Successful hit on a ship
        if (hit) {
            manageShipHit(shot);
        }
    }

    /**
     * Method to return the name of the author
     * @return Name of the author
     */
    @Override
    public String getAuthors() {
        return "Kevin Binu Thottumkal";
    }

    /**
     * This method manages a successful hit on a ship.
     * It determines if the ship is sunk completely or else
     * look for the adjacent positions for the remaining part of the ship
     * @param shot Coordinates of the hit on the ship
     */
    private void manageShipHit(Point shot) {
        pointsHit.add(shot);
        int numShipSunk = battleship.numberOfShipsSunk();

        if (numShipSunk > shipsSunk) {
            // Ship sunk completely
            int sunkLength = calculateShipLength();
            for (int i = 0; i < shipsLeft.length; i++) {
                if (shipsLeft[i] == sunkLength) {
                    shipsLeft[i] = 0;
                    break;
                }
            }
            // clearing the data to look for next ship
            pointsHit.clear();
            targetPoints.clear();
            shipsSunk = numShipSunk;
        } else {
            // looking for adjacent points
            if (pointsHit.size() > 1) {
                // determining the ship direction if we have more than 1 hit
                Point prevHit = pointsHit.get(pointsHit.size() - 2);
                isShipHorizontal = prevHit.y == shot.y;
                addShipDirectionTargets(shot);
            } else {
                // shooting adjacent directions
                addAdjacentTargets(shot);
            }
        }
    }

    /**
     * Method that will select the targets from the target stack. If its empty it will select the
     * best possible target based on the cell probability which is
     * updated using updateCellProbabilities() and findBestTarget() methods
     *
     * @return coordinates of the target
     */
    private Point selectTarget() {
        if (!targetPoints.isEmpty()) {
            return targetPoints.pop();
        }

        // if target stack is empty
        updateCellProbabilities();
        return findBestTarget();
    }

    /**
     * This method is used to update the probability values of each cell in
     * the grid based on the remaining ship sized and cell states (empty cells)
     */
    private void updateCellProbabilities() {
        for (Cell[] row : gameGrid) {
            for (Cell cell : row) {
                cell.probability = 0;
            }
        }

        // Calculate probabilities only for active ships
        for (int shipSize : shipsLeft) {
            if (shipSize == 0) continue;    // ships already sunk
            for (int x = 0; x < GRIDSIZE; x++) {
                for (int y = 0; y < GRIDSIZE; y++) {
                    if (gameGrid[x][y].state == CellState.Empty) {
                        checkShipDirection(x, y, shipSize);
                    }
                }
            }
        }
    }

    /**
     * This method will check for the possible direction of a ship and update the
     * probability value of cells
     * @param x x coordinate of the ship
     * @param y y coordinate of the ship
     * @param size size of the ship
     */
    private void checkShipDirection(int x, int y, int size) {
        // Horizontal check
        boolean isHorizontal = x + size <= GRIDSIZE;
        if (isHorizontal) {
            boolean horizontal = true;
            for (int i = 0; i < size && horizontal; i++) {
                // looking at the cells on the horizontal directions for non-empty cells or other ships
                if (gameGrid[x + i][y].state != CellState.Empty || hasAdjacentShip(x + i, y)) {
                    horizontal = false;
                }
            }
            if (horizontal) {
                for (int i = 0; i < size; i++) {
                    gameGrid[x + i][y].probability++;
                }
            }
        }

        // Vertical check
        boolean isVertical = y + size <= GRIDSIZE;
        if (isVertical) {
            boolean vertical = true;
            for (int i = 0; i < size && vertical; i++) {
                // looking at the cells on the vertical directions for non-empty cells or other ships
                if (gameGrid[x][y + i].state != CellState.Empty || hasAdjacentShip(x, y + i)) {
                    vertical = false;
                }
            }
            if (vertical) {
                for (int i = 0; i < size; i++) {
                    gameGrid[x][y + i].probability++;
                }
            }
        }
    }

    /**
     * This method will look for the presence of a ship in adjacent directions
     * @param x x coordinate of the ship
     * @param y y coordinate of the ship
     * @return boolean values depends on the presence of a ship
     */
    private boolean hasAdjacentShip(int x, int y) {
        // left
        if (x > 0 && gameGrid[x-1][y].state == CellState.Hit) {
            return true;
        }
        // right
        else if (x < GRIDSIZE -1 && gameGrid[x+1][y].state == CellState.Hit) {
            return true;
        }
        // top
        else if (y > 0 && gameGrid[x][y-1].state == CellState.Hit) {
            return true;
        }
        // down
        else if (y < GRIDSIZE -1 && gameGrid[x][y+1].state == CellState.Hit) {
            return true;
        }

        return false;
    }

    /**
     * This method will determine using the parity method.
     * If large ships are present, it will occupy both odd and even cells.
     * We give more probability for even ships so there is a high chance we find
     * a ship. We will also shoot at the odd cells with less priority.
     * This helps to avoid unnecessary shots and gives us a high probability
     * for finding a ship
     *
     * @return coordinates of the best target
     */
    private Point findBestTarget() {
        Point best = null;
        int maxProb = -1;

        // Check if ships of size 4 or larger is present
        boolean hasLargeShips = false;
        for (int size : shipsLeft) {
            if (size >= 4) {
                hasLargeShips = true;
                break;
            }
        }

        // only if large ships are present
        if (hasLargeShips) {
            for (int x = 0; x < GRIDSIZE; x++) {
                for (int y = 0; y < GRIDSIZE; y++) {
                    if (gameGrid[x][y].state == CellState.Empty) {
                        int probability = gameGrid[x][y].probability;
                        if (hasLargeShips && (x + y) % 2 == 0) {
                            probability++;  // add probability to even cells
                        }
                        if (probability > maxProb) {
                            maxProb = probability;
                            best = new Point(x, y);
                        }
                    }
                }
            }
        }

        if (best == null) {
            return getRandomCell();
        }
        return best;
    }

    /**
     * This method will add targets in the direction of the ship
     * @param hit coordinates of the hit
     */
    private void addShipDirectionTargets(Point hit) {
        if (isShipHorizontal) {
            // horizontal targets (left and right)
            if (hit.x > 0 && gameGrid[hit.x-1][hit.y].state == CellState.Empty) {
                targetPoints.push(new Point(hit.x-1, hit.y));
            }
            if (hit.x < GRIDSIZE -1 && gameGrid[hit.x+1][hit.y].state == CellState.Empty) {
                targetPoints.push(new Point(hit.x+1, hit.y));
            }
        } else {
            // vertical targtes (up and down)
            if (hit.y > 0 && gameGrid[hit.x][hit.y-1].state == CellState.Empty) {
                targetPoints.push(new Point(hit.x, hit.y-1));
            }
            if (hit.y < GRIDSIZE -1 && gameGrid[hit.x][hit.y+1].state == CellState.Empty) {
                targetPoints.push(new Point(hit.x, hit.y+1));
            }
        }
    }

    /**
     * Add hit target to all the adjacent cells in horizontal and vertical direction
     * @param hit coordinates of the hit
     */
    private void addAdjacentTargets(Point hit) {
        if (hit.x > 0 && gameGrid[hit.x-1][hit.y].state == CellState.Empty) {
            targetPoints.push(new Point(hit.x-1, hit.y));
        }
        if (hit.x < GRIDSIZE -1 && gameGrid[hit.x+1][hit.y].state == CellState.Empty) {
            targetPoints.push(new Point(hit.x+1, hit.y));
        }
        if (hit.y > 0 && gameGrid[hit.x][hit.y-1].state == CellState.Empty) {
            targetPoints.push(new Point(hit.x, hit.y-1));
        }
        if (hit.y < GRIDSIZE -1 && gameGrid[hit.x][hit.y+1].state == CellState.Empty) {
            targetPoints.push(new Point(hit.x, hit.y+1));
        }
    }

    /**
     * This method will find a random empty cell
     * @return coordinates of an empty cell
     */
    private Point getRandomCell() {
        Point p = new Point(random.nextInt(GRIDSIZE), random.nextInt(GRIDSIZE));

        while (gameGrid[p.x][p.y].state != CellState.Empty) {
            p = new Point(random.nextInt(GRIDSIZE), random.nextInt(GRIDSIZE));
        }
        return p;
    }

    /**
     * This method will calculate the length of the ship that was sunk
     * @return length of the sunk ship
     */
    private int calculateShipLength() {
        if (pointsHit.size() <= 1) {
            return 1;
        }
        Point first = pointsHit.get(0);
        Point last = pointsHit.get(pointsHit.size() - 1);
        return Math.max(Math.abs(last.x - first.x), Math.abs(last.y - first.y)) + 1;
    }

    /**
     * Class representing a single cell in the game grid.
     * Each cell has a cell state and a probability value
     */
    private static class Cell {
        CellState state = CellState.Empty;
        int probability = 0;

        /**
         * Method to reset the state of a cell
         */
        public void reset() {
            state = CellState.Empty;
            probability = 0;
        }
    }

    /**
     * Enum class representing the possible states of a cell (Same as Battleship3 API Cell State)
     */
    private enum CellState {
        Empty, Hit, Miss
    }
}