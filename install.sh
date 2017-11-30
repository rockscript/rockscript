#!/usr/bin/env bash

EXECUTABLE=/usr/local/bin/rock
# EXECUTABLE_DEBUG=${EXECUTABLE}d
echo "#!/usr/bin/env bash" > $EXECUTABLE
echo "java -jar `pwd`/rockscript-cli/target/rockscript-cli.jar" '$@' >> $EXECUTABLE
chmod a+x $EXECUTABLE
echo Created $EXECUTABLE
echo Test it by typing rock -Enter-

if [ -n "${EXECUTABLE_DEBUG}" ]; then
  echo "#!/usr/bin/env bash" > $EXECUTABLE_DEBUG
  echo "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=3003 -jar `pwd`/rockscript-cli/target/rockscript-cli.jar" '$@' >> $EXECUTABLE_DEBUG
  chmod a+x $EXECUTABLE_DEBUG
  echo Created $EXECUTABLE_DEBUG
fi
