package src;

public class User {
    private final int id;
    private String name;
    private String surname;
    private String mail;
    private String password;
    private int age;

    //costruttore
    public User(int id, String name, String surname, String mail, String password, int age) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.mail = mail;
        this.password = password;
        this.age = age;
    }


}