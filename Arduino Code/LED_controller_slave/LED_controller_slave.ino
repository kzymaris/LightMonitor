#include <Wire.h>
#include <ArduinoJson.h>
#include <Adafruit_NeoPixel.h>

#define LED_PIN 12
#define LED_COUNT 150

Adafruit_NeoPixel strip(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);

//fade variables
int transIndex = 0; 
bool up = true; 
bool doFade = false; 
int x0, y0, z0, x1, y1, z1;
int dx, dy, dz, dm, sx, sy, sz; 

//rainbow variables
bool doRainbow = false; 
int rainbowIndex = 0;

bool doPride = false; 
int prideIndex = 0;

//snake variables
int offSet = 0;
bool doSnake = false;

unsigned long previousMillis = 0;        // will store last time transition was made
String r1, g1, b1, r2, g2, b2, wait;


void setup()
{
  strip.begin();           // INITIALIZE NeoPixel strip object (REQUIRED)
  strip.show();            // Turn OFF all pixels ASAP
  strip.setBrightness(255); // Set BRIGHTNESS to about 1/5 (max = 255)
  
  Wire.begin(4);                // join i2c bus with address #4
  Wire.onReceive(receiveEvent); // register event
  
  Serial.begin(9600);           // start serial for output
  Serial.print("started");
}

void loop()
{
  unsigned long currentMillis = millis();

  if (currentMillis - previousMillis >= wait.toInt() && doFade) {
    // save the last time a transition was made
    previousMillis = currentMillis;

    uint32_t transition = strip.gamma32(strip.Color(x0, y0, z0));
    Serial.println(x0);
    Serial.println(y0);
    Serial.println(z0);
    Serial.println("---------");
    x1 -= dx; if (x1 < 0) { x1 += dm; x0 += sx; } 
    y1 -= dy; if (y1 < 0) { y1 += dm; y0 += sy; } 
    z1 -= dz; if (z1 < 0) { z1 += dm; z0 += sz; } 
 
    for(int i=0; i<strip.numPixels(); i++) { // For each pixel in strip...
      strip.setPixelColor(i, transition);         //  Set pixel's color (in RAM)
    }
    strip.show();
    if(up){
      transIndex++;
      if(transIndex == dm-1){
        up = false;
        fade(up);
      }
    }else{
      transIndex--;
      if(transIndex == 0){
        up = true;
        fade(up);
      }
    }
  }

  if (currentMillis - previousMillis >= wait.toInt() && doRainbow) {
    previousMillis = currentMillis;
    for(int i=0; i<strip.numPixels(); i++) { // For each pixel in strip...
      //int pixelHue = rainbowIndex + (i * 65536L / strip.numPixels());
      strip.setPixelColor(i, strip.gamma32(strip.ColorHSV(rainbowIndex)));
    }
    strip.show(); // Update strip with new contents
    rainbowIndex+=256;
    if(rainbowIndex >= 5*65536){
      rainbowIndex = 0;
    }
  }

  if (currentMillis - previousMillis >= wait.toInt() && doPride) {
    previousMillis = currentMillis;
    for(int i=0; i<strip.numPixels(); i++) { // For each pixel in strip...
      int pixelHue = prideIndex + (i * 65536L / strip.numPixels());
      strip.setPixelColor(i, strip.gamma32(strip.ColorHSV(pixelHue)));
    }
    strip.show(); // Update strip with new contents
    prideIndex+=256;
    if(prideIndex >= 5*65536){
      prideIndex = 0;
    }
  }

  if (currentMillis - previousMillis >= wait.toInt() && doSnake) {
    previousMillis = currentMillis;
    for(int i=0; i<strip.numPixels(); i++) { // turn off all pixels
      strip.setPixelColor(i, strip.Color(r2.toInt(),   g2.toInt(),   b2.toInt()));         
    }
    uint32_t color = strip.Color(r1.toInt(),   g1.toInt(),   b1.toInt());
    for(int i=0; i<10; i++) { 
      if(i+offSet < strip.numPixels()){
        strip.setPixelColor(i+offSet, strip.gamma32(color));
      } else {
        int wrap = strip.numPixels() - offSet;
        strip.setPixelColor(i-wrap, strip.gamma32(color));
      }      
    }
    strip.show(); // Update strip with new contents
    offSet++;
    if(offSet >= strip.numPixels()){
      offSet = 0;
    }
  }

  if(currentMillis - previousMillis >= 100 && !doRainbow && !doFade && !doSnake && !doPride){
    previousMillis = currentMillis;
    uint32_t color = strip.Color(r1.toInt(),   g1.toInt(),   b1.toInt());
    Serial.print(color);
    for(int i=0; i<strip.numPixels(); i++) { // For each pixel in strip...
      strip.setPixelColor(i, strip.gamma32(color));         //  Set pixel's color (in RAM)
    }
    strip.show();    
  }
}

