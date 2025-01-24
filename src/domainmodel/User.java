package src.domainmodel;

public class User {
    private final int id;
    private String nickname;
    private final String name;
    private final String surname;

    //costruttore
    public User(int id, String nickname, String name, String surname) {
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
}