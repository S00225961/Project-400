import time
import smbus
import sys
import os
sys.path.append(os.path.abspath("library_files"))
from DFRobot_BloodOxygen_S import *

AHT20_I2C_ADDR = 0x38

CMD_INITIALIZE = [0xBE, 0x08, 0x00]

CMD_MEASURE = [0xAC, 0x33, 0x00]

bus = smbus.SMBus(1)

ctype=0

I2C_1       = 0x01               
I2C_ADDRESS = 0x57               
max30102 = DFRobot_BloodOxygen_S_i2c(I2C_1 ,I2C_ADDRESS)

def initialize_sensors():
    bus.write_i2c_block_data(AHT20_I2C_ADDR, 0, CMD_INITIALIZE)
    while (False == max30102.begin()):
        print("init fail!")
        time.sleep(1)
        print("start measuring...")
        max30102.sensor_start_collect()
    time.sleep(0.01)

def read_temp_and_humidity():
    bus.write_i2c_block_data(AHT20_I2C_ADDR, 0, CMD_MEASURE)
    time.sleep(0.1)
    data = bus.read_i2c_block_data(AHT20_I2C_ADDR, 0, 6)
    raw_humidity = ((data[1] << 16) | (data[2]) << 8 | data[3]) >> 4
    raw_temp = ((data[3] & 0x0F) << 16) | (data[4] << 8) | data[5]

    humidity = (raw_humidity / 1048576.0) * 100
    temp = (raw_temp / 1048576.0) * 200 - 50

    print(f"Temperature: {temp:.2f} Degress Celcius")
    print(f"Humidity: {humidity:.2f} %")
    print("_" * 30)

def read_spo2_and_heart_rate():
    max30102.get_heartbeat_SPO2()
    print("SPO2 is : "+str(max30102.SPO2)+"%") 
    print("heart rate is : "+str(max30102.heartbeat)+"Times/min")
    time.sleep(1)

while True:
    read_spo2_and_heart_rate()
    read_temp_and_humidity()
    time.sleep(1)