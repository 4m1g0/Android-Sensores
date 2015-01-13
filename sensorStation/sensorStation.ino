#include <dht11.h>
#include <Wire.h>
#include <SoftwareSerial.h>
#include <LiquidCrystal.h>
#include <Ethernet.h>
#include <SPI.h>
#include <Servo.h> 
 


#define BMP085_ADDRESS 0x77  // I2C address of BMP085

#define DHTLIB_OK 0
#define DHTLIB_ERROR_CHECKSUM -1
#define DHTLIB_ERROR_TIMEOUT -2

const unsigned char OSS = 0;  // Oversampling Setting

Servo myservo;

// Calibration values
int ac1;
int ac2; 
int ac3; 
unsigned int ac4;
unsigned int ac5;
unsigned int ac6;
int b1; 
int b2;
int mb;
int mc;
int md;

// b5 is calculated in bmp085GetTemperature(...), this variable is also used in bmp085GetPressure(...)
// so ...Temperature(...) must be called before ...Pressure(...).
long b5; 

short temperature;
long pressure;

// Use these for altitude conversions
const float p0 = 101325;     // Pressure at sea level (Pa)
float altitude;

int LDR = A3;
int luminosity = 0;

int humidity = 0;

int NPIN = A1;
int noise = 0;

#define DHT11PIN A2
dht11 dht112(DHT11PIN);

LiquidCrystal lcd(8, 9, 5, 4, 3, 2);

byte mac[] = { 
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED};
IPAddress ip(192,168,0,88);
IPAddress gateway(192,168,0,1);
IPAddress subnet(255, 255, 255, 0);
EthernetServer server(4952);

unsigned long loopTimer = 0, sensorsTimer=0, lcdTimer = 0;
int SENSORS_TIME = 3000;
#define LCD_TIME 5000
#define TAMSTRING 15
int lcdActual = 0;
int encender = 0;
int led = 13;
int contador=0;

void setup()
{
  Serial.begin(9600);
  lcd.begin(16, 2);
  lcd.clear();
  
  pinMode(led, OUTPUT);
  
  //Wire.begin();
  //bmp085Calibration();
  pinMode(LDR, INPUT);
  pinMode(DHT11PIN, INPUT);
  pinMode(NPIN, INPUT);
  //Ethernet.begin(mac, ip);
  //server.begin();
  //showLoading();
  
  myservo.attach(9);
}

void loop()
{
  loopTimer = millis();
  //lcd.setCursor(0, 0);
  //lcd.print("loop");
  if ((millis()-sensorsTimer) > SENSORS_TIME){
     sensorsTimer=millis();
     calculateTemperature();
     calculatePressure();
     calculateAltitude();
     calculateLuminosity();
     calculateHumidity();
     calculateNoise();
     
     /*Serial.print("R:");
     Serial.print(random(0,255), DEC);
     Serial.println(";");
     */
     Serial.println(tamanoMsg("R:" + random(0,255) + ";"),TAMSTRING )

     /*
     Serial.print("C:");
     Serial.print(contador++, DEC);
     Serial.println(";");
     */

     Serial.println(tamanoMsg("C:" + contador++ + ";"),TAMSTRING )
     
     
     
     
     if (encender == 0){
       //Serial.println("LED:L;");
       Serial.println(tamanoMsg("LED:L;"),TAMSTRING )
     }else{
       //Serial.println("LED:H;");
       Serial.println(tamanoMsg("LED:H;")TAMSTRING )
     }
  }
  
  if (Serial.available() > 0){
       char command = Serial.read();
       int angle;
       switch (command) {
            case 'L':
                encender = Serial.parseInt();
                if (encender == 0) {
                    digitalWrite(led, LOW);
                } else{
                    digitalWrite(led,HIGH);
                }
                break;
            case 'S':
                angle = Serial.parseInt();
                myservo.write(constrain(angle, 0, 180));
                break;
            case 'T':
                SENSORS_TIME = constrain(Serial.parseInt(), 1, 5000);
                break;
       }
       
  }
  
  /*if (0) //((millis()-lcdTimer) > LCD_TIME)
  {
    lcdTimer=millis();
    switch(lcdActual){
      case 0:
        showTemperature();
        lcdActual++;
        break;
      case 1:
        showPressure();
        lcdActual++;
        break;
      case 2:
        showAltitude();
        lcdActual++;
        break;
      case 3:
        showLuminosity(); 
        lcdActual++;
        break;
      case 4:
        showHumidity();
        lcdActual++;
        break;
      case 5:
        showNoise();
        lcdActual=0;
        break; 
      default:
        showError();
    }
  }
  //listenForEthernetClients();*/
}

