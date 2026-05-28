package logic;

public class Event {
    private String venue;
    private int year;
    private int month;
    private int day;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    public Event(
            String venue,
            int year, int month, int day,
            int startHour, int startMinute,
            int endHour, int endMinute) {
        this.venue       = venue;
        this.year        = year;
        this.month       = month;
        this.day         = day;
        this.startHour   = startHour;
        this.startMinute = startMinute;
        this.endHour     = endHour;
        this.endMinute   = endMinute;
    }

    public String getVenue()       { return venue;       }
    public int    getYear()        { return year;        }
    public int    getMonth()       { return month;       }
    public int    getDay()         { return day;         }
    public int    getStartHour()   { return startHour;   }
    public int    getStartMinute() { return startMinute; }
    public int    getEndHour()     { return endHour;     }
    public int    getEndMinute()   { return endMinute;   }
}