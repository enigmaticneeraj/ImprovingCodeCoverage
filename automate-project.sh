#!/bin/bash
echo "==============Clear Tomcat Logs================="
echo trmhmrh | sudo -S rm /var/lib/tomcat6/logs/localhost_access_log.2013-12-16.txt
echo trmhmrh | sudo -S /etc/init.d/tomcat6 restart
cd /home/neeraj/bookstore
ant automate-coverage-project