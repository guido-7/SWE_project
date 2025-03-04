package src.servicemanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FormattedTime {
    public String getFormattedTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        long minutesAgo = ChronoUnit.MINUTES.between(time, now);
        long hoursAgo = ChronoUnit.HOURS.between(time, now);
        long daysAgo = ChronoUnit.DAYS.between(time, now);

        if (minutesAgo < 1) {
            return "Just now";
        } else if (minutesAgo < 60) { // Meno di un'ora
            return minutesAgo + " min ago";
        } else if (hoursAgo < 24) { // Oggi
            return hoursAgo + "h ago";
        } else if (daysAgo < 7) { // Ultima settimana
            return daysAgo + "d ago";
        } else if (time.getYear() == now.getYear()) { // Stesso anno
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            return time.format(formatter);
        } else { // Anni precedenti
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
            return time.format(formatter);
        }
    }

    public String getBanTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        long daysAgo = ChronoUnit.DAYS.between(now,time);

        if(daysAgo < 1) {
            return "Today, "+time.getHour()+":"+time.getMinute();
        } else if (daysAgo < 7) { // Ultima settimana
            return daysAgo + "d ago";
        } else if (time.getYear() == now.getYear()) { // Stesso anno
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            return time.format(formatter);
        } else { // Anni precedenti
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
            return time.format(formatter);
        }

    }

}
