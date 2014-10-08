import webiopi
import sqlite3
import datetime
import json 


conn = sqlite3.connect('doordatabase.db')
c = conn.cursor()
c.execute('SELECT * from events')
response = c.fetchall()
conn.close()
res = json.dumps(response);
print(res)