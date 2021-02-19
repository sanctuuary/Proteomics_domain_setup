echo "--------------------------TEST STARTS-----------------------------------"

echo "---------------------------------1---------------------------------------"

java -Xmx14024m -XX:+HeapDumpOnOutOfMemoryError -jar APE-1.1.1-executable.jar 1 3 1

echo "-------------------------------------------------------------------------"

java -Xmx14024m -XX:+HeapDumpOnOutOfMemoryError -jar APE-1.1.1-executable.jar 1 3 2


echo "---------------------------------3---------------------------------------"

java -Xmx14024m -XX:+HeapDumpOnOutOfMemoryError -jar APE-1.1.1-executable.jar 3 3 1

echo "-------------------------------------------------------------------------"

java -Xmx14024m -XX:+HeapDumpOnOutOfMemoryError -jar APE-1.1.1-executable.jar 3 3 2

echo "----------------------------------4--------------------------------------"

java -Xmx14024m -XX:+HeapDumpOnOutOfMemoryError -jar APE-1.1.1-executable.jar 4 3 1

echo "-------------------------------------------------------------------------"

java -Xmx14024m -XX:+HeapDumpOnOutOfMemoryError -jar APE-1.1.1-executable.jar 4 3 2

echo "---------------------------------2---------------------------------------"

java -Xmx14024m -XX:+HeapDumpOnOutOfMemoryError -jar APE-1.1.1-executable.jar 2 3 2

echo "-------------------------------------------------------------------------"

java -Xmx14024m -XX:+HeapDumpOnOutOfMemoryError -jar APE-1.1.1-executable.jar 2 3 1

echo "-------------------------------------------------------------------------"

echo "--------------------------TEST ENDS--------------------------------------"
