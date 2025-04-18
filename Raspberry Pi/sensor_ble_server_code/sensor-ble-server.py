import dbus
import time
import smbus
import sys
import os
script_dir = os.path.dirname(os.path.abspath(__file__))
library_dir = os.path.join(script_dir, "library_files")
sys.path.append(library_dir)
from DFRobot_BloodOxygen_S import *
from advertisement import Advertisement
from service import Application, Service, Characteristic, Descriptor


# Bluetooth Constants
GATT_CHRC_IFACE = "org.bluez.GattCharacteristic1"
NOTIFY_TIMEOUT = 5000

# Sensor Setup
AHT20_I2C_ADDR = 0x38
CMD_INITIALIZE = [0xBE, 0x08, 0x00]
CMD_MEASURE = [0xAC, 0x33, 0x00]
bus = smbus.SMBus(1)

I2C_1 = 0x01
I2C_ADDRESS = 0x57
max30102 = DFRobot_BloodOxygen_S_i2c(I2C_1, I2C_ADDRESS)

# Initialize Sensors
def initialize_sensors():
    bus.write_i2c_block_data(AHT20_I2C_ADDR, 0, CMD_INITIALIZE)
    while not max30102.begin():
        print("[ERROR] MAX30102 sensor failed to initialize. Retrying...")
        time.sleep(1)
    
    print("[INFO] Sensors initialized successfully!")
    max30102.sensor_start_collect()

# Get Temperature & Humidity
def get_temp_humidity():
    bus.write_i2c_block_data(AHT20_I2C_ADDR, 0, CMD_MEASURE)
    time.sleep(0.1)
    data = bus.read_i2c_block_data(AHT20_I2C_ADDR, 0, 6)
    
    raw_humidity = ((data[1] << 16) | (data[2] << 8) | data[3]) >> 4
    raw_temp = ((data[3] & 0x0F) << 16) | (data[4] << 8) | data[5]

    humidity = round((raw_humidity / 1048576.0) * 100, 1)
    temp = round((raw_temp / 1048576.0) * 200 - 50, 1)

    # Log sensor data
    print(f"[INFO] Temperature: {temp}°C | Humidity: {humidity}%")

    return temp, humidity

# Get SpO2 & Heart Rate
def get_spo2_hr():
    max30102.get_heartbeat_SPO2()
    spo2 = max30102.SPO2
    hr = max30102.heartbeat

    # Log sensor data
    print(f"[INFO] SpO2: {spo2}% | Heart Rate: {hr} BPM")

    return spo2, hr


# BLE Advertisement
class SensorAdvertisement(Advertisement):
    def __init__(self, index):
        Advertisement.__init__(self, index, "peripheral")
        self.add_local_name("NeuroPiFitness")
        self.include_tx_power = True


# BLE Service
class SensorService(Service):
    SENSOR_SVC_UUID = "00000001-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, index):
        Service.__init__(self, index, self.SENSOR_SVC_UUID, True)
        self.add_characteristic(TempCharacteristic(self))
        self.add_characteristic(HRCharacteristic(self))
        self.add_characteristic(SpO2Characteristic(self))
        self.add_characteristic(HumidityCharacteristic(self))


# BLE Characteristics
class TempCharacteristic(Characteristic):
    UUID = "00000002-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, service):
        Characteristic.__init__(self, self.UUID, ["notify", "read"], service)

    def ReadValue(self, options):
        temp, _ = get_temp_humidity()
        return [dbus.Byte(c.encode()) for c in str(temp) + " C"]


class HRCharacteristic(Characteristic):
    UUID = "00000003-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, service):
        Characteristic.__init__(self, self.UUID, ["notify", "read"], service)

    def ReadValue(self, options):
        _, hr = get_spo2_hr()
        return [dbus.Byte(c.encode()) for c in str(hr)]


class SpO2Characteristic(Characteristic):
    UUID = "00000004-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, service):
        Characteristic.__init__(self, self.UUID, ["notify", "read"], service)

    def ReadValue(self, options):
        spo2, _ = get_spo2_hr()
        return [dbus.Byte(c.encode()) for c in str(spo2)]


class HumidityCharacteristic(Characteristic):
    UUID = "00000005-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, service):
        Characteristic.__init__(self, self.UUID, ["notify", "read"], service)

    def ReadValue(self, options):
        _, humidity = get_temp_humidity()
        return [dbus.Byte(c.encode()) for c in str(humidity)]


# Start BLE Server
print("[INFO] Starting BLE Server...")
initialize_sensors()
app = Application()
app.add_service(SensorService(0))
app.register()

adv = SensorAdvertisement(0)
adv.register()

try:
    print("[INFO] BLE Server is now running. Waiting for connections...")
    app.run()
except KeyboardInterrupt:
    print("[INFO] Shutting down BLE Server.")
    app.quit()
