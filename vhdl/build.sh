#!/bin/bash

primary_unit='top_level'
ieee_standard='synopsys'
flags="--std=02 --ieee=$ieee_standard"

echo "Analysis..."
for i in *.vhd; do
	echo "====> $i"
	ghdl -a $flags $i
done


echo ""
echo "Elaborating primary unit : $primary_unit..."
ghdl -e $flags $primary_unit && echo "DONE"
