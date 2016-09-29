# VerifyActivityRecoService
Sample Activity Recognition Service

Working sample of an Android Service that performs Activity Recognition API.  
Service calls location services when criteria is met.

ActivityDetectService:onStartCommand is called because it's registered in
GooglePlay:getActivityDetectionPendingIntent.  

Intent if passed to the handler which broadcasts the activity information to 
the LocationDbProcessor.  

Tested by moving app to the background and verifing that the activity events
still received.
