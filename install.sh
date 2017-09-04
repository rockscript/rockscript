#!/usr/bin/env bash

EXECUTABLE=/usr/local/bin/rock

echo "#!/usr/bin/env bash" > $EXECUTABLE
echo "java -jar `pwd`/rockscript/target/rockscript.jar" '$@' >> $EXECUTABLE
chmod a+x /usr/local/bin/rock

echo Created $EXECUTABLE
echo Test it by typing rock -Enter-
