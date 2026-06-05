// Código para Arduino Uno
// Sensor en pin 7

#define SENSOR_PIN 7
#define LED_PIN 13

int lastState = HIGH;
int currentState = HIGH;

void setup() {
  Serial.begin(9600);
  pinMode(SENSOR_PIN, INPUT_PULLUP);
  pinMode(LED_PIN, OUTPUT);
  Serial.println("ARDUINO_LISTO");
}

void loop() {
  currentState = digitalRead(SENSOR_PIN);
  
  if (currentState != lastState) {
    lastState = currentState;
    
    if (currentState == LOW) {
      digitalWrite(LED_PIN, HIGH);
      Serial.println("SENSOR:ACTIVADO");
    } else {
      digitalWrite(LED_PIN, LOW);
      Serial.println("SENSOR:NORMAL");
    }
  }
  
  delay(50);
}
