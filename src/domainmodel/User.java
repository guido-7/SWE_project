package src.domainmodel;

import java.util.Set;

public class User extends Guest {
    private final int id;
    private String nickname;
    private final String name;
    private final String surname;
    private String description;

    public User(int id, String nickname, String name, String surname, Set<Permits> permits) {
        super(permits);
        this.id = id;
        this.nickname = nickname;
        this.name = name;
        this.surname = surname;
    }

    public int getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}