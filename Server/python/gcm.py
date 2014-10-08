from gcmclient import *
import json 

def sendNotification(dataIn):
	data = {'events' : dataIn}
	# Pass 'proxies' keyword argument, as described in 'requests' library if you
	# use proxies. Check other options too.
	gcm = GCM('AIzaSyD9cmIW6F0MHreDN61nGyukqTozOKlnvfw')

	# Unicast or multicast message, read GCM manual about extra options.
	# It is probably a good idea to always use JSONMessage, even if you send
	# a notification to just 1 registration ID.
	multicast = JSONMessage(["APA91bHWXZ1PzT36HwO3twX3uP_H7OlY96oQBkZ49wPV21xDVYD2v2W6fHVzmUdRHf1t6Qb8VpIG4Vldr7bQZJ87imrLR2KLpGYCgWDZqsElYGD_0oKDgRSLYQw4PxVx1MKSoivnvk2gInpziL-TGrIkMLnhtDg3Tal-XybH4fpGIJ12ZcIHVDU"], data, collapse_key='my.key', dry_run=False)

	try:
		# attempt send
		res_multicast = gcm.send(multicast)

		for res in [res_multicast]:
			# nothing to do on success
			for reg_id, msg_id in res.success.items():
				print ("Successfully sent %s as %s" % (reg_id, msg_id))

			# update your registration ID's
			for reg_id, new_reg_id in res.canonical.items():
				print ("Replacing %s with %s in database" % (reg_id, new_reg_id))

			# probably app was uninstalled
			for reg_id in res.not_registered:
				print ("Removing %s from database" % reg_id)

			# unrecoverably failed, these ID's will not be retried
			# consult GCM manual for all error codes
			for reg_id, err_code in res.failed.items():
				print ("Removing %s because %s" % (reg_id, err_code))

			# if some registration ID's have recoverably failed
			if res.needs_retry():
				# construct new message with only failed regids
				retry_msg = res.retry()
				# you have to wait before attemting again. delay()
				# will tell you how long to wait depending on your
				# current retry counter, starting from 0.
				print ("Wait or schedule task after %s seconds" % res.delay(retry))
				# retry += 1 and send retry_msg again

	except GCMAuthenticationError:
		# stop and fix your settings
		print ("Your Google API key is rejected")
	except ValueError as e:
		# probably your extra options, such as time_to_live,
		# are invalid. Read error message for more info.
		print ("Invalid message/option or invalid GCM response")
		print (e.args[0])
	except Exception:
		# your network is down or maybe proxy settings
		# are broken. when problem is resolved, you can
		# retry the whole message.
		print ("Something wrong with requests library")