void calculateTemperature(){
  temperature = 2;//bmp085GetTemperature(bmp085ReadUT())/10;
  /*Serial.print("T:");
  Serial.print(temperature, DEC);
  Serial.println(";");
  */
  Serial.println(tamanoMsg("T:" + temperature + ";"),TAMSTRING )
}

string tamanoMsg(string msg, int tam){
    string salida = "";
    int final = tam -strlen(msg);
    for(int i; i<tam;i++){
        salida= salida + " ";
    }
    return msg + salida;
}

void showTemperature(){
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Temperature (C):");
  lcd.setCursor(0, 1);
  lcd.print(temperature);
}

void calculatePressure(){
  pressure = 5;//bmp085GetPressure(bmp085ReadUP());
  /*Serial.print("P:");
  Serial.print(pressure/100, DEC);
  Serial.println(";");
  */
  Serial.println(tamanoMsg("P:" + pressure/100 + ";"),TAMSTRING )
}   

void showPressure(){
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Pressure (hPa):");
  lcd.setCursor(0, 1);
  lcd.print(pressure/100);
}

void calculateAltitude(){
  altitude = (float)44330 * (1 - pow(((float) pressure/p0), 0.190295));
  /*Serial.print("A:");
  Serial.print(altitude, 2);
  Serial.println(";");
  */
  Serial.println(tamanoMsg("A:" + altitude + ";"),TAMSTRING )
}

void showAltitude(){ 
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Altitude (m):");
  lcd.setCursor(0, 1);
  lcd.print(altitude);
}

void calculateLuminosity(){
  luminosity=map(analogRead(LDR),0,1023,100,0);
  /*Serial.print("L:");
  Serial.print(luminosity);
  Serial.println(";");
  */
  Serial.println(tamanoMsg("L:" + luminisity + ";"),TAMSTRING )
}

void showLuminosity(){
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Luminosity (%):");
  lcd.setCursor(0, 1);
  lcd.print(luminosity);
}

void calculateHumidity(){
  humidity = dht112.read();
  /*Serial.print("H:");
  Serial.print(humidity);
  Serial.println(";");
  */
  Serial.println(tamanoMsg("H:" + humidity + ";"),TAMSTRING )
}

void showHumidity(){
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Humidity (%):");
  lcd.setCursor(0, 1);
  lcd.print(humidity);
}

void calculateNoise(){
  noise=map(analogRead(NPIN),0,1023,0,100);  
  /*Serial.print("N:");
  Serial.print(noise);
  Serial.println(";");
  */
  Serial.println(tamanoMsg("N:" + noise + ";"),TAMSTRING )
}

void showNoise(){
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Noise (%):");
  lcd.setCursor(0, 1);
  lcd.print(noise);
}

void showLoading(){
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Loading...");
  lcd.setCursor(0, 1);
  lcd.print(":-)");
}

void showError(){
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("ERROR");
  lcd.setCursor(0, 1);
  lcd.print(":-(");
}

