package logic;
import java.sql.*;
import java.util.*;
class SlotMonitor extends Thread {
    public void run(){
        try{
            while(true){
                System.out.println("\n[Background Slot Monitor Running...]");
                Thread.sleep(5000);
            }
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }
}

class ReservationTimer extends Thread {
    public void run(){
        try{
            System.out.println("\nSlot temporarily reserved...");
            Thread.sleep(15000);
            System.out.println("Reservation expired after 15 seconds.");
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }
}
class BookingThread extends Thread{
    private Event newEvent;
    private List<Event> events;
    private EventValidator validator;
    public BookingThread(
            Event newEvent,
            List<Event> events,
            EventValidator validator
    ){
        this.newEvent = newEvent;
        this.events = events;
        this.validator = validator;
    }
    public void run(){
        synchronized(events){
            if(validator.canSchedule(newEvent,events)){
                events.add(newEvent);

                System.out.println("\n"+Thread.currentThread().getName()+" booked "+newEvent.getVenue()+" successfully.");
            }
            else{
                System.out.println("\n"+Thread.currentThread().getName()+" failed. Slot already booked.");
            }
        }
    }
}

public class checker {
    static final String URL ="jdbc:mysql://localhost:3306/scheduler_db";
    static final String USER = "root";
    static final String PASSWORD ="Utkarsh@6969";
    public static Connection getConnection(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL,USER,PASSWORD);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static List<Event> getBookingRequests(){
    List<Event> requests =new ArrayList<>();
    try{
        Connection con =getConnection();
        String query ="SELECT * FROM booking_requests";
        Statement st =con.createStatement();
        ResultSet rs =st.executeQuery(query);
        while(rs.next()){
            Event e =new Event(

                rs.getString("venue"),

                rs.getInt("year"),
                rs.getInt("month"),
                rs.getInt("day"),

                rs.getInt("start_hour"),
                rs.getInt("start_minute"),

                rs.getInt("end_hour"),
                rs.getInt("end_minute")
            );

            requests.add(e);
        }

        con.close();
    }

    catch(Exception e){
        e.printStackTrace();
    }
    return requests;
}
    public static List<Event> getEventsFromDB(){
        List<Event> events =
        new ArrayList<>();
        try{
            Connection con = getConnection();
            String query = "SELECT * FROM events";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next()){
                Event e = new Event(
                    rs.getString("venue"),
                    rs.getInt("year"),
                    rs.getInt("month"),
                    rs.getInt("day"),
                    rs.getInt("start_hour"),
                    rs.getInt("start_minute"),
                    rs.getInt("end_hour"),
                    rs.getInt("end_minute")
                );
                events.add(e);
            }
            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return events;
    }
public static void main(String args[]){
    SlotMonitor monitor =new SlotMonitor();
    monitor.setDaemon(true);
    monitor.start();
    List<Event> events =getEventsFromDB();
    List<Event> requests =getBookingRequests();
    EventValidator validator =new EventValidator();
    for(int i=0;i<requests.size();i++){
        BookingThread t =new BookingThread(requests.get(i),events,validator);
        t.setName("User-"+(i+1));
        t.start();
    }
    ReservationTimer t =new ReservationTimer();
    t.start();
}
}