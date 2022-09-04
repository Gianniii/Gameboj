package ch.epfl.gameboj.gui;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.embed.swing.SwingFXUtils;

/**
 * Class Main, launches the Gameboy and displays it
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public class Main extends Application {
    private float EMULATION_SPEED = 1f;
    private int CYCLES_PER_ITERATION = (int)(17_556 * EMULATION_SPEED);
    private Cartridge cartridge;
    private Stage window;
    private ImageView gameBoyView;
    private String fileName;
    //Map linking key codes to Joypad keys
    private HashMap<javafx.scene.input.KeyCode, Joypad.Key> keys;
    private KeyCode turboKeyCode, resetKeyCode, colorKeyCode, screenShotKeyCode;
    private HashMap<ToggleButton, Joypad.Key> buttonToKeyCorrespondence;
    
    
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        setKeysToDefault(); 
        launchMainMenu();
    }
    
    @Override
    public void stop() {
        //Autosave 
        if(!(fileName == null))
                cartridge.save(fileName);
    }
    
    private ImageView loadGameBoyImageView(String fileName) throws IllegalArgumentException, IOException {
        //Create Gameboy
        System.out.println("1");
        cartridge = Cartridge.ofFile(new File(fileName));
        System.out.println("2");
        cartridge.loadSave(fileName);
        System.out.println("3");
        GameBoy gb = new GameBoy(cartridge);
        System.out.println("gets to here");
        
        ImageView imageView = new ImageView();
        
        //Handles key events
        imageView.setOnKeyPressed(e -> {
            //For cross-keyboard compatibility
            KeyCode k = KeyCode.getKeyCode(e.getText().toUpperCase());
            //if k is null must be an arrow key
            if (k == null && keys.containsKey(e.getCode())) {
                gb.joypad().keyPressed(keys.get(e.getCode()));
            }
            else if (keys.containsKey(k)){
                gb.joypad().keyPressed(keys.get(k));
            }
            if (k != null) {
                if (k.equals(turboKeyCode)) {
                    //Allow three speeds(x1, x2, x3)
                    EMULATION_SPEED = (EMULATION_SPEED % 3) + 1;
                    CYCLES_PER_ITERATION = (int) (EMULATION_SPEED * 17556);
                }
                if (k.equals(resetKeyCode)) {
                    //save before reloading main menu
                    cartridge.save(fileName);
                    window.close();
                    launchMainMenu();
                }
                if (k.equals(colorKeyCode)) {
                    ImageConverter.changeColor();
                }
                if (k.equals(screenShotKeyCode)) {
                    screenshot(gb);
                }
            }
        });
        imageView.setOnKeyReleased(e -> {
            KeyCode k = KeyCode.getKeyCode(e.getText().toUpperCase());
            if (k == null && keys.containsKey(e.getCode())) {
                gb.joypad().keyReleased(keys.get(e.getCode()));
            }
            else if (keys.containsKey(k)) {
                gb.joypad().keyReleased(keys.get(k));
            }
        });
        
        //Synchronize simulation time to real time
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                gb.runUntil(gb.cycles() + CYCLES_PER_ITERATION);
                imageView.setImage(ImageConverter.convert(gb.lcdController().currentImage()));
            }
        }.start();
        
        return imageView;
    }
    //sets gameBoyView, and sets window to a new scene
    private void launchGame(String gameName) {
        //Careful this relative may change on your system, so so this may the cause of the main not working.
        fileName = "GamebojProject/games/" +gameName + ".gb"; 
        //GamebojProject\games\Bomberman.gb
        System.out.println(fileName);
        try {
            gameBoyView = loadGameBoyImageView(fileName);
          } catch (IllegalArgumentException | IOException e1) {
        }
          BorderPane mainPane = new BorderPane(gameBoyView);
          mainPane.setPrefHeight(2 * LcdController.LCD_HEIGHT);
          mainPane.setPrefWidth(2 * LcdController.LCD_WIDTH);
          
          gameBoyView.fitHeightProperty().bind(mainPane.heightProperty());
          gameBoyView.fitWidthProperty().bind(mainPane.widthProperty());
          window.close();
          window.setScene(new Scene(mainPane));
          window.setTitle("Gameboj");
          window.show();
          gameBoyView.requestFocus();
    }
    
    private void launchMainMenu() {
        List<Button> buttonList = new ArrayList<>();

        GridPane menuPane = new GridPane();
        //Buttons for the games
        Button rom1 = new Button("TasmaniaStory");
        Button rom2 = new Button("SuperMarioLand1");
        Button rom3 = new Button("SuperMarioLand2");
        Button rom4 = new Button("ZeldaLinksAwakening");
        Button rom5 = new Button("DonkeyKong");
        Button rom6 = new Button("BomberMan");
        Button rom7 = new Button("FlappyBoy");
        Button rom8 = new Button("Tetris");
        Button controlSettings = new Button("CONTROLS");
        Button screenshots = new Button("SCREENSHOTS");
        
        buttonList.add(rom1);
        buttonList.add(rom2);
        buttonList.add(rom3);
        buttonList.add(rom4);
        buttonList.add(rom5);
        buttonList.add(rom6);
        buttonList.add(rom7);
        buttonList.add(rom8);
        
        for (Button b : buttonList) {
            b.setOnAction(e -> launchGame(b.getText()));
            b.setPrefWidth(150);
            b.setStyle("-fx-background-color:#F83D4F;");
            b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:white;"));
            b.setOnMouseExited(e -> b.setStyle("-fx-background-color:#F83D4F;"));
        }
        
        screenshots.setOnAction(e -> launchScreenshotTab());
        controlSettings.setOnAction(e -> launchSettingsTab());
        screenshots.setPrefWidth(310);
        screenshots.setPrefHeight(40);
        controlSettings.setPrefWidth(310);
        controlSettings.setPrefHeight(40);
       
        menuPane.add(rom1, 0, 0);
        menuPane.add(rom2, 0, 1);
        menuPane.add(rom3, 0, 2);
        menuPane.add(rom4, 0, 3);
        menuPane.add(rom5, 1, 0);
        menuPane.add(rom6, 1, 1);
        menuPane.add(rom7, 1, 2);
        menuPane.add(rom8, 1, 3);
        menuPane.add(controlSettings, 0, 8, 2, 2);
        menuPane.add(screenshots, 0, 10, 2, 2);
        
        menuPane.setHgap(20);
        menuPane.setVgap(10);
        menuPane.setPadding(new Insets(10, 15 , 10, 15));
        menuPane.setPrefHeight(2 *LcdController.LCD_HEIGHT);
        menuPane.setPrefWidth(2 * LcdController.LCD_WIDTH);
        menuPane.setStyle("-fx-background-color:#393D3D");
        
        window.setTitle("Main menu");
        window.setMinHeight(2 * LcdController.LCD_HEIGHT);
        window.setMinWidth(2 * LcdController.LCD_WIDTH);
        window.setScene(new Scene(menuPane));  
        window.show();
    }
    
    private void launchSettingsTab() {
        List<ToggleButton> buttonList = new ArrayList<>();
        List<Label> labelList = new ArrayList<>();
        GridPane controlPane = new GridPane();
        setKeysToDefault();
        
        //Instantiate buttons and labels
        Button mainMenuButton = new Button("Main Menu");
        Label textA = new Label("A:");
        Label textB = new Label("B:");
        Label textSELECT = new Label("Select:");
        Label textSTART = new Label("Start:");
        Label textUP = new Label("Up:");
        Label textLEFT = new Label("Left:");
        Label textRIGHT = new Label("Right:");
        Label textDOWN = new Label("Down:");
        Label textCOLOR = new Label("Color:");
        Label textTURBO = new Label("Turbo:");
        Label textRESET = new Label("Reset:");
        Label textSCREENSHOT = new Label("Picture:");
        
        ToggleButton A = new ToggleButton("S");
        ToggleButton B = new ToggleButton("A");
        ToggleButton UP = new ToggleButton("Up");
        ToggleButton RIGHT = new ToggleButton("Right");
        ToggleButton LEFT = new ToggleButton("Left");
        ToggleButton DOWN = new ToggleButton("Down");
        ToggleButton SELECT = new ToggleButton("Space");
        ToggleButton START = new ToggleButton("D");
        ToggleButton RESET = new ToggleButton("R");
        ToggleButton COLOR = new ToggleButton("C");
        ToggleButton TURBO = new ToggleButton("T");
        ToggleButton SCREENSHOT = new ToggleButton("Q");
        
        final ToggleGroup group = new ToggleGroup();
        
        //add Labels to labelList and set their properties
        labelList.add(textA);
        labelList.add(textB);
        labelList.add(textDOWN);
        labelList.add(textUP);
        labelList.add(textLEFT);
        labelList.add(textRIGHT);
        labelList.add(textSTART);
        labelList.add(textSELECT);
        labelList.add(textTURBO);
        labelList.add(textRESET);
        labelList.add(textCOLOR);
        labelList.add(textSCREENSHOT);
        for (Label l : labelList) {
            l.setTextFill(Color.WHITE);
            l.setFont(new Font("Verdana", 15));
            l.setPrefWidth(125);
        }
        textSCREENSHOT.setFont(new Font("Verdana", 13));
        
        //add ToggleButtons to List
        buttonList.add(A);
        buttonList.add(B);
        buttonList.add(UP);
        buttonList.add(LEFT);
        buttonList.add(DOWN);
        buttonList.add(RIGHT);
        buttonList.add(SELECT);
        buttonList.add(START);
        buttonList.add(RESET);
        buttonList.add(COLOR);
        buttonList.add(TURBO);
        buttonList.add(SCREENSHOT);
        
        //Set mainMenuButton size and behavior
        mainMenuButton.setPrefSize(500, 40);
        mainMenuButton.setOnAction(e -> {
            //Avoid having two buttons for the same command
            boolean allButtonsAreDifferent = true;
            HashSet<String> intersectingButtonTexts = new HashSet<>();
            //By default the buttons have no style
            for (ToggleButton b : buttonList) {
                b.setStyle(null);
            }
            //Set buttons to red if they have the same text as others
            for (int i = 0 ; i < buttonList.size() ; ++i) {
                for (int j = i + 1 ; j < buttonList.size() ; ++j) {
                    ToggleButton a = buttonList.get(i);
                    ToggleButton b = buttonList.get(j);
                    if (a.getText().equals(b.getText())) {
                        allButtonsAreDifferent = false;
                        a.setStyle("-fx-background-color:#F83D4F;");
                        b.setStyle("-fx-background-color:#F83D4F;");
                        intersectingButtonTexts.add(a.getText());
                    } else if (intersectingButtonTexts.contains(a.getText()) &&
                                intersectingButtonTexts.contains(b.getText()) ) {
                        a.setStyle("-fx-background-color:#F83D4F;");
                        b.setStyle("-fx-background-color:#F83D4F;");
                    }
             
                }
            }
            if (allButtonsAreDifferent) {
                updateKeys(buttonList, COLOR, RESET, SCREENSHOT,TURBO);
                launchMainMenu();
            }
        });

        //Setup controlPane
        controlPane.add(mainMenuButton, 0, 12, 4, 5);
        controlPane.add(textA, 0, 0);
        controlPane.add(textB, 0, 1);
        controlPane.add(textSTART, 0, 2);
        controlPane.add(textSELECT, 0, 3);
        controlPane.add(textUP, 2, 0);
        controlPane.add(textLEFT, 2, 1);
        controlPane.add(textDOWN, 2, 2);
        controlPane.add(textRIGHT, 2, 3);
        controlPane.add(textCOLOR, 0, 4);
        controlPane.add(textRESET, 2, 4);
        controlPane.add(textTURBO, 0, 5);
        controlPane.add(textSCREENSHOT, 2, 5);
        controlPane.add(A, 1, 0);
        controlPane.add(B, 1, 1);
        controlPane.add(UP, 3, 0);
        controlPane.add(LEFT, 3, 1);
        controlPane.add(DOWN, 3, 2);
        controlPane.add(RIGHT, 3, 3);
        controlPane.add(START, 1, 2);
        controlPane.add(SELECT, 1, 3);
        controlPane.add(COLOR, 1,  4);
        controlPane.add(RESET, 3, 4);
        controlPane.add(TURBO, 1, 5);
        controlPane.add(SCREENSHOT, 3, 5);
        controlPane.setHgap(7);
        controlPane.setVgap(10);
        controlPane.setPadding(new Insets(10, 15 , 10, 15));
        controlPane.setPrefHeight(2 *LcdController.LCD_HEIGHT);
        controlPane.setPrefWidth(2 * LcdController.LCD_WIDTH);
        controlPane.setStyle("-fx-background-color:#393D3D;");

        
        //Associate buttons to joypad Keys 
        buttonToKeyCorrespondence = new HashMap<>();
        buttonToKeyCorrespondence.put(LEFT, Joypad.Key.LEFT);
        buttonToKeyCorrespondence.put(RIGHT, Joypad.Key.RIGHT);
        buttonToKeyCorrespondence.put(UP, Joypad.Key.UP);
        buttonToKeyCorrespondence.put(DOWN, Joypad.Key.DOWN);
        buttonToKeyCorrespondence.put(A, Joypad.Key.A);
        buttonToKeyCorrespondence.put(B, Joypad.Key.B);
        buttonToKeyCorrespondence.put(START, Joypad.Key.START);
        buttonToKeyCorrespondence.put(SELECT, Joypad.Key.SELECT);
        
        //Description of button behavior
        for (ToggleButton b : buttonList) {
            b.setPrefWidth(150);
            //Put all toggle buttons in a group so only one can be active at a time
            b.setToggleGroup(group);
            //handle key modifying
            b.setOnMousePressed(e -> {
                b.setOnKeyPressed(event -> {
                    if (b.isSelected()) {
                        //Use correct KeyCode
                        KeyCode k = KeyCode.getKeyCode(event.getText().toUpperCase());
                        if (k == null) {
                            b.setText(event.getCode().getName());
                        } else {
                            b.setText(k.getName());
                        }
                        event.consume();
                    }
                });
            });
            
        }         
        //Indicate if a red button is selected or not by updating 
        //button colors after every click on the scene
        Scene scene = new Scene(controlPane);
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            for (ToggleButton b : buttonList) {
                if (b.isSelected() && b.getStyle()=="-fx-background-color:#F83D4F;")
                    b.setStyle("-fx-background-color:#ff7f7f;");
                if (!b.isSelected() && b.getStyle()=="-fx-background-color:#ff7f7f;")    
                    b.setStyle("-fx-background-color:#F83D4F;");
            }
        });
        
        window.setTitle("Control Menu");
        window.setMinHeight(2 * LcdController.LCD_HEIGHT);
        window.setMinWidth(2 * LcdController.LCD_WIDTH);
        window.setScene(scene);
        window.show();
    }
   
    //Update the keyCodes of the keys HashMap with text contained in each button
    private void updateKeys(List<ToggleButton> buttonList, ToggleButton COLOR, ToggleButton RESET,
                            ToggleButton SCREENSHOT, ToggleButton TURBO) {
        keys.clear();
        for (ToggleButton button : buttonList) {
            //Turbo, color, reset and screenshot keys are not mapped on joypad so need to treat them separately
            KeyCode buttonKeyCode = KeyCode.getKeyCode(button.getText());
            if (buttonToKeyCorrespondence.containsKey(button)) {
                keys.put(buttonKeyCode, buttonToKeyCorrespondence.get(button));
            } else if (button.equals(COLOR)) {
                colorKeyCode = buttonKeyCode;
            } else if (button.equals(RESET)) {
                resetKeyCode = buttonKeyCode;
            } else if (button.equals(TURBO)) {
                turboKeyCode = buttonKeyCode;
            } else if (button.equals(SCREENSHOT)){
                screenShotKeyCode = buttonKeyCode;
            }
        }
    }
    
    private void setKeysToDefault() {
        keys = new HashMap<>();
        keys.put(KeyCode.LEFT, Joypad.Key.LEFT);
        keys.put(KeyCode.RIGHT, Joypad.Key.RIGHT);
        keys.put(KeyCode.UP, Joypad.Key.UP);
        keys.put(KeyCode.DOWN, Joypad.Key.DOWN);
        keys.put(KeyCode.S, Joypad.Key.A);
        keys.put(KeyCode.A, Joypad.Key.B);
        keys.put(KeyCode.D, Joypad.Key.START);
        keys.put(KeyCode.SPACE, Joypad.Key.SELECT);
        turboKeyCode = KeyCode.T;
        resetKeyCode = KeyCode.R;
        colorKeyCode = KeyCode.C;
        screenShotKeyCode = KeyCode.Q;
    }
    
    //Take a screenshot and saves it in screenshot folder
    private void screenshot(GameBoy gb) {
        BufferedImage image = SwingFXUtils.fromFXImage
                (ImageConverter.convert(gb.lcdController().currentImage()), null);
        String timeStamp = new SimpleDateFormat("-dd-HH-mm-ss").format(new java.util.Date());
        try {
            ImageIO.write(image, "png", 
                    new File("games/screenshots/screenshot" + timeStamp + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ScreenShot saved !");
    }
    
    private void launchScreenshotTab() {
        File folder = new File("games/screenshots");
        File[] listOfFiles = folder.listFiles();
        Button mainMenuButton = new Button("Main Menu");
        Button deleteEnable = new Button("Enable Delete Mode");
        mainMenuButton.setPrefSize(500, 40);
        deleteEnable.setPrefSize(500, 40);
        mainMenuButton.setOnAction(e -> launchMainMenu());
        //Switch mode
        deleteEnable.setOnAction(e -> {
            if (deleteEnable.getText().equals("Enable Delete Mode")) {
                deleteEnable.setText("Disable Delete Mode");
            } else {
                deleteEnable.setText("Enable Delete Mode");
            }
        });
        
        List<Button> buttonList = new ArrayList<>();
        Button shot1 = new Button();
        Button shot2 = new Button();
        Button shot3 = new Button();
        Button shot4 = new Button();
        Button shot5 = new Button();
        Button shot6 = new Button();
        Button shot7 = new Button();
        Button shot8 = new Button();
        
        buttonList.add(shot1);
        buttonList.add(shot2);
        buttonList.add(shot3);
        buttonList.add(shot4);
        buttonList.add(shot5);
        buttonList.add(shot6);
        buttonList.add(shot7);
        buttonList.add(shot8);
       
        int i = 0;
        //Button description and behavior(display or delete screenshots)
        for (Button b : buttonList) {
            if (i < listOfFiles.length) {
                b.setText(listOfFiles[listOfFiles.length - i - 1].getName());
            }    
            b.setOnAction(e -> {
                if (deleteEnable.getText().equals("Disable Delete Mode")) {
                    for (File f : listOfFiles) {
                        if (f.getName().equals(b.getText())) {
                            f.delete();
                            b.setText("");
                        }
                    }
                } else {
                    showImage(b.getText());
                }
            });
            ++i;
            b.setPrefSize(150, 20);
        }

        GridPane screenshotsPane = new GridPane();
        screenshotsPane.setHgap(7);
        screenshotsPane.setVgap(10);
        screenshotsPane.setPadding(new Insets(10, 15 , 10, 15));

        screenshotsPane.setPrefHeight(2 * LcdController.LCD_HEIGHT);
        screenshotsPane.setPrefWidth(2 * LcdController.LCD_WIDTH);
        screenshotsPane.setStyle("-fx-background-color:#393D3D");
        screenshotsPane.add(mainMenuButton, 0, 12, 2, 5);
        screenshotsPane.add(deleteEnable, 0, 7, 2, 5);
        screenshotsPane.add(shot1, 0, 0);
        screenshotsPane.add(shot2, 0, 1);
        screenshotsPane.add(shot3, 0, 2);
        screenshotsPane.add(shot4, 0, 3);
        screenshotsPane.add(shot5, 1, 0);
        screenshotsPane.add(shot6, 1, 1);
        screenshotsPane.add(shot7, 1, 2);
        screenshotsPane.add(shot8, 1, 3);

        window.setTitle("ScreenShots");
        window.setScene(new Scene(screenshotsPane));
        window.show();
    }
    
    //Gets image file and shows it on a new stage
    private void showImage(String imageName) {
        File file = new File("games/screenshots/" + imageName);
        ImageView imageView = new ImageView(new Image(file.toURI().toString()));
        StackPane pane = new StackPane();
        pane.getChildren().add(imageView);
        pane.setPrefHeight(2 * LcdController.LCD_HEIGHT);
        pane.setPrefWidth(2 * LcdController.LCD_WIDTH);
        imageView.fitWidthProperty().bind(pane.widthProperty());
        imageView.fitHeightProperty().bind(pane.heightProperty());
        Scene scene = new Scene(pane);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(imageName);
        stage.show();
    }
    
}