// function that executes whenever data is received from master
// this function is registered as an event, see setup()
void receiveEvent(int howMany)
{
  String input;
  while(Wire.available()) // loop through all but the last
  {
    char c = Wire.read(); // receive byte as a character
    Serial.print(c);
    input += c;
  }
 

  const size_t capacity = JSON_OBJECT_SIZE(6) + 70;
  DynamicJsonDocument doc(capacity);
  
  const char* json = input.c_str();

  deserializeJson(doc, json);
  const char* t = doc["t"];
  const char* s = doc["s"];
  Serial.print(s);
  String type = t;
  wait = s;
  Serial.print(wait);
  if(type != "speed"){
    doFade = false;
    doRainbow = false;
    doSnake = false;
    doPride = false;
  }

  if(type == "wipe"){
    Serial.print("wipe");
    const char* red1 = doc["r1"]; 
    const char* green1 = doc["g1"]; 
    const char* blue1 = doc["b1"]; 
    r1 = red1;
    g1 = green1;
    b1 = blue1;
  } else if(type == "fade"){
    Serial.print("fade");
    const char* red1 = doc["r1"]; 
    const char* green1 = doc["g1"]; 
    const char* blue1 = doc["b1"]; 
    const char* red2 = doc["r2"]; 
    const char* green2 = doc["g2"]; 
    const char* blue2 = doc["b2"]; 
    r1 = red1;
    g1 = green1;
    b1 = blue1;
    r2 = red2;
    g2 = green2;
    b2 = blue2;
    doFade = true;
    fade(true);
  } else if(type == "rainbow"){
    Serial.print("rainbow");
    doRainbow = true;
  } else if(type == "snake"){
    Serial.print("snake");
    const char* red1 = doc["r1"]; 
    const char* green1 = doc["g1"]; 
    const char* blue1 = doc["b1"]; 
    const char* red2 = doc["r2"]; 
    const char* green2 = doc["g2"]; 
    const char* blue2 = doc["b2"]; 
    r1 = red1;
    g1 = green1;
    b1 = blue1;
    r2 = red2;
    g2 = green2;
    b2 = blue2;
    doSnake = true; 
  } else if(type == "off"){
    Serial.print("off");
    r1 = "0";
    g1 = "0";
    b1 = "0";
  } else if(type == "pride"){
    Serial.print("pride");
    doPride = true;
  }
  

}

void fade(bool fadeUp) {
    if(fadeUp){
      x0 = r1.toInt();
      y0 = g1.toInt();
      z0 = b1.toInt();
      x1 = r2.toInt();
      y1 = g2.toInt();
      z1 = b2.toInt();
    } else{
      x0 = r2.toInt();
      y0 = g2.toInt();
      z0 = b2.toInt();
      x1 = r1.toInt();
      y1 = g1.toInt();
      z1 = b1.toInt();
    }

    dx = abs(x1-x0), sx = x0<x1 ? 1 : -1;
    dy = abs(y1-y0), sy = y0<y1 ? 1 : -1; 
    dz = abs(z1-z0), sz = z0<z1 ? 1 : -1; 
    dm = max3(dx,dy,dz); /* maximum difference */
    x1 = dm/2;
    y1 = dm/2;
    z1 = dm/2; /* error offset */
  
}

int max3(int a, int b, int c)
{
  int maxguess;

  maxguess = max(a,b);  // biggest of A and B
  maxguess = max(maxguess, c);  // but maybe C is bigger?

  return(maxguess);
}

