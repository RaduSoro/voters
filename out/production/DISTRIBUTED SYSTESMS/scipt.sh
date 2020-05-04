 #!/bin/sh

 java UDPLoggerServer 12344 &
 echo " Waiting  for  logger  server  to  start ... "
 sleep 5
 java Coordinator 12345 4 500 A B &

 echo " Waiting  for  coordinator to start ... "
 sleep 5
 java Participant 12345 12346 500 &
 sleep 1
 java Participant 12345 12347 500 &
 sleep 1
 java Participant 12345 12348 500 &
 sleep 1
 java Participant 12345 12349 500 &
