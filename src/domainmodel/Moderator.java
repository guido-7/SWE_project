package src.domainmodel;

public class Moderator extends User{
    public Moderator(User user) {
        super(user.getId(), user.getNickname(), user.getName(), user.getSurname());
    }
}
