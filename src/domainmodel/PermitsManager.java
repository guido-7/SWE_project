package src.domainmodel;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PermitsManager {
    public static Set<Permits> createGuestPermits() {
        return new HashSet<>(Set.of(Permits.WATCH, Permits.SHARE));
    }
    public static Set<Permits> createUserPermits() {
        Set<Permits> permits = new HashSet<>(Set.of(Permits.POST, Permits.COMMENT, Permits.INTERACT));
        if(permits.addAll(createGuestPermits() )){
            return permits;
        }
        return null;
    }
    public static Set<Permits> createModeratorPermits() {
        Set<Permits> permits = new HashSet<>(Set.of(Permits.DELETE, Permits.MODIFY, Permits.BAN,
                Permits.UNBAN));
        if(permits.addAll(Objects.requireNonNull(createUserPermits()))){
            return permits;
        }
        return null;
    }
    public static Set<Permits> createAdminPermits() {
        Set<Permits> permits = new HashSet<>(Set.of(Permits.DO_ALL));
        if(permits.addAll(Objects.requireNonNull(createModeratorPermits()))){
            return permits;
        }
        return null;
    }
    public static Set<Permits> getModeratorPermits(){
        return createModeratorPermits();
    }
}

