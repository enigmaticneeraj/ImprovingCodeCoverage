#!/bin/bash
echo "==============Run TestSuite================="
cd /home/neeraj/Project/ImproveCoverage
./wgetTestSuite.sh
wget http://localhost:8080/bookstore/WriteCoberturaCoverage.jsp --directory-prefix /home/neeraj/wget-output
cd /home/neeraj/cobertura
./cobertura-merge.sh /var/lib/tomcat6/cobertura.ser /tmp/cobertura/sb-initial-bookstore.ser
./cobertura-report.sh --format xml --srcdir /home/neeraj/bookstore/src --destination project_report
./cobertura-report.sh --srcdir /home/neeraj/bookstore/src --destination project_report/report_old
gnome-open project_report/report_old/index.html

echo "==============Removing instrumentation code================="
cd /home/neeraj/bookstore
ant compile

echo "=================Guide The Testing================="
cd /home/neeraj/Project/ImproveCoverage
mvn exec:java -Dexec.mainClass="com.StartGuiding" -Dexec.args="/var/lib/tomcat6/logs/localhost_access_log.2013-12-16.txt 
																		    /home/neeraj/bookstore/analysis/interfaces/bookstore-wamai-pda-interfaces.xml 
																	       	    /home/neeraj/Project/ImproveCoverage/wgetTestSuite.sh 
																		    /home/neeraj/Project/ImproveCoverage/augmentedTestSuite.sh 
																		      http://localhost:8080/bookstore/ 
																		      WriteCoberturaCoverage.jsp 
																		    /home/neeraj/bookstore/src/org/apache/jsp/ 
																	            /home/neeraj/bookstore/web/WEB-INF/classes/org/apache/jsp/ 
																		    /home/neeraj/cobertura/project_report/coverage.xml"
echo "==========Run Augmented TestSuite==========="
cd /home/neeraj/bookstore
ant install
cd /home/neeraj/Project/ImproveCoverage
echo trmhmrh | sudo -S chmod 777 augmentedTestSuite.sh
./augmentedTestSuite.sh
wget http://localhost:8080/bookstore/WriteCoberturaCoverage.jsp --directory-prefix /home/neeraj/wget-output
cd /home/neeraj/cobertura
./cobertura-merge.sh /var/lib/tomcat6/cobertura.ser /tmp/cobertura/sb-initial-bookstore.ser
./cobertura-report.sh --srcdir /home/neeraj/bookstore/src --destination project_report/report_new
gnome-open project_report/report_new/index.html