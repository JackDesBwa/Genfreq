#!/bin/bash

ieee_standard='synopsys'

for i in *.vhd; do
	echo "====> $i"
	ghdl -a --std=02 --ieee=$ieee_standard $i
done
