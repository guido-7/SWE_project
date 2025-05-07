package src.controllers;

import java.sql.SQLException;

public interface PageController extends Controller {
    void init_data() throws SQLException;
    void setOnEvent();
}