/*void listenForEthernetClients() {
  // listen for incoming clients
  EthernetClient client = server.available();
  if (client) {
    Serial.println("Got a client");
    // an http request ends with a blank line
    boolean currentLineIsBlank = true;
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        // if you've gotten to the end of the line (received a newline
        // character) and the line is blank, the http request has ended,
        // so you can send a reply
        if (c == '\n' && currentLineIsBlank) {
          // send a standard http response header
          client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: text/html");
          client.println();
          client.print("Temperature: ");
          client.print(temperature);
          client.println(" C");
          client.println("<br />");
          client.print("Pressure: ");
          client.print(pressure/100);
          client.println(" hPa");
          client.println("<br />");
          client.print("Altitude: ");
          client.print(altitude);
          client.println(" m");
          client.println("<br />"); 
          client.print("Luminosity: ");
          client.print(luminosity);
          client.println(" %"); 
          client.println("<br />");
          client.print("Humidity: ");
          client.print(humidity);
          client.println(" %");
          client.println("<br />");
          client.print("Noise: ");
          client.print(noise);
          client.println(" %");
          client.println("<br />");
          break;
        }
        if (c == '\n') {
          // you're starting a new line
          currentLineIsBlank = true;
        } 
        else if (c != '\r') {
          // you've gotten a character on the current line
          currentLineIsBlank = false;
        }
      }
    }
    // give the web browser time to receive the data
    delay(1);
    // close the connection:
    client.stop();
  }
} */


// Stores all of the bmp085's calibration values into global variables
// Calibration values are required to calculate temp and pressure
// This function should be called at the beginning of the program
/*void bmp085Calibration()
{
  ac1 = bmp085ReadInt(0xAA);
  ac2 = bmp085ReadInt(0xAC);
  ac3 = bmp085ReadInt(0xAE);
  ac4 = bmp085ReadInt(0xB0);
  ac5 = bmp085ReadInt(0xB2);
  ac6 = bmp085ReadInt(0xB4);
  b1 = bmp085ReadInt(0xB6);
  b2 = bmp085ReadInt(0xB8);
  mb = bmp085ReadInt(0xBA);
  mc = bmp085ReadInt(0xBC);
  md = bmp085ReadInt(0xBE);
}*/

// Calculate temperature given ut.
// Value returned will be in units of 0.1 deg C
short bmp085GetTemperature(unsigned int ut)
{
  long x1, x2;
  
  x1 = (((long)ut - (long)ac6)*(long)ac5) >> 15;
  x2 = ((long)mc << 11)/(x1 + md);
  b5 = x1 + x2;

  return ((b5 + 8)>>4);  
}

// Calculate pressure given up
// calibration values must be known
// b5 is also required so bmp085GetTemperature(...) must be called first.
// Value returned will be pressure in units of Pa.
long bmp085GetPressure(unsigned long up)
{
  long x1, x2, x3, b3, b6, p;
  unsigned long b4, b7;
  
  b6 = b5 - 4000;
  // Calculate B3
  x1 = (b2 * (b6 * b6)>>12)>>11;
  x2 = (ac2 * b6)>>11;
  x3 = x1 + x2;
  b3 = (((((long)ac1)*4 + x3)<<OSS) + 2)>>2;
  
  // Calculate B4
  x1 = (ac3 * b6)>>13;
  x2 = (b1 * ((b6 * b6)>>12))>>16;
  x3 = ((x1 + x2) + 2)>>2;
  b4 = (ac4 * (unsigned long)(x3 + 32768))>>15;
  
  b7 = ((unsigned long)(up - b3) * (50000>>OSS));
  if (b7 < 0x80000000)
    p = (b7<<1)/b4;
  else
    p = (b7/b4)<<1;
    
  x1 = (p>>8) * (p>>8);
  x1 = (x1 * 3038)>>16;
  x2 = (-7357 * p)>>16;
  p += (x1 + x2 + 3791)>>4;
  
  return p;
}

// Read 1 byte from the BMP085 at 'address'
/*char bmp085Read(unsigned char address)
{
  unsigned char data;
  
  Wire.beginTransmission(BMP085_ADDRESS);
  Wire.write(address);
  Wire.endTransmission();
  
  Wire.requestFrom(BMP085_ADDRESS, 1);
  while(!Wire.available())
    ;
    
  return Wire.read();
}*/

