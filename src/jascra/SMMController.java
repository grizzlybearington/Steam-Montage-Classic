package jascra;

import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;

public class SMMController {
	ObservableList<String> chooseIDTypeList = FXCollections.observableArrayList("steamname","steamID64");
	
	@FXML
	private ChoiceBox<String> chooseIDType;
	@FXML
	private TextField insertID;
	@FXML
	private TextArea logText;
	@FXML
	private Hyperlink githubLink;
	@FXML
	private Button goButton;
	@FXML
	private TextField userWidth;
	
	UnaryOperator<Change> numericOnly = change -> {
	    String input = change.getText();
	    if (input.matches("[0-9]*")) { 
	        return change;
	    }
	    return null;
	};
	
	@FXML
	private void initialize() {		
		chooseIDType.setValue("steamname");
		chooseIDType.setItems(chooseIDTypeList);
		chooseIDType.setTooltip(new Tooltip("Choose steam ID type"));
		userWidth.setTextFormatter(new TextFormatter<String>(numericOnly));
	}
	
	@FXML
	public void goButtonPress(MouseEvent event) {
		disableAllUI();
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {
				montage();
				return null;
			}
		};
		Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
	}
	
	@FXML
	public void montage() {
		logText.setText("");
		String steamID = insertID.getText();
		int montageWidth;
		
		try {
			montageWidth = Integer.parseInt(userWidth.getText());
		} catch (NumberFormatException e) {
			montageWidth = 0;
		}
		if (steamID.equals("") || montageWidth <= 0 ) {
			logText.appendText("- ERROR: invalid ID and/or width.");
			enableAllUI();
			return;
		}
		
		String userURL;
		if (chooseIDType.getValue().equals("steamname")) {
			userURL = "http://steamcommunity.com/id/" + steamID + "/games?tab=all&sort=name&xml=1";
		} else {
			userURL = "http://steamcommunity.com/profiles/" + steamID + "/games?tab=all&sort=name&xml=1";
		}
		
		logText.appendText("- Please wait, this could take a while...\n");
		
		TreeMap<String,String> gameList = new TreeMap<>(); //TreeMap auto-sorts based on key.
		try {
			URL url = new URL(userURL);
	        String name = null, appID = null;
	        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
	        try {
	            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(url.openStream());
	            while (xmlEventReader.hasNext()) {
	                XMLEvent xmlEvent = xmlEventReader.nextEvent();
	                if (xmlEvent.isStartElement()) {
	                    StartElement startElement = xmlEvent.asStartElement();
	                    if (startElement.getName().getLocalPart().equals("appID")) {
	                        xmlEvent = xmlEventReader.nextEvent();
	                        appID = (xmlEvent.asCharacters().getData());                  
	                    } else if (startElement.getName().getLocalPart().equals("name")) {
	                        xmlEvent = xmlEventReader.nextEvent();
	                        name = (xmlEvent.asCharacters().getData());
	                    }
	                    if (name != null && appID != null) {
	                    	gameList.put(name,appID);
	                    	name = null; appID = null;
	                    }
	                }
	            }
	        } catch (javax.xml.stream.XMLStreamException e) {
	            e.printStackTrace();
	            enableAllUI();
	            return;
	        }
		} catch (IOException e) {
			e.printStackTrace();
			enableAllUI();
			return;
		}
		
		logText.appendText("- Games successfully loaded. Starting montage creation...\n");
		
		double count = gameList.size();
		if (montageWidth > count) { 
			logText.appendText("- ERROR: width bigger than amount of games!");
			enableAllUI();
			return;
		}
        int width = montageWidth * 460;
        int x = 0, y = 0;
        int height = (int)(Math.ceil(count/montageWidth) * 215);
        
        logText.appendText("Games: " + (int)count + ", height: " + height + "px, width: " + width + "px\n");
        logText.appendText("- Please wait...\n");
		
        BufferedImage bufferedImage;
        bufferedImage = BigBufferedImage.create(width, height, BigBufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();
        
        for (Map.Entry<String, String> g : gameList.entrySet()) {
            try {
                BufferedImage bufferedImage1 = ImageIO.read(new URL("http://cdn.akamai.steamstatic.com/steam/apps/" + g.getValue() + "/header.jpg"));
                graphics.drawImage(bufferedImage1,x,y,null);
                x+=460;
                if (x >= bufferedImage.getWidth()) {
                    x = 0;
                    y += 215;
                }
            } catch (java.io.IOException e) {
            	logText.appendText("- Cannot find header for " + g.getKey() + ". Perhaps it should be in a banlist?\n");
                continue;
            }
        }
        try {
            ImageIO.write(bufferedImage, "png", new File(new SimpleDateFormat("'montage 'yyyy-MM-dd hh-mm-ss'.png'").format(new Date())));
        } catch (IOException e) {
            e.printStackTrace();
            enableAllUI();
			return;
        }
        logText.appendText("- Done!");
        enableAllUI();
	}
	
	public void disableAllUI() {
		insertID.setDisable(true);
		chooseIDType.setDisable(true);
		userWidth.setDisable(true);
		goButton.setDisable(true);
	}
	
	public void enableAllUI() {
		insertID.setDisable(false);
		chooseIDType.setDisable(false);
		userWidth.setDisable(false);
		goButton.setDisable(false);
	}
	
	@FXML
	public void visitGit(MouseEvent event) {
		try {
	        Desktop.getDesktop().browse(new URI("http://www.github.com/"));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}