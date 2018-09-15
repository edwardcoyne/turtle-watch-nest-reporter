#!/bin/bash

tools_dir=$(pwd)/../tools
dir=appengine-endpoints
v=1

rm -r $dir/*.jar 
cd $dir &&\
$tools_dir/appengine-java-sdk-1.9.64/bin/endpoints.sh get-client-lib --war=../../server/TurtleNestReporter-AppEngine/war\
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
