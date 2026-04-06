cd ./gatling/output/
var_name=$(ls | tail -n 1)
tail -f $var_name -n1000