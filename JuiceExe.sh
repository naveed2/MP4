for PARAM; do
        args=$args" "$PARAM
done
java -Xmx2g -jar JuiceExe.jar $args
