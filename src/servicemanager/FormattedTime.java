package src.servicemanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FormattedTime {
    public String getFormattedTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();

        if (time.isAfter(now.minusHours(24))) { // Oggi
            long hoursAgo = ChronoUnit.HOURS.between(time, now);
            return hoursAgo + "h ago";
        } else if (time.isAfter(now.minusDays(7))) { // Ultima settimana
            long daysAgo = ChronoUnit.DAYS.between(time, now);
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
