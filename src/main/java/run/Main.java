package run;
/**
 * Main class to prompt start screen
 * 
 * 
 * @author Matthew-Vanhoomissen
 */

import javax.swing.SwingUtilities;
import ui.*;

public class Main {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartWindow();
        });
    }
}