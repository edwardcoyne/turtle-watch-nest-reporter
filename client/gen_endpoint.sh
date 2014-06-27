#!/bin/bash

dir=appengine-endpoints
v=1

rm -r $dir/*.jar 
cd $dir &&\
/Users/edcoyne/code/sdks/appengine-java-sdk-1.9.3/bin/endpoints.sh get-client-lib --war=../../server/TurtleNestReporter-AppEngine/war\
 com.islandturtlewatch.nest.reporter.backend.endpoints.ReportEndpoint com.islandturtlewatch.nest.reporter.backend.endpoints.ImageEndpoint &&\
for name in reportEndpoint imageEndpoint; do  
rm -r ${name}/sources/*;
rm ${name}/*;
unzip ${name}-v${v}-java.zip &&\
cp $name/*.jar $name/sources/ &&\
cd $name/sources/ &&\
unzip *.jar &&\
cd ../..;
done;
