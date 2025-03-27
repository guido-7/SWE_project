package test.functionaltest;

import javafx.application.Platform;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import src.controllers.*;
import src.controllers.factory.PageControllerFactory;
import src.domainmodel.*;
import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.orm.CommunityDAO;
import src.orm.SubscriptionDAO;
import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;
import test.UITestUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionalTest extends ApplicationTest {
    private HomePageController homePageController;
    private final UITestUtils uiTestUtils = new UITestUtils();

    MouseEvent mouseClick = new MouseEvent(
            MouseEvent.MOUSE_CLICKED,   // Tipo di evento
            0,                         // Coordinate X
            0,                         // Coordinate Y
            0,                         // Coordinate screenX
            0,                         // Coordinate screenY
            MouseButton.PRIMARY,       // Tipo di tasto (primo tasto del mouse)
            1,                         // Numero di clic (ad esempio 1 per il clic singolo)
            false,                     // Shift key premuto
            false,                     // Control key premuto
            false,                     // Alt key premuto
            false,                     // Meta key premuto
            false,                     // Primary button down
            false,                     // Middle button down
            false,                     // Secondary button down
            false,                     // Back button down
            false,                     // Forward button down
            false,                     // Synthesized
            false,                     // Popup trigger
            false,                     // Still since press
            null                       // PickResult (puoi lasciare null, se non lo utilizzi)
    );

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        Platform.runLater(() -> {
            try {
                initializeApplication(new Stage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        WaitForAsyncUtils.waitForFxEvents(); // Attendi che l'azione venga eseguita
    }

    @BeforeAll
    public static void seDB() throws SQLException {
        String url = "database/bigDBTest.db";
        DBConnection.changeDBPath(url);
        File dbFile = new File(url);
        if (dbFile.exists()) {
            boolean isDeleted = dbFile.delete();
            if(isDeleted)
                System.out.println("Database deleted successfully");
            else
                System.out.println("Database not deleted successfully");
        } else {
            System.out.println("The database does not exist.");
        }
        DBConnection.connect();
        SetDB.createDB();
        SetDB.generatefakedata(40, 10, 100, 40);
        DBConnection.disconnect();
    }


    @Test
    void testVisibilityGuest() throws Exception {
        ImageView userProfileAccess = uiTestUtils.getPrivateField(homePageController, "userProfileAccess");
        Button createCommunityButton = uiTestUtils.getPrivateField(homePageController, "createCommunityButton");
        Button login = uiTestUtils.getPrivateField(homePageController, "login");

        // Test visibilità iniziale degli elementi
        assertFalse(userProfileAccess.isVisible());
        assertFalse(createCommunityButton.isVisible());
        assertTrue(login.isVisible());

        // Test proprietà managed degli elementi
        assertFalse(userProfileAccess.isManaged());
        assertFalse(createCommunityButton.isManaged());
        assertTrue(login.isManaged());
    }

    @Test
    void testGoToLoginPage() throws Exception {
        uiTestUtils.goToLoginPage();

        assertEquals("login", SceneManager.getCurrentStageName());
    }

    @Test
    void testLogin() throws Exception {
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");

        assertEquals("home", SceneManager.getCurrentStageName());

        ImageView userProfileAccess = uiTestUtils.getPrivateField(homePageController, "userProfileAccess");
        Button login = uiTestUtils.getPrivateField(homePageController, "login");
        assertTrue(userProfileAccess.isVisible());
        assertFalse(login.isVisible());
        assertFalse(login.isManaged());
    }

    @Test
    void testSubscribeCommunity() throws Exception {
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");
        uiTestUtils.subscribeCommunity("news");

        Text communityTitle = lookup("#community_title").queryAs(Text.class);
        String titleText = communityTitle.getText();
        assertEquals("news", titleText);

        assertTrue(lookup("#unsubscribeButton").query().isVisible());
        assertFalse(lookup("#subscribeButton").query().isVisible());
    }

    @Test
    void testOpenPost() {
        uiTestUtils.openPost();

        assertInstanceOf(PostPageController.class, GuestContext.getCurrentController());
    }

    @Test
    void testLikePost() throws Exception {
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");
        uiTestUtils.openPost();

        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();

        Label likeCount = from(post).lookup("#scoreLabel").query();
        int initialLikes = Integer.parseInt(likeCount.getText());

        uiTestUtils.like(post);

        int finalLikes = Integer.parseInt(likeCount.getText());
        assertEquals(initialLikes + 1, finalLikes);

        // remove like
        uiTestUtils.like(post);
    }

    @Test
    void testDislikePost() throws Exception {
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");
        uiTestUtils.openPost();

        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();

        Label dislikeCount = from(post).lookup("#scoreLabel").query();
        int initialDislikes = Integer.parseInt(dislikeCount.getText());

        // add dislike
        uiTestUtils.dislike(post);

        int finalDislikes = Integer.parseInt(dislikeCount.getText());
        assertEquals(initialDislikes - 1, finalDislikes);

        // remove dislike
        uiTestUtils.dislike(post);
    }

    @Test
    void testLikeByGuest() {
        uiTestUtils.openPost();

        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();
        Button likeButton = from(post).lookup("#likeButton").query();
        Label likeCount = from(post).lookup("#scoreLabel").query();
        int initialLikes = Integer.parseInt(likeCount.getText());

        // add like
        Platform.runLater(likeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        int finalLikes = Integer.parseInt(likeCount.getText());
        assertEquals(initialLikes, finalLikes);

        // remove like
        Platform.runLater(likeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testComment() throws Exception {
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");
        uiTestUtils.openPost();

        // open reply field
        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();
        Button openReplyButton = from(post).lookup("#postButton").query();
        Platform.runLater(openReplyButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        TextArea replyText = from(post).lookup("#replyField").query();
        Button replyButton = from(post).lookup("#sendButton").query();
        Platform.runLater(() -> {
            replyText.setText("Test comment");
            replyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        VBox reply = (VBox) postsContainer.getChildren().getLast();
        Label replyTextElement = from(reply).lookup("#content").query();
        assertEquals("Test comment", replyTextElement.getText());
    }

    @Test
    void testCreateCommunity() throws Exception {
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");
        uiTestUtils.pressButton("#createCommunityButton");

        TextField titleField = lookup("#titleField").query();
        TextArea descriptionField = lookup("#descriptionArea").query();
        TextField ruleTitle1 = lookup("#RuleTitle1").query();
        TextArea ruleDescription1 = lookup("#rule1").query();
        TextField ruleTitle2 = lookup("#RuleTitle2").query();
        TextArea ruleDescription2 = lookup("#rule2").query();
        TextField ruleTitle3 = lookup("#RuleTitle3").query();
        TextArea ruleDescription3 = lookup("#rule3").query();
        Button createButton = lookup("#createButton").query();
        Platform.runLater(() -> {
            titleField.setText("Test Community");
            descriptionField.setText("Test description");
            ruleTitle1.setText("Test rule 1");
            ruleDescription1.setText("Test rule description 1");
            ruleTitle2.setText("Test rule 2");
            ruleDescription2.setText("Test rule description 2");
            ruleTitle3.setText("Test rule 3");
            ruleDescription3.setText("Test rule description 3");
            createButton.fireEvent(mouseClick);
        });
        WaitForAsyncUtils.waitForFxEvents();

        uiTestUtils.openCommunityPage("Test Community");

        Text communityTitle = lookup("#community_title").queryAs(Text.class);
        assertEquals("Test Community", communityTitle.getText());
    }

    @Test
    void testCreatePost() throws Exception {
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");
        uiTestUtils.subscribeCommunity("news");
        uiTestUtils.pressButton("#createPostButton");

        // select community
        TextField community = lookup("#communitySearchBar").query();
        Platform.runLater(() -> community.setText("News"));
        WaitForAsyncUtils.waitForFxEvents();

        //select first suggestion
        PostCreationPageController postCreationPageController = (PostCreationPageController) GuestContext.getCurrentController();
        ContextMenu contextMenu = uiTestUtils.getPrivateField(postCreationPageController.getCommunitySearchHelper(), "suggestionsPopup");
        CustomMenuItem firstItem = (CustomMenuItem) contextMenu.getItems().getFirst();
        Platform.runLater(firstItem::fire);
        WaitForAsyncUtils.waitForFxEvents();

        TextField titleField = lookup("#titleField").query();
        TextArea contentField = lookup("#contentArea").query();
        Platform.runLater(() -> {
            titleField.setText("Test Post");
            contentField.setText("Test content");
        });
        WaitForAsyncUtils.waitForFxEvents();

        uiTestUtils.pressButton("#postButton");
        uiTestUtils.openPost();

        // get post
        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();

        Label postTitle = from(post).lookup("#title").queryAs(Label.class);
        assertEquals("Test Post", postTitle.getText());
    }

    @Test
    public void testChangingRole() throws Exception {
        assertEquals(Role.GUEST, GuestContext.getCurrentGuest().getRole());

        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");
        assertEquals(Role.USER, GuestContext.getCurrentGuest().getRole());

        //user go to a community where user is admin
        uiTestUtils.openCommunityPage("news");
        assertEquals(Role.ADMIN,GuestContext.getCurrentGuest().getRole());

        //go to homepage
        ImageView goToHomePage = lookup("#homePageButton").query();
        Platform.runLater(() -> goToHomePage.fireEvent(mouseClick));
        sleep(2000);

        uiTestUtils.openCommunityPage("sport");
        assertEquals(Role.MODERATOR, GuestContext.getCurrentGuest().getRole());

        ImageView goToHomePage2 = lookup("#homePageButton").query();
        Platform.runLater(() -> goToHomePage2.fireEvent(mouseClick));
        sleep(2000);

        uiTestUtils.openCommunityPage("Community Title 1");
        assertEquals(Role.USER, GuestContext.getCurrentGuest().getRole());
    }

    @Test
    public void testFeedPerNoOfCommunity() throws Exception {
        //int maxCommunityId =  getMaxCommunityId();
        for (int i = 2 ; i < 6; i++){
            assertTrue(testFeed(i,10));

        }
        //assertTrue(testFeed(6));
    }

    private int getMaxCommunityId() throws SQLException {
        String query = "SELECT MAX(id) FROM Community";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement stm = connection.prepareStatement(query);
             ResultSet rs = stm.executeQuery()) {
            assertNotNull(rs);
            assertTrue(rs.next());
            return rs.getInt(1);
        }
    }
    private boolean testFeed(int numberOfSubscription,int totalCommunity) throws Exception {
//        uiTestUtils.goToLoginPage();
//        uiTestUtils.login("admin", "12345678");

        // todo iscriversi con UI
        // mi iscrivo a 3 community
//        uiTestUtils.subscribeCommunity("Community Title 1");
//        uiTestUtils.subscribeCommunity("Community Title 2");
//        uiTestUtils.subscribeCommunity("Community Title 3");
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        CommunityDAO communityDAO = new CommunityDAO();
        Set<String> titles = new HashSet<>();
//        for (int i = 1; i < numberOfSubscription+1; i++) {
//            subscriptionDAO.subscribe(101, i);
//            String commTitle = (String) communityDAO.retrieveSingleAttribute("Community", "title", "id = ?", i);
//            titles.add("r/"+commTitle);
//        }
        Set<Integer> uniqueIndexes = new HashSet<>();
        Random random = new Random();
        while (uniqueIndexes.size() < numberOfSubscription) {
            int randomIndex = random.nextInt(totalCommunity) + 1;
            uniqueIndexes.add(randomIndex);
        }
        for (int index : uniqueIndexes) {
            System.out.println("Iscritto a community"+index);
            subscriptionDAO.subscribe(101, index);
            String commTitle = (String) communityDAO.retrieveSingleAttribute("Community", "title", "id = ?", index);
            titles.add("r/"+ commTitle);
        }
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");
        //subscriptionDAO.subscribe(101, 1);
        //subscriptionDAO.subscribe(101, 2);
        //subscriptionDAO.subscribe(101, 3);
        //subscriptionDAO.subscribe(101, 4);

        // Recupero i post dalla homepage
        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty(), "Il feed non dovrebbe essere vuoto");

        Map<String, Integer> communityPostCount = new HashMap<>();
        int totalPosts = postsContainer.getChildren().size();
        int targetCommunityPosts = 0;

        for (Node postNode : postsContainer.getChildren()) {
            VBox post = (VBox) postNode;

            // Label che indica la community del post
            Label communityLabel = from(post).lookup("#community").query();
            String communityName = communityLabel.getText();

            // Incrementa il contatore per questa community
            communityPostCount.put(communityName, communityPostCount.getOrDefault(communityName, 0) + 1);

            // Conta i post provenienti dalle community 1, 2, 3
            if (titles.contains(communityName))
                targetCommunityPosts++;

            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Community label text: " + communityName);
            System.out.println("--------------------------------------------------------------------------");
        }
        for( var string : communityPostCount.entrySet()){
            System.out.println("Community: "+string.getKey()+" Posts: "+string.getValue());
        }
        // Verifica che i post delle community 1,2,3 siano almeno il 40% del totale
        double percentage = (double) targetCommunityPosts / totalPosts * 100;
        System.out.println("Percentage: " + percentage);
        double threshold = 10 + 2.1 *numberOfSubscription;
        assertTrue(percentage >= threshold, "I post delle community 1,2,3 devono essere almeno il "+threshold+" del totale, attualmente sono: " + percentage + "%" + "alla iterazione"+numberOfSubscription+"isema");


//        for (int i = 1; i < numberOfSubscription+1; i++) {
//            subscriptionDAO.unsubscribe(101, i);
//        }
        for (int index : uniqueIndexes) {
            subscriptionDAO.unsubscribe(101, index);
        }

        return true;

    }

    private void initializeApplication(Stage stage) throws IOException {
        Guest guest = new Guest(PermitsManager.createGuestPermits(), Role.GUEST);
        GuestContext.setCurrentGuest(guest);
        SceneManager.setPrimaryStage(stage);
        this.homePageController = PageControllerFactory.createHomePageController(guest);
        SceneManager.loadPrimaryScene("home", "/src/view/fxml/HomePage.fxml", homePageController);
    }

}
