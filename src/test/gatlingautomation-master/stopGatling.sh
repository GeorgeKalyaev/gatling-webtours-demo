var_pids=$(pgrep -f gatling)

for pid in $var_pids
do
  kill -9 $pid
  echo $pid убит
done
echo gatling остановлен