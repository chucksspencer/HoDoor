import sqlite3
import datetime
import json 
from gcmclient import *
from gcm import *

def convertArrayToNiceJsonArray(array):
	array = array.replace('"', '\'')
	array = array.replace('[', '{')
	array = array.replace(']', '}')
	array = array[1:-1]
	array = "[" + array + "]"
	array = array.replace('{\'', '{\'date\':\'')
	array = array.replace('\',', '\', \'state\':')
	return array

def getRecords(num):
	conn = sqlite3.connect('/home/pi/Documents/DoorMonitor/python/doordatabase.db')
	c = conn.cursor()
	c.execute('SELECT * from events ORDER BY \'eventtime\' DESC')
	response = c.fetchmany(int(num))
	conn.close()
	resultArray = json.dumps(response);
	resultArray = convertArrayToNiceJsonArray(resultArray)
	
	print resultArray;
	returnJson = resultArray
	return returnJson;

def testNotifications():
	lastFour = getRecords(4)
	print(lastFour)
	sendNotification(lastFour)
	return lastFour
	

testNotifications();