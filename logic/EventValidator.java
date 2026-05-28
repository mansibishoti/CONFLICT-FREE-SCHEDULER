package logic;

import java.util.List;

public class EventValidator {

    public synchronized boolean canSchedule(Event newEvent,
                                             List<Event> existingEvents) {
        for (Event e : existingEvents) {
            if (isConflict(newEvent, e)) return false;
        }
        return true;
    }

    private boolean isConflict(Event e1, Event e2) {
        if (e1.getYear()  != e2.getYear())  return false;
        if (e1.getMonth() != e2.getMonth()) return false;
        if (e1.getDay()   != e2.getDay())   return false;
        if (!e1.getVenue().equalsIgnoreCase(e2.getVenue())) return false;

        int e1Start = e1.getStartHour() * 60 + e1.getStartMinute();
        int e1End   = e1.getEndHour()   * 60 + e1.getEndMinute();
        int e2Start = e2.getStartHour() * 60 + e2.getStartMinute();
        int e2End   = e2.getEndHour()   * 60 + e2.getEndMinute();

        return (e1Start < e2End && e2Start < e1End);
    }
}