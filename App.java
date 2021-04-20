import java.util.*;

public class App {
    public static void main(String[] args) {
        Reversi game = new Reversi();
        
        String instruction = "How do you like to play? \n" + 
        "1. human vs AI(pure MCT) - human take Black coins, and will go first\n" +
        "2. human vs AI(heuristic) - human take Black coins, and will go first\n" +
        "3. AI (MCT) vs AI (heuristic) - MCT take white coins, and will go first" + 
        "Enter 1, 2 or 3";

        System.out.println(instruction);
        Scanner sc = new Scanner(System.in);
        try {
            String h = sc.nextLine();
            int choice = Integer.parseInt(h);
            if (choice == 3){
                game.runAIcompetition(100);
            }
            else if(choice == 2 || choice == 1){
                game.runHumanAIcompetition(sc, choice);
            }else{
                System.out.println("Invalid choice, exiting program");
            }
        }catch (Exception e){
            System.out.println("Invalid input format, please try again.");
        }

        
        sc.close();
    }
}