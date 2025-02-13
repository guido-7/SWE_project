package src.domainmodel;

import java.util.List;
import java.util.Set;

public class Guest {
    private final Set<Permits> permits;

    public Guest(Set<Permits> permits) {
        this.permits = permits;
    }
    public boolean hasPermit(Permits permit){
        return permits.contains(permit);
    }
    public boolean hasPermits(Set<Permits> permits){
        return this.permits.containsAll(permits);
    }
    public Set<Permits> getPermits() {
        return permits;
    }
}
