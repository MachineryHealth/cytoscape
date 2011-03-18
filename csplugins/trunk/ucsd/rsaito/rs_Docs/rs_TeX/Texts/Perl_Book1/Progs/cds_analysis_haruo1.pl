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

my @cds_start_set; # @cds_start_set��CDS�J�n�ʒu���L�^���Ă���
my @cds_end_set;     # @cds_end_set��CDS�I���ʒu���L�^���Ă���
my $cds_count = 0; # CDS�̐����L�^
my $seq; # ����z��

open(FILE, $ARGV[0]) || die "Cannot open \"$ARGV[0]\": $!\n";

while(<FILE>){ # [[1]]
  chomp; # �s���[�̉��s�L��������
  if($_ =~ /^     CDS             ([0-9]+)\.\.([0-9]+)/){
    # [[3]]
    push(@cds_start_set, $1);
    push(@cds_end_set, $2);
    $cds_count ++;
  }
  elsif($_ =~ /^ORIGIN/){
    # [[4,5]]
    $seq = &get_sequence(*FILE);
  }
}

close FILE;

for my $ncds (0..$cds_count){
  my $cds_seq = substr($seq,
                       $cds_start_set[ $ncds ] - 1,
                       $cds_end_set[ $ncds ] - $cds_start_set[ $ncds ] + 1);
  print "$ncds\t$cds_seq\n";
  
}

