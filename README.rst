=================================
Genfreq : Simple signal generator
=================================

What is it ?
============

This project is a signal generator electronic board created during my scholar cursus.

I share it so that people can make their own if they are courageous.

Special warning for ETSMTL students: Do not copy for your lab, it is plagiarism.

Synopsis
========

Computer can feed data in RAM through a USB-parallel FIFO converter and a CPLD.

Than the CPLD outputs this buffer to a DAC (according to some parameters) when a start command is received.
The resulting signal can be routed to filters/amplifiers with jumpers.

The bandwidth is about 20MHz and the buffer is 64k samples wide (with 14 bits DAC precision).

Files
=====

board
  This directory contains informations about the board itself: schematics, Altium project, gerbers

vhdl
  This directory contains VHDL code present in CPLD, as well as some unit tests

code
  This directory contains code for PC side

Authors
=======

The project was originally created by Florent Touchard and Simon Turcotte-Langevin in 2011
