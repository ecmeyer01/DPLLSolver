#!/bin/bash

E_BADARGS=65
if [ $# -ne 1 ]
then
	echo "Usage: `basename $0` <input>"
	exit $E_BADARGS
fi
	
input=$1

#java -cp src/ solver.sat.Main $input

python src/main.py $input
