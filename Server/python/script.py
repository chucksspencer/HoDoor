import sys
sys.path.insert(0, '/home/pi/Documents/DoorMonitor/python')
import webiopi
import sqlite3
import datetime
import time
import json 
import os
import subprocess
from gcm import *

# Set to True to enable automatic notifications when the door is open for the time
# specified as OPENWARNINGTIMEINMINS
AUTONOTIFICATIONS = False

# The frequency with which to send notifications if the door is open, in minutes
# if AUTONOTIFICATIONS is set to True
OPENWARNINGTIMEINMINS = 15

# The location to store captured images
IMAGEDIR = '/home/pi/Documents/DoorMonitor/html/ImageCapture/'

# The location of the database file
DBLOCATION = '/home/pi/Documents/DoorMonitor/python/doordatabase.db'

# Set to FALSE to disable pictures being taken when the door is opened
IMAGECAPTUREENABLED = True

# How long to wait (secs) before capturing the first image after the door is opened
# Meant to wait for the door to finish opening before capturing any images
IMAGECAPTUREDELAY = 8

# How many pictures to take when the door is opened
IMAGECAPTURENUM = 3

# The time (secs) between image captures when the door is opened
IMAGECAPTUREFREQUENCY = 3;

GPIO = webiopi.GPIO
SWITCHPIN = 2
DOOROPEN = GPIO.HIGH
DOORCLOSED = GPIO.LOW
lastRead = DOOROPEN
currentRead = DOOROPEN
SLEEPTIME = 1;
secondsOpen = 0;

# setup function is automatically called at WebIOPi startup
def setup():
	GPIO.setFunction(SWITCHPIN, GPIO.IN)
	lastRead = GPIO.digitalRead(SWITCHPIN)

# loop function is repeatedly called by WebIOPi 
def loop():
	global lastRead
	global currentRead
	global secondsOpen
	
	# Read the state of the switch
	currentRead = GPIO.digitalRead(SWITCHPIN)
	
	# If the state of the switch has changed, record the event
	if currentRead != lastRead:
		print('Door state has changed, recording event')
		recordEvent(currentRead)

	lastRead = currentRead

	# If the door is open, keep track of how long. Send out a notification every 15 mins until closed.
	if currentRead == DOOROPEN and AUTONOTIFICATIONS == True:
		secondsOpen += SLEEPTIME
		if secondsOpen > OPENWARNINGTIMEINMINS*60:
			print('Door has been open for ' + str(secondsOpen) + ' seconds. Sending notification')

			# Fetch the last two records to send off with the notification
			lastTwo = getRecords(2)
			sendNotification('{' + lastTwo + '}')
			
			# Reset the timer so we can send again in another 15 mins
			secondsOpen = 0
	else:
		secondsOpen = 0
		
	# gives CPU some time before looping again
	webiopi.sleep(SLEEPTIME)

def capturePictures(eventId):
	# Wait for specified delay time capture the specified number of images 
	# spaced out for the specified frequency 
	time.sleep(IMAGECAPTUREDELAY)
	for x in range(0, IMAGECAPTURENUM):
		# Create a timestamp for the image name
		currtime = datetime.datetime.now().time()
		baseFilename = time.strftime("%m%d%Y%H%M%S", time.gmtime()) + ".jpg"
		filename = IMAGEDIR + baseFilename
		
		# Write the image name/location to the DB, associated with the event to which it belongs
		conn=sqlite3.connect(DBLOCATION)
		curs=conn.cursor()
		curs.execute("INSERT INTO images values(null, " + str(eventId) + ", '" + baseFilename + "', datetime('now', 'localtime'))")
		print('The id is ' + str(curs.lastrowid))
		conn.commit()
		conn.close()
		
		# Capture the image
		cmd = 'raspistill -o ' + filename + ' -h 750 -w 1000 -t 100 -q 75 &'
		pid = subprocess.call(cmd, shell=True)
		
		# Wait before taking the next picture
		time.sleep(IMAGECAPTUREFREQUENCY)
		
@webiopi.macro
def getImagesForEvent(eventId):
	conn = sqlite3.connect(DBLOCATION)
	c = conn.cursor()
	c.execute('SELECT imageLocation from images WHERE eventId = ' + str(eventId) + ' ORDER BY \'created\' DESC')
	response = c.fetchall()
	conn.close()
	resultArray = json.dumps(response)
	return resultArray
	
def recordEvent(currentRead):
	global lastRead

	# write the event (state of the door and a timestamp) to the database
	conn=sqlite3.connect(DBLOCATION)
	curs=conn.cursor()
	if currentRead == DOOROPEN:
		print('Recording door event: OPEN')
		curs.execute("INSERT INTO events values(null, datetime('now', 'localtime'), 1)")
	else:
		print('Recording door event: CLOSED')
		curs.execute("INSERT INTO events values(null, datetime('now', 'localtime'), 0)")	
	eventId = curs.lastrowid
	conn.commit()
	conn.close()

	# Fetch the last two records to send off with the notification
	lastTwo = getRecords(2)
	sendNotification('{' + lastTwo + '}')

	# If the door was opened, capture images
	if(currentRead == DOOROPEN and IMAGECAPTUREENABLED):
		capturePictures(eventId);
	
def convertArrayToNiceJsonArray(array):
	array = array.replace('"', '\'')
	array = array.replace('[', '{')
	array = array.replace(']', '}')
	array = array[1:-1]
	array = "[" + array + "]"
	array = array.replace('{', '{\'eventId\':')
	array = array.replace(', \'', ', \'date\':\'')
	array = array.replace('\',', '\', \'state\':')
	return array

# REST call (also used in script) to retrieve event records from the DB and return JSON
@webiopi.macro
def getRecords(num):
	conn = sqlite3.connect(DBLOCATION)
	c = conn.cursor()
	c.execute('SELECT * from events ORDER BY eventtime DESC')
	response = c.fetchmany(int(num))
	conn.close()
	return(convertArrayToNiceJsonArray(json.dumps(response)))

@webiopi.macro
def getCurrentImage():
	currtime = datetime.datetime.now().time()
	baseFilename = time.strftime("%m%d%Y%H%M%S", time.gmtime()) + ".jpg"
	filename = IMAGEDIR + baseFilename
	
	# Capture the image
	cmd = 'raspistill -o ' + filename + ' -h 750 -w 1000 -t 100 -q 75'
	pid = subprocess.call(cmd, shell=True)

	return baseFilename
		
# REST call to register android devices for notifications with the service	
@webiopi.macro
def registerDevice(deviceId):
	print('adding to table: ' + deviceId);
	conn=sqlite3.connect(DBLOCATION)
	curs=conn.cursor()
	curs.execute("INSERT INTO registrations values('" + deviceId + "', datetime('now', 'localtime'))")
	conn.commit()
	conn.close()

@webiopi.macro
def testEventAdd(value):
	recordEvent(int(value));

@webiopi.macro
def testNotifications():
	lastFour = getRecords(4)
	print(lastFour)
	sendNotification(lastFour)
	return lastFour