// Read 2 bytes from the BMP085
// First byte will be from 'address'
// Second byte will be from 'address'+1
/*int bmp085ReadInt(unsigned char address)
{
  unsigned char msb, lsb;
  
  Wire.beginTransmission(BMP085_ADDRESS);
  Wire.write(address);
  Wire.endTransmission();
  
  Wire.requestFrom(BMP085_ADDRESS, 2);
  while(Wire.available()<2)
    ;
  msb = Wire.read();
  lsb = Wire.read();
  
  return (int) msb<<8 | lsb;
}

// Read the uncompensated temperature value
unsigned int bmp085ReadUT()
{
  unsigned int ut;
  
  // Write 0x2E into Register 0xF4
  // This requests a temperature reading
  Wire.beginTransmission(BMP085_ADDRESS);
  Wire.write(0xF4);
  Wire.write(0x2E);
  Wire.endTransmission();
  
  // Wait at least 4.5ms
  delay(5);
  
  // Read two bytes from registers 0xF6 and 0xF7
  ut = bmp085ReadInt(0xF6);
  return ut;
}

// Read the uncompensated pressure value
unsigned long bmp085ReadUP()
{
  unsigned char msb, lsb, xlsb;
  unsigned long up = 0;
  
  // Write 0x34+(OSS<<6) into register 0xF4
  // Request a pressure reading w/ oversampling setting
  Wire.beginTransmission(BMP085_ADDRESS);
  Wire.write(0xF4);
  Wire.write(0x34 + (OSS<<6));
  Wire.endTransmission();
  
  // Wait for conversion, delay time dependent on OSS
  delay(2 + (3<<OSS));
  
  // Read register 0xF6 (MSB), 0xF7 (LSB), and 0xF8 (XLSB)
  Wire.beginTransmission(BMP085_ADDRESS);
  Wire.write(0xF6);
  Wire.endTransmission();
  Wire.requestFrom(BMP085_ADDRESS, 3);
  
  // Wait for data to become available
  while(Wire.available() < 3)
    ;
  msb = Wire.read();
  lsb = Wire.read();
  xlsb = Wire.read();
  
  up = (((unsigned long) msb << 16) | ((unsigned long) lsb << 8) | (unsigned long) xlsb) >> (8-OSS);
  
  return up;
}*/

//Celsius to Fahrenheit conversion
double Fahrenheit(double celsius)
{
        return 1.8 * celsius + 32;
}

// fast integer version with rounding
//int Celcius2Fahrenheit(int celcius)
//{
//  return (celsius * 18 + 5)/10 + 32;
//}


//Celsius to Kelvin conversion
double Kelvin(double celsius)
{
        return celsius + 273.15;
}

// dewPoint function NOAA
// reference (1) : http://wahiduddin.net/calc/density_algorithms.htm
// reference (2) : http://www.colorado.edu/geography/weather_station/Geog_site/about.htm
//
double dewPoint(double celsius, double humidity)
{
        // (1) Saturation Vapor Pressure = ESGG(T)
        double RATIO = 373.15 / (273.15 + celsius);
        double RHS = -7.90298 * (RATIO - 1);
        RHS += 5.02808 * log10(RATIO);
        RHS += -1.3816e-7 * (pow(10, (11.344 * (1 - 1/RATIO ))) - 1) ;
        RHS += 8.1328e-3 * (pow(10, (-3.49149 * (RATIO - 1))) - 1) ;
        RHS += log10(1013.246);

        // factor -3 is to adjust units - Vapor Pressure SVP * humidity
        double VP = pow(10, RHS - 3) * humidity;

        // (2) DEWPOINT = F(Vapor Pressure)
        double T = log(VP/0.61078);   // temp var
        return (241.88 * T) / (17.558 - T);
}

// delta max = 0.6544 wrt dewPoint()
// 6.9 x faster than dewPoint()
// reference: http://en.wikipedia.org/wiki/Dew_point
double dewPointFast(double celsius, double humidity)
{
        double a = 17.271;
        double b = 237.7;
        double temp = (a * celsius) / (b + celsius) + log(humidity*0.01);
        double Td = (b * temp) / (a - temp);
        return Td;
}


