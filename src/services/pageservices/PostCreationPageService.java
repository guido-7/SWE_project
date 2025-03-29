package src.services.pageservices;

import javafx.scene.control.Label;
import src.persistence.DAOs.PostDAO;

import java.sql.SQLException;
import java.util.Map;

public class PostCreationPageService {
    PostDAO postDAO = new PostDAO();
    Label errorLabel;

    public boolean createPost(String community, String title, String content, int communityId, int userId) throws SQLException {
        String validationMessage = validateInput(community, title, content);
        if(validationMessage != null){
            showError(errorLabel, validationMessage);
            return false;
        }
        postDAO.save(Map.of("title", title, "content", content, "community_id", communityId, "user_id", userId));
        return true;
    }

    private String validateInput(String community, String title, String content) {
        if (community.isEmpty() && title.isEmpty() && content.isEmpty()) {
            return "Please enter all fields.";
        }
        if (community.isEmpty() && title.isEmpty()) {
            return "Please select community and enter title.";
        }
        if (community.isEmpty() && content.isEmpty()) {
            return "Please select community and enter title.";
        }
        if (title.isEmpty() && content.isEmpty()) {
            return "Please enter title and content.";
        }
        if (community.isEmpty()) {
            return "Please select community.";
        }
        if (title.isEmpty()) {
            return "Please enter title.";
        }
        if (content.isEmpty()) {
            return "Please enter content.";
        }
        return null;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setOpacity(1);
        errorLabel.setText(message);
    }

    public void setErrorLabel(Label errorLabel) {
        this.errorLabel = errorLabel;
    }

}
