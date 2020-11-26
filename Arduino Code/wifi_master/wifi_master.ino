#include <Wire.h>
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WiFiMulti.h> 
#include <ESP8266mDNS.h>
#include <ESP8266WebServer.h>   // Include the WebServer library

ESP8266WiFiMulti wifiMulti;     // Create an instance of the ESP8266WiFiMulti class, called 'wifiMulti'

ESP8266WebServer server(301);    // Create a webserver object that listens for HTTP request on port 301

void handleRoot();              // function prototypes for HTTP handlers
void handleNotFound();


// setup() function -- runs once at startup --------------------------------

void setup() {

  Wire.begin(); // join i2c bus (address optional for master)

  Serial.begin(9600);

  delay(10);

  Serial.println('\n');

  wifiMulti.addAP("Fios-W233S-5G", "bid4482hoof0465zoo");   // add Wi-Fi networks you want to connect to
  wifiMulti.addAP("Fios-W233S", "bid4482hoof0465zoo");
  //wifiMulti.addAP("ssid_from_AP_3", "your_password_for_AP_3");

  Serial.println("Connecting ...");
  int i = 0;
  while (wifiMulti.run() != WL_CONNECTED) { // Wait for the Wi-Fi to connect: scan for Wi-Fi networks, and connect to the strongest of the networks above
    delay(250);
    Serial.print('.');
  }
  Serial.println('\n');
  Serial.print("Connected to ");
  Serial.println(WiFi.SSID());              // Tell us what network we're connected to
  Serial.print("IP address:\t");
  Serial.println(WiFi.localIP());           // Send the IP address of the ESP8266 to the computer

  if (MDNS.begin("esp8266")) {              // Start the mDNS responder for esp8266.local
    Serial.println("mDNS responder started");
  } else {
    Serial.println("Error setting up MDNS responder!");
  }

  server.on("/", handleRoot);               // Call the 'handleRoot' function when a client requests URI "/"

  server.begin();                           // Actually start the server
  Serial.println("HTTP server started");

  //populate transitions array
}


// loop() function -- runs repeatedly as long as board is on ---------------

void loop() {




  server.handleClient();                    // Listen for HTTP requests from clients

}




void handleRoot() {
  
  //Wire.beginTransmission(4); // transmit to device #4
  //Wire.write(server.arg("plain").c_str());        // sends five bytes
  //Wire.write("esp test");
  //Wire.endTransmission();    // stop transmitting

  String s = server.arg("plain");
  char buffer[s.length()+1];
  s.toCharArray(buffer, s.length()+1);
  Serial.print(buffer);
  Wire.beginTransmission(4); // transmit to device #4
  Wire.write(buffer);        // sends five bytes
  Wire.endTransmission();    // stop transmitting
  
  server.send(200, "text/plain", "Hello world!");   // Send HTTP status 200 (Ok) and send some text to the browser/client
}

void handleNotFound(){
  server.send(404, "text/plain", "404: Not found"); // Send HTTP status 404 (Not Found) when there's no handler for the URI in the request
}

