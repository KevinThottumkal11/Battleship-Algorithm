import battleship.BattleShip3;

/**
 * Starting code for COMP10205 - Assignment#5 - Version 3 of BattleShip
 * @author mark.yendt@mohawkcollege.ca (November 2024)
 */

public class A5 {
    public static void main(String[] args) {

        final int NUMBEROFGAMES = 10000;
        System.out.println(BattleShip3.getVersion());
        BattleShip3 battleShip = new BattleShip3(NUMBEROFGAMES, new BattleBot());
        int [] gameResults = battleShip.run();

        battleShip.reportResults();
    }
}
