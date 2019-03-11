import os
import RPi.GPIO as GPIO
import time
TEST_SERVO = 15 #change this
GPIO.setmode(GPIO.BOARD)
GPIO.setup(TEST_SERVO,GPIO.OUT)
pwm=GPIO.PWM(TEST_SERVO,50)
pwm.start(6)
time.sleep(1)
pwm.ChangeDutyCycle(0)

p=os.popen('/usr/bin/zbarcam /dev/video0', 'r')
i=0
while True:
    code = p.readline()
    print ('Got barcode:', code)
    i+=1
    if(i>=4):
        pwm.ChangeDutyCycle(1)
        time.sleep(1)
        pwm.ChangeDutyCycle(0)
        time.sleep(4)
        pwm.ChangeDutyCycle(6)
        time.sleep(8)
        pwm.ChangeDutyCycle(0)
        i=0
