#!/usr/bin/perl -w

use strict;
local(*FILE);
my $seq = "";

open(FILE, $ARGV[0]) || die "Cannot open \"$ARGV[0]\": $!";
while(<FILE>){
    if($_ =~ /^LOCUS/){ $seq = ""; }
    elsif($_ =~ /^ *[0-9]+ [a-z]/){ 
            $_ =~ s/[^a-z]//g;
            $seq .= $_; # $seq = $seq . $_ �ƈӖ��͓��������A��葬�����������
    }
    elsif($_ =~ /^\/\//){
		#�����͍l���܂��傤
    }
}
close FILE;

