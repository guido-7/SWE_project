package src.domainmodel;

import java.util.List;
import java.util.Set;

public class Guest {
    private final Set<Permits> permits;
    private Role role;

    public Guest(Set<Permits> permits,Role role) {
        this.permits = permits;
        this.role = role;
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
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }
}
