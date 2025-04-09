import dbus
from advertisement import Advertisement
from service import Application, Service, Characteristic, Descriptor
import random
import time

GATT_CHRC_IFACE = "org.bluez.GattCharacteristic1"
NOTIFY_TIMEOUT = 5000

# Advertisement class
class SensorAdvertisement(Advertisement):
    def __init__(self, index):
        Advertisement.__init__(self, index, "peripheral")
        self.add_local_name("NeuroPiFitness")
        self.include_tx_power = True

# Service class for multiple characteristics
class SensorService(Service):
    SENSOR_SVC_UUID = "00000001-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, index):
        Service.__init__(self, index, self.SENSOR_SVC_UUID, True)

        # Explicitly initialize characteristics
        self.temp_char = TempCharacteristic(self)
        self.hr_char = HRCharacteristic(self)
        self.spo2_char = SpO2Characteristic(self)
        self.humidity_char = HumidityCharacteristic(self)

        # Add characteristics to the service
        self.add_characteristic(self.temp_char)
        self.add_characteristic(self.hr_char)
        self.add_characteristic(self.spo2_char)
        self.add_characteristic(self.humidity_char)

# Temperature Characteristic class
class TempCharacteristic(Characteristic):
    TEMP_CHARACTERISTIC_UUID = "00000002-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, service):
        self.notifying = False
        Characteristic.__init__(self, self.TEMP_CHARACTERISTIC_UUID, ["notify", "read"], service)
        self.add_descriptor(TempDescriptor(self))

    def get_temperature(self):
        temp = round(random.uniform(36.1, 37.8), 1)
        return [dbus.Byte(c.encode()) for c in str(temp) + " C"]

    def set_temperature_callback(self):
        if self.notifying:
            value = self.get_temperature()
            self.PropertiesChanged(GATT_CHRC_IFACE, {"Value": value}, [])
        return self.notifying

    def StartNotify(self):
        if self.notifying:
            return
        self.notifying = True
        self.PropertiesChanged(GATT_CHRC_IFACE, {"Value": self.get_temperature()}, [])
        self.add_timeout(NOTIFY_TIMEOUT, self.set_temperature_callback)

    def StopNotify(self):
        self.notifying = False

    def ReadValue(self, options):
        return self.get_temperature()

class TempDescriptor(Descriptor):
    TEMP_DESCRIPTOR_UUID = "2901"  # Changed UUID to be unique
    TEMP_DESCRIPTOR_VALUE = "Human Body Temperature"

    def __init__(self, characteristic):
        Descriptor.__init__(self, self.TEMP_DESCRIPTOR_UUID, ["read"], characteristic)

    def ReadValue(self, options):
        return [dbus.Byte(c.encode()) for c in self.TEMP_DESCRIPTOR_VALUE]

# Heart Rate Characteristic class
class HRCharacteristic(Characteristic):
    HR_CHARACTERISTIC_UUID = "00000003-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, service):
        self.notifying = False
        Characteristic.__init__(self, self.HR_CHARACTERISTIC_UUID, ["notify", "read"], service)
        self.add_descriptor(HRDescriptor(self))

    def get_heart_rate(self):
        heart_rate = random.randint(60, 100)
        return [dbus.Byte(c.encode()) for c in str(heart_rate)]

    def ReadValue(self, options):
        return self.get_heart_rate()

class HRDescriptor(Descriptor):
    HR_DESCRIPTOR_UUID = "2901"  # Changed UUID
    HR_DESCRIPTOR_VALUE = "Heart Rate"

    def __init__(self, characteristic):
        Descriptor.__init__(self, self.HR_DESCRIPTOR_UUID, ["read"], characteristic)

    def ReadValue(self, options):
        return [dbus.Byte(c.encode()) for c in self.HR_DESCRIPTOR_VALUE]

# SpO2 Characteristic class
class SpO2Characteristic(Characteristic):
    SPO2_CHARACTERISTIC_UUID = "00000004-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, service):
        self.notifying = False
        Characteristic.__init__(self, self.SPO2_CHARACTERISTIC_UUID, ["notify", "read"], service)
        self.add_descriptor(SpO2Descriptor(self))

    def get_spo2(self):
        spo2 = random.randint(95, 100)
        return [dbus.Byte(c.encode()) for c in str(spo2)]

    def ReadValue(self, options):
        return self.get_spo2()

class SpO2Descriptor(Descriptor):
    SPO2_DESCRIPTOR_UUID = "2901"  # Changed UUID
    SPO2_DESCRIPTOR_VALUE = "SpO2 Level"

    def __init__(self, characteristic):
        Descriptor.__init__(self, self.SPO2_DESCRIPTOR_UUID, ["read"], characteristic)

    def ReadValue(self, options):
        return [dbus.Byte(c.encode()) for c in self.SPO2_DESCRIPTOR_VALUE]

# Humidity Characteristic class
class HumidityCharacteristic(Characteristic):
    HUMIDITY_CHARACTERISTIC_UUID = "00000005-710e-4a5b-8d75-3e5b444bc3cf"

    def __init__(self, service):
        self.notifying = False
        Characteristic.__init__(self, self.HUMIDITY_CHARACTERISTIC_UUID, ["notify", "read"], service)
        self.add_descriptor(HumidityDescriptor(self))

    def get_humidity(self):
        humidity = random.randint(40, 60)
        return [dbus.Byte(c.encode()) for c in str(humidity)]

    def ReadValue(self, options):
        return self.get_humidity()

class HumidityDescriptor(Descriptor):
    HUMIDITY_DESCRIPTOR_UUID = "2901"  # Changed UUID
    HUMIDITY_DESCRIPTOR_VALUE = "Humidity Level"

    def __init__(self, characteristic):
        Descriptor.__init__(self, self.HUMIDITY_DESCRIPTOR_UUID, ["read"], characteristic)

    def ReadValue(self, options):
        return [dbus.Byte(c.encode()) for c in self.HUMIDITY_DESCRIPTOR_VALUE]

# Main Application
app = Application()
app.add_service(SensorService(0))
app.register()

adv = SensorAdvertisement(0)
adv.register()

try:
    app.run()
except KeyboardInterrupt:
    app.quit()
