package src.domainmodel;

public class Admin extends User{
    public Admin(User user) {
        super(user.getId(), user.getNickname(), user.getName(), user.getSurname());
    }
}
