package src.domainmodel;

import java.util.Set;

public class Admin extends User{
    public Admin(User user) {
        super(user.getId(), user.getNickname(), user.getName(), user.getSurname(), user.getPermits());
    }

    public Admin(int id, String nickname, String name, String surname, Set<Permits> permits) {
        super(id, nickname, name, surname, permits);
    }
}
