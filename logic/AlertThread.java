package logic;
import javax.swing.*;

public class AlertThread extends Thread {

    private String condition;

    public AlertThread(String condition) {
        this.condition = condition;
    }

    @Override
    public void run() {

        if(condition.equalsIgnoreCase("FULL")) {

            JOptionPane.showMessageDialog(
                    null,
                    "Seats Full!"
            );

        }
        else if(condition.equalsIgnoreCase("AVAILABLE")) {

            JOptionPane.showMessageDialog(
                    null,
                    "Seats Available!"
            );
        }
    }
}