#!/usr/bin/env bash

EXECUTABLE=/usr/local/bin/rock
EXECUTABLE_DEBUG=${EXECUTABLE}d
echo "#!/usr/bin/env bash" > $EXECUTABLE
echo "java -jar `pwd`/rockscript/target/rockscript.jar" '$@' >> $EXECUTABLE
chmod a+x $EXECUTABLE

echo "#!/usr/bin/env bash" > $EXECUTABLE_DEBUG
echo "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=3003 -jar `pwd`/rockscript/target/rockscript.jar" '$@' >> $EXECUTABLE_DEBUG
chmod a+x $EXECUTABLE_DEBUG

echo Created $EXECUTABLE and $EXECUTABLE_DEBUG
echo Test it by typing rock -Enter-
