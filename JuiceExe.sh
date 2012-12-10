for PARAM; do
        args=$args" "$PARAM
done
java -Xmx4g -jar JuiceExe.jar $args
