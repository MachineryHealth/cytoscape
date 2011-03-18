#!/usr/bin/perl -w

use strict;

# ������get_sequence, save_sequence�Ȃǂ̊֐���u��

sub get_sequence {

   my($fh) = @_; #�t�@�C���n���h��
   my $seq = "";
   my $seq_frag;

   while(<$fh>){
      if($_ =~ /^\/\//){ # //����������A�����ŏI��
         last;
      }
      else {
         $seq_frag = $_; #�ǂ݂��񂾍s�̉���z���$seq_frag�Ɋi�[����B
         $seq_frag =~ s/[^a-z]//g;  # �����A�󔒂Ȃǂ͍폜�B
         $seq .= $seq_frag; # $seq�ɔz��S�̂��i�[
      }
   }
   return $seq;
}

sub save_sequence {

    my($filename, $fh) = @_;
    my($seq_frag);
    local(*SEQFILE);

    open(SEQFILE, "> $filename");
       # $filename�Ƃ������O�̃t�@�C�����������ݗp�ɃI�[�v������

    while(<$fh>){
        if($_ =~ /^\/\//){
            last;
        }
        else {
            $seq_frag = $_;
            $seq_frag =~ s/[^a-z]//g;
            print SEQFILE $seq_frag;
        }
    }

    close SEQFILE;
}


my(@cds_start_set, $cds_start); # @cds_start_set�ɊJ�n�ʒu���L�^���Ă���
my($cds_count, $atg_count); # CDS�̐��Catg�Ŏn�܂�CDS�̐����J�E���g
open(FILE, $ARGV[0]) || die "Cannot open \"$ARGV[0]\": $!\n";

$cds_count = 0;
$atg_count = 0;

while(<FILE>){ # [[1]]
   chomp; # �s���[�̉��s�L��������
   if($_ =~ /^LOCUS/){
      # [[2]]
      @cds_start_set = ();
   }
   elsif($_ =~ /^     CDS             ([0-9]+)\.\.([0-9]+)/){
      # [[3]]
      $cds_start = $1;
      # $cds_end   = $2;
      push(@cds_start_set, $cds_start);
      $cds_count ++;
   }
   elsif($_ =~ /^ORIGIN/){
      # [[4,5]]
      my $seq = &get_sequence(*FILE);
      for $cds_start (@cds_start_set){
         if(substr($seq, $cds_start - 1, 3) eq "atg"){
            $atg_count ++;
         }
      }
   }
}

close FILE;

print "$atg_count / $cds_count = ", 1.0*$atg_count / $cds_count, "\n";
